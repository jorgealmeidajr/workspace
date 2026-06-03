import gitlab
import urllib3
from pathlib import Path
from dotenv import load_dotenv
from gitlab import Gitlab

from environment import get_vigia_ng_path
from vigiang import get_front_project_names, get_back_project_names
from shared import connect_gitlab, get_project


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
    print(f"  ✅  Written: {output_path}\n")


def main() -> None:
    print("Starting script3: update commits history log.\n")

    tasks_folder = Path(get_vigia_ng_path()) / "tasks"
    branches = ["version-2.3.0", "version-3.1.0", "version-3.2.0"]

    load_dotenv()
    urllib3.disable_warnings(urllib3.exceptions.InsecureRequestWarning)

    gl = connect_gitlab()

    for branch in branches:
        print(f"\n{'─' * 60}")
        print(f"Branch: {branch}")

        version = ".".join(branch.replace("version-", "").split(".")[:2])
        version_path = tasks_folder / version
        version_path.mkdir(parents=True, exist_ok=True)

        project_names = get_front_project_names()
        md_path = version_path / f"{version}.mrs.front.md"
        write_mrs(branch, project_names, gl, md_path)

        project_names = get_back_project_names(branch)
        md_path = version_path / f"{version}.mrs.back.md"
        write_mrs(branch, project_names, gl, md_path)

    print("\nEnding script3.")


def write_mrs(branch: str, project_names: list[str], gl: Gitlab, md_path: Path):
    project_mrs: dict = {}
    for project_name in project_names:
        try:
            project = get_project(gl, project_name)
        except ValueError as e:
            print(f"  ❌  {e}")
            project_mrs[project_name] = []
            continue

        print(f"  Fetching merged requests for '{project_name}'...")
        mrs = get_merged_requests(project, branch)
        project_mrs[project_name] = mrs

    write_branch_md(branch, project_mrs, md_path)


if __name__ == "__main__":
    main()
