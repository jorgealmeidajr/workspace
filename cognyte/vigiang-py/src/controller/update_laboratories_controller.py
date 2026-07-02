import gitlab

from shared.environment import (
    get_laboratories_vigia_ng,
    get_branch_laboratories_vigia_ng,
)
from shared import (
    connect_gitlab,
    validate_previous_branches,
    validate_source_branch,
    validate_laboratories_from_branch,
    check_laboratories_up,
    run_laboratory_ssh_command,
    extract_backend_images,
    get_back_project_names,
    get_projects_data,
    find_projects_last_tag,
)


class UpdateLaboratoriesController:
    def __init__(self, source_branch: str, previous_branches: list[str]) -> None:
        self.source_branch = source_branch
        self.previous_branches = previous_branches
        self._validate_branches()
        self.gl: gitlab.Gitlab = connect_gitlab()


    def _validate_branches(self) -> None:
        validate_previous_branches(self.previous_branches)
        validate_source_branch(self.source_branch, self.previous_branches)


    def _get_active_laboratories(self) -> list[dict]:
        laboratories = get_laboratories_vigia_ng()
        return [lab for lab in laboratories if lab.get("active")]


    def load_data(self) -> None:
        active_laboratories = self._get_active_laboratories()

        branch_laboratory_map = get_branch_laboratories_vigia_ng()
        branch_laboratory_names = validate_laboratories_from_branch(
            self.source_branch, active_laboratories, branch_laboratory_map
        )
        print(f"Laboratories to update for branch '{self.source_branch}': {branch_laboratory_names}")

        branch_laboratories = [
            lab for lab in active_laboratories if lab.get("name") in branch_laboratory_names
        ]

        print("Checking laboratories are reachable over SSH...")
        check_laboratories_up(branch_laboratory_names, branch_laboratories)

        version = ".".join(self.source_branch.replace("version-", "").split(".")[:2])

        print("Processing BACK projects...")
        back_project_names = get_back_project_names(self.source_branch)
        back_projects_data = get_projects_data(self.source_branch, self.gl, back_project_names, version)
        self.back_projects_last_tag = find_projects_last_tag(back_projects_data, version, self.previous_branches)

        print("Reading backend images from laboratories' docker-compose...")
        for laboratory in branch_laboratories:
            lab_name = laboratory.get("name")
            try:
                compose_text = run_laboratory_ssh_command(
                    laboratory, f"cat /opt/vigiang/scripts/docker-compose.yml"
                )
                backend_images = extract_backend_images(compose_text, back_project_names)
            except Exception as error:
                print(f"❌ Failed to read backend images from '{lab_name}': {error}")
                raise

            self.lab_backend_images[lab_name] = backend_images


    def execute(self) -> None:
        print("Updating laboratories...")

