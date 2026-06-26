import urllib3
from dotenv import load_dotenv

from shared.vigiang import get_front_project_names, get_back_project_names, get_current_branches
from shared import connect_gitlab, get_projects_data, find_untagged_projects


def print_untagged_new_commits(project_data: dict) -> None:
    """Print projects that have new commits (newest) with no version tag."""
    untagged_projects = find_untagged_projects(project_data)

    if not untagged_projects:
        print("\n✅ No projects have untagged new commits.")
        return

    print(f"\n{'═' * 60}")
    print("Projects with new commits ahead of latest tag:")
    print(f"{'═' * 60}")
    for entry in untagged_projects:
        tags = entry["tags"]
        tag_suffix = "🏷️ " + ", ".join(tags) if tags else ""
        print(f"\n📦 {entry['project_name']} {tag_suffix}")


def main() -> None:
    print("Starting untagged-commits check...")

    branches = get_current_branches()

    load_dotenv()
    urllib3.disable_warnings(urllib3.exceptions.InsecureRequestWarning)

    gl = connect_gitlab()

    for branch in branches:
        print(f"{'─' * 120}")
        print(f"Branch: {branch}")

        version = ".".join(branch.replace("version-", "").split(".")[:2])

        print("  Processing front projects...")
        project_names = get_front_project_names()
        projects_data = get_projects_data(branch, gl, project_names, version)
        print_untagged_new_commits(projects_data)
        print("\n")

        print("  Processing back projects...")
        project_names = get_back_project_names(branch)
        projects_data = get_projects_data(branch, gl, project_names, version)
        print_untagged_new_commits(projects_data)

    print("\nEnding script.")


if __name__ == "__main__":
    main()

