import gitlab
import urllib3
from dotenv import load_dotenv
from dataclasses import dataclass
from typing import List

from shared.environment import get_laboratories_vigia_ng
from shared import connect_gitlab


def get_projects(gl: gitlab.Gitlab) -> list[gitlab.v4.objects.Project]:
    projects = gl.projects.list(owned=True, all=True)
    return sorted(projects, key=lambda p: p.name.lower())


def get_branches(project: gitlab.v4.objects.Project, version: str) -> list:
    branches = project.branches.list(all=True)
    return [b for b in branches if version in b.name]


def get_tags(project: gitlab.v4.objects.Project, version: str) -> list:
    tags = project.tags.list(all=True)
    return [t for t in tags if version in t.name]



###################################################################################################
@dataclass
class Request:
    laboratories_to_update: List[str]
    backend_services_to_update: List[str]
    frontend: bool
    backend: bool


def update_deploy_hosts(gl: gitlab.Gitlab, req: Request) -> None:
    projects = get_projects(gl)

    laboratories = get_laboratories_vigia_ng()

    laboratories_filtered = [
        lab for lab in laboratories
        if lab["name"].lower() in [name.lower() for name in req.laboratories_to_update]
    ]

    if req.backend:
        update_backend_projects(laboratories_filtered, projects, req)

    if req.frontend:
        update_frontend_projects(laboratories_filtered, projects)


def update_frontend_projects(laboratories_filtered: list, projects: list) -> None:
    frontend_deploy_hosts = " ".join(
        f'{lab["sshHost"]}-{lab["alias"]}' for lab in laboratories_filtered
    )
    print(f"frontend deploy hosts: {frontend_deploy_hosts}")

    frontend_project = next(
        (project for project in projects if project.name.lower() == "vigia_ng_app"),
        None
    )

    update_frontend_variable(frontend_deploy_hosts, frontend_project, 'DEPLOY_HOSTS_WEBVIEWER')
    update_frontend_variable(frontend_deploy_hosts, frontend_project, 'DEPLOY_HOSTS_WORKFLOW')
    print("Frontend deploy hosts update completed.\n")


def update_frontend_variable(frontend_deploy_hosts: str, frontend_project, variable_name: str) -> None:
    try:
        var = frontend_project.variables.get(variable_name)
        var.value = frontend_deploy_hosts
        var.save()
        print(f"Updated {variable_name} for project {frontend_project.name}")
    except gitlab.exceptions.GitlabGetError:
        print(f"{variable_name} not found for project {frontend_project.name}, skipping.")


def update_backend_projects(laboratories_filtered: list, projects: list, req: Request) -> None:
    backend_deploy_hosts = " ".join(lab["sshHost"] for lab in laboratories_filtered)
    print(f"backend deploy hosts: {backend_deploy_hosts}")

    backend_projects = [
        project for project in projects
        if project.name.lower() in [name.lower() for name in req.backend_services_to_update]
    ]

    for project in backend_projects:
        try:
            var = project.variables.get('DEPLOY_HOSTS')
            var.value = backend_deploy_hosts
            var.save()
            print(f"Updated DEPLOY_HOSTS for project {project.name}")
        except gitlab.exceptions.GitlabGetError:
            print(f"DEPLOY_HOSTS not found for project {project.name}, skipping.")
    print("Backend deploy hosts update completed.\n")


def main() -> None:
    print("starting script1: update deploy hosts.")

    req = Request(
        laboratories_to_update = "TIM,CLARO1,VIVO,ENTEL,MOVISTAR,QA1".split(","),
        backend_services_to_update = "system-service,zuul-server".split(","),
        frontend = True,
        backend = False
    )

    load_dotenv()
    urllib3.disable_warnings(urllib3.exceptions.InsecureRequestWarning)

    gl = connect_gitlab()

    update_deploy_hosts(gl, req)

    print("ending script1: update deploy hosts.")


if __name__ == "__main__":
    # todo: pass version param, get projects to tag, change deploy hosts, generate tags
    # front you have to play deploy
    # save the projects you alter the deploy hosts in a file
    # other script will reset deploy hosts
    main()
