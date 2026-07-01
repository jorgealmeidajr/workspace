import gitlab

from shared import (
    connect_gitlab,
    validate_previous_branches,
    validate_source_branch,
)


class UpdateLaboratoriesController:
    def __init__(self, source_branch: str, previous_branches: list[str]) -> None:
        self.source_branch = source_branch
        self.previous_branches = previous_branches
        self._validate_branch()
        self.gl: gitlab.Gitlab = connect_gitlab()

    def _validate_branch(self) -> None:
        """Validate the previous branches and the source branch."""
        validate_previous_branches(self.previous_branches)
        validate_source_branch(self.source_branch, self.previous_branches)

