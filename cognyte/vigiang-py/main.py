# $ pip install -r requirements.txt
import gitlab
import urllib3
import os
from dotenv import load_dotenv

load_dotenv()

urllib3.disable_warnings(urllib3.exceptions.InsecureRequestWarning)

private_token = os.getenv('GITLAB_PRIVATE_TOKEN')
gl = gitlab.Gitlab('https://brgit01.cognyte.local/', private_token=private_token, ssl_verify=False)

projects = gl.projects.list(owned=True, all=True)
projects = sorted(projects, key=lambda p: p.name.lower())

for project in projects:
    print(f"{project.id} - {project.name} - {project.web_url}")

project_name = 'warrant-service'
project = gl.projects.list(search=project_name)[0]

print(f"\nFound project: {project.id} - {project.name} - {project.web_url}")

variables = project.variables.list()
deploy_hosts = next((var.value for var in variables if var.key == 'DEPLOY_HOSTS'), None)

print(f"DEPLOY_HOSTS: {deploy_hosts}")

new_value = "10.50.153.101 10.50.153.107 10.50.153.112 10.50.153.103 10.50.153.118"

variable = project.variables.get('DEPLOY_HOSTS')
variable.value = new_value
variable.save()

print(f"Updated DEPLOY_HOSTS to: {new_value}")

version = "2.2"

branches = project.branches.list(all=True)
print(f"\nBranches containing '{version}' ({sum(1 for b in branches if version in b.name)}):")
for branch in branches:
    if version in branch.name:
        print(f"  - {branch.name}")

tags = project.tags.list(all=True)
print(f"\nTags containing '{version}' ({sum(1 for t in tags if version in t.name)}):")
for tag in tags:
    if version in tag.name:
        print(f"  - {tag.name}")
        if tag.message:
            print(f"    Description: {tag.message}")
