def get_project_names(branch: str) -> list[str]:
    projects = []
    projects += get_front_project_names()
    projects += get_back_project_names(branch)
    return projects


def get_front_project_names() -> list[str]:
    return sorted([
        "vigia_ng_webviewer",
        "vigia_ng_workflow"
    ])


def get_back_project_names(branch: str) -> list[str]:
    shared = [
        # cloud-control:
        "auth-service",
        "config-server",
        "eureka-server",
        "user-service",
        "zuul-server",

        # cloud-vigiang:
        "block-service",
        "carrier-service",
        "dashboard-service",
        "data-retention-service",
        "event-service",
        "interception-service",
        "log-service",
        "message-service",
        "operation-service",
        "portability-service",
        "process-service",
        "report-service",
        "scheduler-service",
        "sittel-service",
        "system-service",
        "tracking-service",
        "voucher-service",
        "warrant-service",
    ]

    if branch.startswith("version-2."):
        return sorted(shared)
    elif branch.startswith("version-3."):
        return sorted(shared + [
            # cloud-vigiang:
            "websocket-service",
            "wms-service",
        ])
    else:
        raise ValueError(f"Unsupported branch prefix: '{branch}'. Expected 'version-2.' or 'version-3.'.")


def get_current_branches() -> list[str]:
    return ["version-2.3.0", "version-3.1.0", "version-3.2.0"]


def validate_laboratory_tasks(
    source_branch: str,
    active_laboratories: list[dict],
    tasks: list[dict],
) -> list[str]:
    """
    Find the task entry whose 'branch' matches source_branch and validate that
    every laboratory name it lists exists among the active laboratories.

    Returns the validated list of laboratory names.
    Raises ValueError if no/multiple matching entries are found, or if any
    listed laboratory is missing from the active laboratories.
    """
    matches = [task for task in tasks if task.get("branch") == source_branch]
    if len(matches) == 0:
        raise ValueError(
            f"No laboratory task entry found for branch '{source_branch}'."
        )
    if len(matches) > 1:
        raise ValueError(
            f"Multiple laboratory task entries found for branch '{source_branch}', "
            f"expected exactly one."
        )

    requested = matches[0].get("laboratories", [])
    active_names = {lab.get("name") for lab in active_laboratories}
    missing = [name for name in requested if name not in active_names]
    if missing:
        raise ValueError(
            f"The following laboratories for branch '{source_branch}' were not "
            f"found among active laboratories: {missing}."
        )

    return requested

