import os
import re
import argparse
import urllib3
import gitlab
from pathlib import Path
from dotenv import load_dotenv
from gitlab import Gitlab
from jira import JIRA, JIRAError

from shared.environment import get_vigia_ng_path
from shared.vigiang import get_front_project_names, get_back_project_names, get_current_branches
from shared import connect_gitlab, get_project, write_content, get_merged_requests, get_mr_commits


# Jira project prefixes we recognize. Detection is restricted to these to avoid
# false positives from fragments like "77_fix".
JIRA_PREFIXES = ("NISR", "VRP")

# Matches both hyphen and underscore variants, case-insensitively, e.g.
# "VRP-2242", "vrp-2242" and "vrp_2242_unifique" (trailing "_unifique" ignored).
# A lookbehind (instead of \b) allows a leading underscore, as in branch names
# like "77_fix_vrp_2242_unifique", while still rejecting letter/digit prefixes.
JIRA_KEY_PATTERN = re.compile(
    rf"(?<![A-Za-z0-9])(?:{'|'.join(JIRA_PREFIXES)})[-_]\d+",
    re.IGNORECASE,
)

GITLAB_ISSUE_PATTERN = re.compile(r"(?:Closes|Related to)\s+#(\d+)", re.IGNORECASE)


def connect_jira() -> JIRA:
    """
    Build a Jira client using JIRA_URL and JIRA_TOKEN from the environment.

    Returns a configured ``JIRA`` client (Jira Server/Data Center personal
    access token auth) so callers can query the Jira REST API directly.
    """
    base_url = (os.getenv("JIRA_URL") or "").rstrip("/")
    token = os.getenv("JIRA_TOKEN")

    client = JIRA(server=base_url, token_auth=token, options={"verify": False})

    print(f"Connected to Jira: {base_url}")
    return client


def get_jira_issue(client: JIRA, key: str) -> dict | None:
    """
    Fetch a Jira issue's title (summary) and status by its key.

    On any failure (network error, missing/invalid issue, no access) the failure
    is logged and ``None`` is returned so callers can skip the invalid key.
    """
    jira_url = f"{client.server_url}/browse/{key}"
    try:
        issue = client.issue(key, fields="summary,status")
        title = getattr(issue.fields, "summary", "") or ""
        status = getattr(getattr(issue.fields, "status", None), "name", "") or ""
        return {"key": key, "url": jira_url, "title": title, "status": status}
    except JIRAError as e:
        message = (e.text or "").strip() or str(e)
        print(f"⚠️ Could not fetch Jira issue '{key}', skipping: HTTP {e.status_code}: {message}")
        return None
    except Exception as e:
        print(f"⚠️ Could not fetch Jira issue '{key}', skipping: {e}")
        return None


def extract_jira_keys(text: str) -> list[str]:
    """Return the unique Jira keys found in a piece of text (order preserved).

    Matches are normalized to canonical uppercase-hyphen form (e.g. ``vrp_2242``
    and ``vrp-2242`` both become ``VRP-2242``).
    """
    if not text:
        return []
    keys: list[str] = []
    for match in JIRA_KEY_PATTERN.findall(text):
        key = match.upper().replace("_", "-")
        if key not in keys:
            keys.append(key)
    return keys


def extract_issue_ids(text: str) -> list[int]:
    """
    Return the unique GitLab issue IIDs referenced via "Closes"/"Related to"
    in a piece of text (order preserved).
    """
    if not text:
        return []
    ids: list[int] = []
    for match in GITLAB_ISSUE_PATTERN.findall(text):
        iid = int(match)
        if iid not in ids:
            ids.append(iid)
    return ids


def get_gitlab_issue(project: gitlab.v4.objects.Project, iid: int):
    """
    Fetch a GitLab issue by its IID.

    On any failure (missing issue, no access, network error) the failure is
    logged and ``None`` is returned so callers can skip the reference.
    """
    try:
        return project.issues.get(iid)
    except Exception as e:
        print(f"⚠️ Could not fetch GitLab issue #{iid} in '{project.name}', skipping: {e}")
        return None


def collect_mr_jiras(project: gitlab.v4.objects.Project, branch: str) -> list:
    """
    Scan every merged MR (and its commit messages) for Jira keys.

    Returns a list of dicts, one per MR, each holding the MR ``web_url``,
    ``merged_at`` date, ``author`` name and the list of unique Jira keys found
    in the MR title and its commit messages.
    """
    mr_jiras: list = []
    issue_cache: dict[int, object] = {}
    for mr in get_merged_requests(project, branch):
        keys = extract_jira_keys(mr.title)

        extract_jira_keys_from_mr(issue_cache, keys, mr, project)

        for commit in get_mr_commits(mr):
            message = getattr(commit, "message", "") or commit.title
            for key in extract_jira_keys(message):
                if key not in keys:
                    keys.append(key)

        mr_jiras.append({
            "web_url": mr.web_url,
            "mr_title": mr.title,
            "merged_at": (mr.merged_at or "")[:10],
            "author": mr.author.get("name", "") if mr.author else "",
            "keys": keys,
        })

    return mr_jiras


def extract_jira_keys_from_mr(issue_cache: dict[int, object], keys: list[str], mr, project):
    for iid in extract_issue_ids(getattr(mr, "description", "") or ""):
        if iid not in issue_cache:
            issue_cache[iid] = get_gitlab_issue(project, iid)
        issue = issue_cache[iid]
        if issue is None:
            continue
        issue_text = f"{getattr(issue, 'title', '') or ''}\n{getattr(issue, 'description', '') or ''}"
        for key in extract_jira_keys(issue_text):
            if key not in keys:
                keys.append(key)


def write_jiras_md(
    project_data: dict,  # project_name → [{web_url, merged_at, author, keys}]
    client: JIRA,
    output_path: Path,
) -> None:
    lines = []
    for project_name, mr_jiras in project_data.items():
        project_lines = []
        for mr in mr_jiras:
            keys = mr["keys"]
            if not keys:
                continue

            issues = [issue for key in keys if (issue := get_jira_issue(client, key)) is not None]
            if not issues:
                continue

            project_lines.append(f"[{mr['merged_at']}] [{mr['author']}]\n")
            project_lines.append(f"{mr['mr_title']}\n")
            project_lines.append(f"{mr['web_url']}\n")

            for issue in issues:
                status = issue["status"]
                if status.strip().lower() == "done":
                    status = "✅ DONE"
                project_lines.append(
                    f"  [{issue['key']}] [{status}]\n"
                    f"  {issue['title'].strip()}\n"
                    f"  {issue['url']}\n"
                    f"\n"
                )
            project_lines.append("\n")

        if not project_lines:
            continue

        lines.append(f"\n# {project_name}\n")
        lines.append("```\n")
        lines.extend(project_lines)
        lines.append("```\n")

    write_content(output_path, "".join(lines))


def collect_jiras(branch: str, project_names: list[str], gl: Gitlab, label: str) -> dict:
    print(f"## Fetching Jira references for {label} projects...")
    project_data: dict = {}
    for project_name in project_names:
        try:
            project = get_project(gl, project_name)
        except ValueError as e:
            print(f"❌ {e}")
            project_data[project_name] = []
            continue

        project_data[project_name] = collect_mr_jiras(project, branch)

    return project_data


# usage example: python src/write_jiras.py 2.3
def main(initial_version: str | None = None) -> None:
    print("Starting to write the JIRAS...")

    tasks_folder = Path(get_vigia_ng_path()) / "tasks"

    if initial_version is None:
        branches = get_current_branches()
    else:
        branch = f"version-{initial_version}.0"
        valid_branches = get_current_branches()
        if branch not in valid_branches:
            valid_versions = [b.replace("version-", "").rsplit(".", 1)[0] for b in valid_branches]
            raise ValueError(
                f"Invalid version '{initial_version}'. Valid versions are: {', '.join(valid_versions)}."
            )
        branches = [branch]

    load_dotenv()
    urllib3.disable_warnings(urllib3.exceptions.InsecureRequestWarning)

    gl = connect_gitlab()
    client = connect_jira()

    for branch in branches:
        print(f"{'─' * 60}")
        print(f"# Branch: {branch}")

        version = ".".join(branch.replace("version-", "").split(".")[:2])
        version_path = tasks_folder / version
        version_path.mkdir(parents=True, exist_ok=True)

        project_data: dict = {}
        project_data.update(collect_jiras(branch, get_front_project_names(), gl, "FRONT"))
        project_data.update(collect_jiras(branch, get_back_project_names(branch), gl, "BACK"))

        md_path = version_path / f"{version}.jiras.md"
        write_jiras_md(project_data, client, md_path)

    print("\nEnding script.")


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Write Jira references markdown files per branch.")
    parser.add_argument(
        "version",
        nargs="?",
        default=None,
        help="Optional initial version (e.g. '2.3'). If omitted, all current branches are processed.",
    )
    args = parser.parse_args()
    main(args.version)

