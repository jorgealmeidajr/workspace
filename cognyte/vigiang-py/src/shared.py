import os
import gitlab


def connect_gitlab() -> gitlab.Gitlab:
    private_token = os.getenv('GITLAB_PRIVATE_TOKEN')
    gitlab_url = os.getenv('GITLAB_URL')
    gl = gitlab.Gitlab(gitlab_url, private_token=private_token, ssl_verify=False)
    print(f"Connected to GitLab: {gitlab_url}")
    return gl


def get_project(gl: gitlab.Gitlab, project_name: str) -> gitlab.v4.objects.Project:
    projects = gl.projects.list(search=project_name, all=True)
    match = next((p for p in projects if p.name.lower() == project_name.lower()), None)
    if match is None:
        raise ValueError(f"Project '{project_name}' not found.")
    return match

