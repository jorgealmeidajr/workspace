
def get_project_names(branch: str) -> list[str]:
    projects = []
    projects += get_front_project_names(branch)
    projects += get_back_project_names(branch)
    return projects


def get_front_project_names(branch: str) -> list[str]:
    shared = [
        "vigia_ng_webviewer",
        "vigia_ng_workflow",
    ]

    # 'vigia-components' was introduced in version 3.1.
    if branch.startswith("version-3."):
        minor = int(branch.replace("version-", "").split(".")[1])
        if minor >= 1:
            return sorted(shared + ["vigia-components"])

    return sorted(shared)


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
    return ["version-2.2.0", "version-2.3.0", "version-3.1.0", "version-3.2.0"]

