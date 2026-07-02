import urllib3
from pathlib import Path
from dotenv import load_dotenv

from shared.environment import get_vigia_ng_path
from shared.vigiang import get_front_project_names, get_back_project_names, get_current_branches
from shared import connect_gitlab, get_projects_data, write_content


def write_tags_md(
    project_data: dict,  # project_name → {"commits": [...], "tag_map": {...}}
    output_path: Path,
) -> None:
    lines = []
    for project_name, data in project_data.items():
        lines.append(f"\n# {project_name}\n")
        lines.append("```\n")

        commits = data["commits"]
        tag_map = data["tag_map"]

        if not commits:
            lines.append("No commits found.\n")
            lines.append("```\n")
            continue

        for commit in commits:
            sha = commit.id
            short_id = commit.short_id
            date = (commit.authored_date or "")[:10]
            title = commit.title

            tag_names = tag_map.get(sha, [])
            tag_suffix = ""
            if tag_names:
                tag_suffix = " 🏷️ " + ", ".join(sorted(tag_names))

            lines.append(f"[{short_id}] {date} - {title}{tag_suffix}\n")

        lines.append("```\n")

    write_content(output_path, "".join(lines))



def main() -> None:
    print("Starting to write the TAGS...")

    tasks_folder = Path(get_vigia_ng_path()) / "tasks"
    branches = get_current_branches()

    load_dotenv()
    urllib3.disable_warnings(urllib3.exceptions.InsecureRequestWarning)

    gl = connect_gitlab()

    for branch in branches:
        print(f"{'─' * 120}")
        print(f"Branch: {branch}")

        version = ".".join(branch.replace("version-", "").split(".")[:2])
        version_path = tasks_folder / version
        version_path.mkdir(parents=True, exist_ok=True)

        print("  Processing front projects...")
        project_names = get_front_project_names()
        md_path = version_path / f"{version}.tags.front.md"

        projects_data = get_projects_data(branch, gl, project_names, version)
        write_tags_md(projects_data, md_path)
        print("\n")

        print("  Processing back projects...")
        project_names = get_back_project_names(branch)
        md_path = version_path / f"{version}.tags.back.md"

        projects_data = get_projects_data(branch, gl, project_names, version)
        write_tags_md(projects_data, md_path)

    print("\nEnding script.")


if __name__ == "__main__":
    # todo: improve the logging
    main()

