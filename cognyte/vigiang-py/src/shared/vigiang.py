import paramiko
import yaml

SSH_TIMEOUT_SECONDS = 10


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


def check_laboratory_ssh(laboratory: dict) -> None:
    """
    Open an SSH connection to the given laboratory to verify it is reachable.

    Uses the laboratory's 'sshHost', 'sshPort', 'sshUsername' and 'sshPassword'
    fields. Raises an exception if the connection cannot be established.
    """
    client = paramiko.SSHClient()
    client.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    try:
        client.connect(
            hostname=laboratory.get("sshHost"),
            port=laboratory.get("sshPort", 22),
            username=laboratory.get("sshUsername"),
            password=laboratory.get("sshPassword"),
            timeout=SSH_TIMEOUT_SECONDS,
            banner_timeout=SSH_TIMEOUT_SECONDS,
            auth_timeout=SSH_TIMEOUT_SECONDS,
        )
    finally:
        client.close()


def check_laboratories_up(
    task_names: list[str],
    active_laboratories: list[dict],
) -> None:
    """
    Verify every laboratory in 'task_names' is reachable over SSH, using the
    connection data found in 'active_laboratories' matched by the 'name' field.

    Prints a per-laboratory confirmation on success, collects all failures and
    raises a single RuntimeError listing every laboratory that did not respond.
    """
    labs_by_name = {lab.get("name"): lab for lab in active_laboratories}

    failures: list[str] = []
    for name in task_names:
        laboratory = labs_by_name.get(name)
        if laboratory is None:
            failures.append(f"{name}: not found among active laboratories")
            continue

        try:
            check_laboratory_ssh(laboratory)
            print(f"Laboratory '{name}' ({laboratory.get('sshHost')}) is reachable.")
        except Exception as error:
            failures.append(f"{name} ({laboratory.get('sshHost')}): {error}")

    if failures:
        details = "\n  - ".join(failures)
        raise RuntimeError(
            f"The following laboratories did not respond over SSH:\n  - {details}"
        )


def run_laboratory_ssh_command(laboratory: dict, command: str) -> str:
    """
    Open an SSH connection to the given laboratory, run 'command' and return its
    standard output as text.

    Uses the laboratory's 'sshHost', 'sshPort', 'sshUsername' and 'sshPassword'
    fields. Raises a RuntimeError if the command exits with a non-zero status,
    or any underlying exception if the connection cannot be established.
    """
    client = paramiko.SSHClient()
    client.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    try:
        client.connect(
            hostname=laboratory.get("sshHost"),
            port=laboratory.get("sshPort", 22),
            username=laboratory.get("sshUsername"),
            password=laboratory.get("sshPassword"),
            timeout=SSH_TIMEOUT_SECONDS,
            banner_timeout=SSH_TIMEOUT_SECONDS,
            auth_timeout=SSH_TIMEOUT_SECONDS,
        )
        _stdin, stdout, stderr = client.exec_command(command, timeout=SSH_TIMEOUT_SECONDS)
        exit_status = stdout.channel.recv_exit_status()
        output = stdout.read().decode("utf-8", errors="replace")
        error_output = stderr.read().decode("utf-8", errors="replace")
        if exit_status != 0:
            raise RuntimeError(
                f"Command '{command}' failed with exit status {exit_status}: "
                f"{error_output.strip()}"
            )
        return output
    finally:
        client.close()


def extract_backend_images(compose_text: str, back_project_names: list[str]) -> list[str]:
    """
    Parse a docker-compose YAML document and return the list of service image
    references whose image name matches one of 'back_project_names'.

    Matching is done by substring: an image is kept when any backend project
    name appears within the image string (e.g. 'event-service' matches
    'registry/vigiang/event-service:3.1.0.rc01').
    """
    document = yaml.safe_load(compose_text)
    if not isinstance(document, dict):
        raise ValueError("Invalid docker-compose content: expected a mapping at the root.")

    services = document.get("services") or {}
    if not isinstance(services, dict):
        raise ValueError("Invalid docker-compose content: 'services' is not a mapping.")

    images: list[str] = []
    for service in services.values():
        if not isinstance(service, dict):
            continue
        image = service.get("image")
        if not image:
            continue
        if any(name in image for name in back_project_names):
            images.append(image)

    return images


