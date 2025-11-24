# $ pip install -r requirements.txt
import gitlab
import urllib3
import os
import time
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
branches = [b for b in branches if version in b.name]
print(f"\nBranches containing '{version}' ({len(branches)}):")
for branch in branches:
    if version in branch.name:
        print(f"  - {branch.name}")

tags = project.tags.list(all=True)
tags = [t for t in tags if version in t.name]
print(f"\nTags containing '{version}' ({len(tags)}):")
for tag in tags:
    print(f"  - {tag.name}")
    if tag.message:
        print(f"    Description: {tag.message}")


def main():
    pass

def create_tag_draft():
    tag_name = "2.2.3"
    tag_ref = "version-2.2.0"  # Branch or commit SHA to create tag from
    tag_message = "Official Release - Based on branch: version-2.2.0"
    # tag_message = "Release Candidate - Based on branch: version-2.2.0"

    #new_tag = project.tags.create({'tag_name': tag_name, 'ref': tag_ref, 'message': tag_message})
    print(f"\nCreated tag: {new_tag.name}")
    print(f"  Message: {new_tag.message}")
    print(f"  Commit: {new_tag.commit['id']}")

    # Get the pipeline status from the newly created tag
    #commit = project.commits.get(new_tag.commit['id'])
    pipelines = commit.pipelines.list()

    if pipelines:
        pipeline = pipelines[0] # Get the most recent pipeline
        print(f"\nPipeline status: {pipeline.status}")
        print(f"Pipeline ID: {pipeline.id}")
        print(f"Pipeline URL: {pipeline.web_url}")

        # Wait for pipeline to finish
        pipeline.refresh()
        while pipeline.status in ['pending', 'running']:
            time.sleep(10)  # Wait 10 seconds
            pipeline.refresh()
            print(f"Pipeline status: {pipeline.status}")
    else:
        print("\nNo pipeline found for this tag")

    print(f"Final pipeline status: {pipeline.status}")

if __name__ == "__main__":
    main()
