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
    run_laboratory_ssh_command,
    extract_backend_images,
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
    #NEXT_TAG = ""
    CURRENT_BRANCH = "version-3.2.0"

    validate_previous_branches(PREVIOUS_BRANCHES)
    validate_source_branch(SOURCE_BRANCH, PREVIOUS_BRANCHES)

    load_dotenv()
    urllib3.disable_warnings(urllib3.exceptions.InsecureRequestWarning)

    gl = connect_gitlab()

    active_laboratories = get_active_laboratories()

    branch_laboratory_map = get_branch_laboratories_vigia_ng()
    branch_laboratory_names = validate_laboratory_tasks(SOURCE_BRANCH, active_laboratories, branch_laboratory_map)
    print(f"Laboratories to update for branch '{SOURCE_BRANCH}': {branch_laboratory_names}")

    branch_laboratories = [lab for lab in active_laboratories if lab.get("name") in branch_laboratory_names]

    print("Checking laboratories are reachable over SSH...")
    check_laboratories_up(branch_laboratory_names, branch_laboratories)

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

    print("Reading backend images from laboratories' docker-compose...")
    DOCKER_COMPOSE_PATH = "/opt/vigiang/scripts/docker-compose.yml"
    lab_backend_images: dict[str, list[str]] = {}
    for laboratory in branch_laboratories:
        lab_name = laboratory.get("name")
        try:
            compose_text = run_laboratory_ssh_command(
                laboratory, f"cat {DOCKER_COMPOSE_PATH}"
            )
            backend_images = extract_backend_images(compose_text, back_project_names)
        except Exception as error:
            print(f"❌ Failed to read backend images from '{lab_name}': {error}")
            raise

        lab_backend_images[lab_name] = backend_images
        print(f"\nBackend images for laboratory '{lab_name}':")
        for image in backend_images:
            print(f"  - {image}")

    answer = input("\nDo you want to create the new tags? yes(y) or no(n)? ").strip().lower()
    if answer in {"y", "yes"}:
        print("Tags will be created...")

    print("\nEnding script.")


if __name__ == "__main__":
    main()

