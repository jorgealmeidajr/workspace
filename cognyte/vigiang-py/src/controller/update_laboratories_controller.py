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
        self.back_project_names: list[str] = []
        self.back_projects_last_tag: list[dict] = []
        self.lab_backend_images: dict[str, list[str]] = {}


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

        print("Processing BACK projects...")
        back_project_names = get_back_project_names(self.source_branch)
        self.back_project_names = back_project_names
        self.back_projects_last_tag = find_projects_last_tag(self.gl, back_project_names, self.source_branch, self.previous_branches)

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


    def _match_project_name(self, image: str) -> str | None:
        for name in self.back_project_names:
            if name in image:
                return name
        return None


    def execute(self) -> None:
        print("Updating laboratories...")

        expected_tags = {
            entry.get("name"): entry.get("last_tag")
            for entry in self.back_projects_last_tag
        }

        for lab_name, images in self.lab_backend_images.items():
            print(f"\nLaboratory '{lab_name}':")
            mismatches: list[str] = []

            for image in images:
                project_name = self._match_project_name(image)
                if project_name is None:
                    print(f"⚠️ No matching BACK project for image '{image}'")
                    continue

                deployed_tag = image.rsplit(":", 1)[-1] if ":" in image else ""
                expected_tag = expected_tags.get(project_name)
                if expected_tag is None:
                    print(f"⚠️ No expected tag for project '{project_name}'")
                    continue

                if deployed_tag != expected_tag:
                    mismatches.append(
                        f"{project_name}: expected {expected_tag}, got {deployed_tag}"
                    )

            if mismatches:
                print("\n".join(mismatches))
            else:
                print(f"✅ {lab_name} is up to date")

