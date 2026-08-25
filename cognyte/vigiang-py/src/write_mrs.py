import argparse
import urllib3
from pathlib import Path
from dotenv import load_dotenv
from gitlab import Gitlab

from shared.environment import get_vigia_ng_path
from shared.vigiang import get_front_project_names, get_back_project_names, get_current_branches
from shared import connect_gitlab, get_project, write_content, get_merged_requests, get_mr_commits


def get_mr_changed_files(mr) -> list:
    try:
        return mr.changes()["changes"]
    except Exception as e:
        print(f"⚠️ Could not fetch changed files for MR !{mr.iid}: {e}")
        return []


def write_branch_md(project_mrs: dict, output_path: Path) -> None:
    lines = []
    for project_name, mrs in project_mrs.items():
        lines.append(f"\n# {project_name}\n")
        lines.append("```\n")

        if not mrs:
            lines.append("No merged requests found.\n")
            lines.append("```\n")
            continue

        for mr in sorted(mrs, key=lambda m: m.merged_at or "", reverse=True):
            iid = mr.iid
            date = (mr.merged_at or "")[:10]
            title = mr.title
            author = mr.author.get("name", "") if mr.author else ""
            lines.append(f"[!{iid}] {date} - {author} - {title}\n")

            lines.append("[commits]:\n")
            for commit in get_mr_commits(mr):
                short_id = commit.short_id
                commit_title = commit.title
                commit_date = (commit.authored_date or "")[:10]
                commit_author = commit.author_name or ""
                lines.append(f"  [{short_id}] {commit_date} - {commit_author} - {commit_title}\n")

            lines.append("[files]:\n")
            for change in get_mr_changed_files(mr):
                lines.append(f"  📄 {change['new_path']}\n")

            lines.append("\n")

        lines.append("```\n")

    write_content(output_path, "".join(lines))


def main(initial_version: str | None = None) -> None:
    print("Starting to write the MERGE REQUESTS...")

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

    for branch in branches:
        print(f"{'─' * 60}")
        print(f"# Branch: {branch}")

        version = ".".join(branch.replace("version-", "").split(".")[:2])
        version_path = tasks_folder / version
        version_path.mkdir(parents=True, exist_ok=True)

        project_names = get_front_project_names()
        md_path = version_path / f"{version}.mrs.front.md"
        write_mrs(branch, project_names, gl, md_path, "FRONT")

        project_names = get_back_project_names(branch)
        md_path = version_path / f"{version}.mrs.back.md"
        write_mrs(branch, project_names, gl, md_path, "BACK")

    print("\nEnding script.")


def write_mrs(branch: str, project_names: list[str], gl: Gitlab, md_path: Path, label: str):
    print(f"## Fetching merged requests for {label} projects...")
    project_mrs: dict = {}
    for project_name in project_names:
        try:
            project = get_project(gl, project_name)
        except ValueError as e:
            print(f"❌ {e}")
            project_mrs[project_name] = []
            continue

        mrs = get_merged_requests(project, branch)
        project_mrs[project_name] = mrs

    write_branch_md(project_mrs, md_path)


# exemple: python src/write_mrs.py 2.3
if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Write merged requests markdown files per branch.")
    parser.add_argument(
        "version",
        nargs="?",
        default=None,
        help="Optional initial version (e.g. '2.3'). If omitted, all current branches are processed.",
    )
    args = parser.parse_args()
    main(args.version)

