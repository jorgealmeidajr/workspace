import os
import re
import urllib3
import requests
import gitlab
from pathlib import Path
from dotenv import load_dotenv
from gitlab import Gitlab

from shared.environment import get_vigia_ng_path
from shared.vigiang import get_front_project_names, get_back_project_names, get_current_branches
from shared import connect_gitlab, get_project, write_content


JIRA_KEY_PATTERN = re.compile(r"\b([A-Z][A-Z0-9]+-\d+)\b")


def connect_jira() -> tuple[str, requests.Session]:
    """
    Build a Jira REST session using JIRA_URL and JIRA_TOKEN from the environment.

    Returns a tuple of (base_url, session). The session is preconfigured with the
    bearer token so callers can query the Jira REST API directly.
    """
    base_url = (os.getenv("JIRA_URL") or "").rstrip("/")
    token = os.getenv("JIRA_TOKEN")

    session = requests.Session()
    session.headers.update({
        "Authorization": f"Bearer {token}",
        "Accept": "application/json",
    })
    session.verify = False

    print(f"Connected to Jira: {base_url}")
    return base_url, session


def get_jira_issue(base_url: str, session: requests.Session, key: str) -> dict:
    """
    Fetch a Jira issue's title (summary) and status by its key.

    On any failure (network error, missing issue, no access) the failure is
    logged and an entry with empty title/status is returned so the run continues.
    """
    url = f"{base_url}/rest/api/2/issue/{key}?fields=summary,status"
    jira_url = f"{base_url}/browse/{key}"
    try:
        response = session.get(url, timeout=15)
        response.raise_for_status()
        fields = response.json().get("fields", {})
        title = fields.get("summary", "")
        status = (fields.get("status") or {}).get("name", "")
        return {"key": key, "url": jira_url, "title": title, "status": status}
    except Exception as e:
        print(f"⚠️ Could not fetch Jira issue '{key}': {e}")
        return {"key": key, "url": jira_url, "title": "", "status": ""}


def extract_jira_keys(text: str) -> list[str]:
    """Return the unique Jira keys found in a piece of text (order preserved)."""
    if not text:
        return []
    keys: list[str] = []
    for match in JIRA_KEY_PATTERN.findall(text):
        if match not in keys:
            keys.append(match)
    return keys


def get_merged_requests(project: gitlab.v4.objects.Project, branch: str) -> list:
    try:
        return project.mergerequests.list(state='merged', target_branch=branch, all=True)
    except gitlab.exceptions.GitlabListError as e:
        print(f"⚠️ Could not fetch merge requests for '{branch}' in '{project.name}': {e}")
        return []


def get_mr_commits(mr) -> list:
    try:
        return mr.commits()
    except Exception as e:
        print(f"⚠️ Could not fetch commits for MR !{mr.iid}: {e}")
        return []


def collect_mr_jiras(project: gitlab.v4.objects.Project, branch: str) -> dict:
    """
    Scan every merged MR (and its commit messages) for Jira keys.

    Returns a dict mapping the MR web URL → list of unique Jira keys found in the
    MR title and its commit messages.
    """
    mr_jiras: dict = {}
    for mr in get_merged_requests(project, branch):
        keys = extract_jira_keys(mr.title)

        for commit in get_mr_commits(mr):
            message = getattr(commit, "message", "") or commit.title
            for key in extract_jira_keys(message):
                if key not in keys:
                    keys.append(key)

        mr_jiras[mr.web_url] = keys

    return mr_jiras


def write_jiras_md(
    project_data: dict,  # project_name → {mr_web_url: [jira keys]}
    base_url: str,
    session: requests.Session,
    output_path: Path,
) -> None:
    lines = []
    for project_name, mr_jiras in project_data.items():
        project_lines = []
        for mr_url, keys in mr_jiras.items():
            if not keys:
                continue
            project_lines.append(f"{mr_url}\n")
            for key in keys:
                issue = get_jira_issue(base_url, session, key)
                status = issue["status"]
                if status.strip().lower() == "done":
                    status = "✅ DONE"
                project_lines.append(
                    f"  {issue['url']}\n"
                    f"    [{status}] {issue['title']}\n"
                )

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
            project_data[project_name] = {}
            continue

        project_data[project_name] = collect_mr_jiras(project, branch)

    return project_data


def main() -> None:
    print("Starting to write the JIRAS...")

    tasks_folder = Path(get_vigia_ng_path()) / "tasks"
    branches = get_current_branches()

    load_dotenv()
    urllib3.disable_warnings(urllib3.exceptions.InsecureRequestWarning)

    gl = connect_gitlab()
    base_url, session = connect_jira()

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
        write_jiras_md(project_data, base_url, session, md_path)

    print("\nEnding script.")


if __name__ == "__main__":
    main()
