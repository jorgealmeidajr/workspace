def get_project_names(branch: str) -> list[str]:
    shared = [
        # frontend:
        "vigia_ng_webviewer",
        "vigia_ng_workflow",

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
        return shared.copy()
    elif branch.startswith("version-3."):
        return shared + [
            # cloud-vigiang:
            "websocket-service",
            "wms-service",
        ]
    else:
        raise ValueError(f"Unsupported branch prefix: '{branch}'. Expected 'version-2.' or 'version-3.'.")

