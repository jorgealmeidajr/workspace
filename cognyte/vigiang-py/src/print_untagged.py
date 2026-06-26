import urllib3
from dotenv import load_dotenv

from shared.vigiang import get_front_project_names, get_back_project_names, get_current_branches
from shared import connect_gitlab, get_projects_data


def print_untagged_new_commits(project_data: dict) -> None:
    """Print projects that have new commits (newest) with no version tag."""
    found_any = False
    for project_name, data in project_data.items():
        commits = data["commits"]
        tag_map = data["tag_map"]

        # Find the index of the first (newest) tagged commit
        first_tagged_index = None
        for i, commit in enumerate(commits):
            if commit.id in tag_map:
                first_tagged_index = i
                break

        if first_tagged_index is None or first_tagged_index == 0:
            # No tags at all, or HEAD itself is tagged → no untagged new commits
            continue

        # Also include the first tagged commit as context (like the example)
        context_commit = commits[first_tagged_index]

        if not found_any:
            print(f"\n{'═' * 60}")
            print("Projects with new commits ahead of latest tag:")
            print(f"{'═' * 60}")
            found_any = True

        tag_names = tag_map.get(context_commit.id, [])
        tag_suffix = "🏷️ " + ", ".join(sorted(tag_names)) if tag_names else ""
        print(f"\n📦 {project_name} {tag_suffix}")

    if not found_any:
        print("\n✅ No projects have untagged new commits.")


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

