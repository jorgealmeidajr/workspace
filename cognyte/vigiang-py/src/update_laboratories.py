import urllib3
from dotenv import load_dotenv

from shared.environment import (
    get_laboratories_vigia_ng,
    get_branch_laboratories_vigia_ng,
)
from shared import (
    connect_gitlab,
    validate_previous_branches,
    validate_source_branch,
    validate_laboratory_tasks,
    check_laboratories_up,
)
from shared import get_front_project_names, get_back_project_names, get_projects_data, find_untagged_projects


def get_active_laboratories() -> list[dict]:
    """Return only the laboratories flagged as active."""
    laboratories = get_laboratories_vigia_ng()
    return [lab for lab in laboratories if lab.get("active")]


def main() -> None:
    print("Starting to update LABORATORIES...")

    SOURCE_BRANCH = "version-3.1.0"
    PREVIOUS_BRANCHES = ["version-3.0.0"]

    validate_previous_branches(PREVIOUS_BRANCHES)
    validate_source_branch(SOURCE_BRANCH, PREVIOUS_BRANCHES)

    load_dotenv()
    urllib3.disable_warnings(urllib3.exceptions.InsecureRequestWarning)

    gl = connect_gitlab()

    active_laboratories = get_active_laboratories()

    branch_laboratories = get_branch_laboratories_vigia_ng()
    task_laboratories_to_update = validate_laboratory_tasks(SOURCE_BRANCH, active_laboratories, branch_laboratories)
    print(f"Laboratories to update for branch '{SOURCE_BRANCH}': {task_laboratories_to_update}")

    print("Checking laboratories are reachable over SSH...")
    check_laboratories_up(task_laboratories_to_update, active_laboratories)

    version = ".".join(SOURCE_BRANCH.replace("version-", "").split(".")[:2])

    print("Processing FRONT projects...")
    front_project_names = get_front_project_names()
    front_projects_data = get_projects_data(SOURCE_BRANCH, gl, front_project_names, version)
    front_untagged = find_untagged_projects(front_projects_data)

    print("Processing BACK projects...")
    back_project_names = get_back_project_names(SOURCE_BRANCH)
    back_projects_data = get_projects_data(SOURCE_BRANCH, gl, back_project_names, version)
    back_untagged = find_untagged_projects(back_projects_data)

    untagged_projects = front_untagged + back_untagged

    print(f"\n{'═' * 60}")
    print("Projects with new commits ahead of latest tag:")
    print(f"{'═' * 60}")
    if untagged_projects:
        for entry in untagged_projects:
            tag_suffix = ", ".join(entry["tags"]) if entry["tags"] else ""
            print(f"📦 {entry['project_name']} {tag_suffix}".rstrip())
    else:
        print("✅ No projects have untagged new commits.")

    print("\nEnding script.")


if __name__ == "__main__":
    main()

