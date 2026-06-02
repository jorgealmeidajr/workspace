import gitlab
import urllib3
import os
from pathlib import Path
from dotenv import load_dotenv
from environment import get_vigia_ng_path
from vigiang import get_project_names


def connect_gitlab() -> gitlab.Gitlab:
    private_token = os.getenv('GITLAB_PRIVATE_TOKEN')
    gitlab_url = os.getenv('GITLAB_URL')
    gl = gitlab.Gitlab(gitlab_url, private_token=private_token, ssl_verify=False)
    print(f"Connected to GitLab: {gitlab_url}")
    return gl


def get_project(gl: gitlab.Gitlab, project_name: str) -> gitlab.v4.objects.Project:
    projects = gl.projects.list(search=project_name, all=True)
    match = next((p for p in projects if p.name.lower() == project_name.lower()), None)
    if match is None:
        raise ValueError(f"Project '{project_name}' not found.")
    return match


def get_merged_requests(project: gitlab.v4.objects.Project, branch: str) -> list:
    try:
        return project.mergerequests.list(state='merged', target_branch=branch, all=True)
    except gitlab.exceptions.GitlabListError as e:
        print(f"  ⚠️  Could not fetch merge requests for '{branch}' in '{project.name}': {e}")
        return []


def get_mr_commits(mr) -> list:
    try:
        return mr.commits()
    except Exception as e:
        print(f"  ⚠️  Could not fetch commits for MR !{mr.iid}: {e}")
        return []


def write_branch_md(branch: str, project_mrs: dict, output_path: Path) -> None:
    lines = [f"# {branch}\n"]
    for project_name, mrs in project_mrs.items():
        lines.append(f"\n## {project_name}\n")
        lines.append("```\n")

        if not mrs:
            lines.append("No merged requests found.\n")
            lines.append("```\n")
            continue

        for mr in sorted(mrs, key=lambda m: m.merged_at or "", reverse=True):
            iid = mr.iid
            date = (mr.merged_at or "")[:10]
            title = mr.title
            lines.append(f"[!{iid}] {date} - {title}\n")

            for commit in get_mr_commits(mr):
                short_id = commit.short_id
                commit_title = commit.title
                commit_date = (commit.authored_date or "")[:10]
                lines.append(f"  [{short_id}] {commit_date} - {commit_title}\n")
            lines.append("\n")

        lines.append("```\n")

    output_path.parent.mkdir(parents=True, exist_ok=True)
    output_path.write_text("".join(lines), encoding="utf-8")
    print(f"  ✅  Written: {output_path}")


def main() -> None:
    print("Starting script3: update commits history log.\n")

    BRANCHES_FOLDER = Path(get_vigia_ng_path()) / "branches"

    BRANCHES = ["version-2.3.0", "version-3.0.0", "version-3.1.0"] # "version-3.2.0"

    load_dotenv()
    urllib3.disable_warnings(urllib3.exceptions.InsecureRequestWarning)

    gl = connect_gitlab()

    for BRANCH in BRANCHES:
        print(f"\n{'─' * 60}")
        print(f"Branch: {BRANCH}")

        PROJECT_NAMES = get_project_names(BRANCH)
        project_mrs: dict = {}

        for project_name in PROJECT_NAMES:
            try:
                project = get_project(gl, project_name)
            except ValueError as e:
                print(f"  ❌  {e}")
                project_mrs[project_name] = []
                continue

            print(f"  Fetching merged requests for '{project_name}'...")
            mrs = get_merged_requests(project, BRANCH)
            project_mrs[project_name] = mrs
            print(f"    → {len(mrs)} merged request(s)")

        md_path = BRANCHES_FOLDER / f"{BRANCH}.md"
        write_branch_md(BRANCH, project_mrs, md_path)

    print("\nEnding script3.")


if __name__ == "__main__":
    main()
