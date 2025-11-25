# $ pip install -r requirements.txt
import gitlab
import urllib3
import os
from dotenv import load_dotenv


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
def main():
    print("starting script1")
    
    load_dotenv()
    urllib3.disable_warnings(urllib3.exceptions.InsecureRequestWarning)

    gl = connect_gitlab()

    projects = get_projects(gl)

    print("ending script1...")


if __name__ == "__main__":
    main()
