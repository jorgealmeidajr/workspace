def get_project_names(branch: str) -> list[str]:
    return sorted(set(get_front_project_names()) | set(get_back_project_names(branch)))


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

