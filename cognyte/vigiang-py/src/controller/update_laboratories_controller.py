from shared import (
    validate_previous_branches,
    validate_source_branch,
)


class UpdateLaboratoriesController:
    @staticmethod
    def validate_branch(source_branch: str, previous_branches: list[str]) -> None:
        """Validate the previous branches and the source branch."""
        validate_previous_branches(previous_branches)
        validate_source_branch(source_branch, previous_branches)

