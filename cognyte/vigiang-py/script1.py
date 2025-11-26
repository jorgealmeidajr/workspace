import gitlab
import urllib3
import os
from dotenv import load_dotenv
from dataclasses import dataclass
from typing import List

from environment import get_laboratories_vigia_ng


def get_projects(gl):
    projects = gl.projects.list(owned=True, all=True)
    return sorted(projects, key=lambda p: p.name.lower())

def get_branches(project, version):
    branches = project.branches.list(all=True)
    return [b for b in branches if version in b.name]

def get_tags(project, version):
    tags = project.tags.list(all=True)
    return [t for t in tags if version in t.name]

def connect_gitlab():
    private_token = os.getenv('GITLAB_PRIVATE_TOKEN')
    gitlab_url = os.getenv('GITLAB_URL')
    gl = gitlab.Gitlab(gitlab_url, private_token=private_token, ssl_verify=False)
    print(f"connected with gitlab {gitlab_url}")
    return gl

###################################################################################################
@dataclass
class Request:
    laboratories_to_update: List[str]
    backend_services_to_update: List[str]
    frontend: bool


def update_deploy_hosts(gl, req):
    projects = get_projects(gl)

    laboratories = get_laboratories_vigia_ng()

    laboratories_filtered = [
        lab for lab in laboratories
        if lab["name"].lower() in [name.lower() for name in req.laboratories_to_update]
    ]

    update_backend_projects(laboratories_filtered, projects, req)

    if req.frontend:
        frontend_deploy_hosts = " ".join(
            f'{lab["sshHost"]}-{lab["alias"]}' for lab in laboratories_filtered
        )
        print(f"frontend deploy hosts: {frontend_deploy_hosts}")


def update_backend_projects(laboratories_filtered, projects, req):
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


def main():
    print("starting script1: update deploy hosts.")

    req = Request(
        laboratories_to_update = "CLARO-01,ENTEL,MOVISTAR,TIM,VIVO".split(","),
        backend_services_to_update = "system-service,zuul-server".split(","),
        frontend = True
    )

    load_dotenv()
    urllib3.disable_warnings(urllib3.exceptions.InsecureRequestWarning)

    gl = connect_gitlab()

    update_deploy_hosts(gl, req)

    print("ending script1: update deploy hosts.")


if __name__ == "__main__":
    main()
