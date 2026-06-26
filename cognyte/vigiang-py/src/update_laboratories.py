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
from shared import get_front_project_names, get_back_project_names


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

    project_names = get_front_project_names()

    project_names = get_back_project_names(SOURCE_BRANCH)

    print("\nEnding script.")


if __name__ == "__main__":
    main()

