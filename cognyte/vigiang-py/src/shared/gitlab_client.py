import os
import gitlab


def parse_version(branch: str) -> tuple[int, ...]:
    """Extract a version tuple from a branch name like 'version-3.1.0'."""
    parts = branch.split("-", 1)
    raw = parts[-1] if len(parts) > 1 else parts[0]
    try:
        return tuple(int(x) for x in raw.split("."))
    except ValueError:
        raise ValueError(f"Cannot parse a version from branch name '{branch}'.")


def validate_previous_branches(branches: list[str]) -> None:
    """
    Validate that previous_branches has at least 1 element and each element
    is a strictly higher version than the one that follows it (descending order).
    """
    if len(branches) < 1:
        raise ValueError(
            f"previous_branches must contain at least 1 element, got {len(branches)}."
        )
    for i in range(len(branches) - 1):
        v_current = parse_version(branches[i])
        v_next = parse_version(branches[i + 1])
        if v_current <= v_next:
            raise ValueError(
                f"previous_branches must be in strictly descending version order, "
                f"but '{branches[i]}' {v_current} is not higher than '{branches[i + 1]}' {v_next}."
            )


def validate_source_branch(source: str, previous_branches: list[str]) -> None:
    """
    Validate that source_branch is a strictly higher version than the first
    (highest) element of previous_branches.
    """
    v_source = parse_version(source)
    v_first_previous = parse_version(previous_branches[0])
    if v_source <= v_first_previous:
        raise ValueError(
            f"source_branch '{source}' {v_source} must be a higher version than "
            f"the first previous branch '{previous_branches[0]}' {v_first_previous}."
        )


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


def get_branch_commits(project: gitlab.v4.objects.Project, branch: str) -> list:
    """Return all commits on a branch, ordered newest → oldest (GitLab default)."""
    try:
        return project.commits.list(ref_name=branch, all=True)
    except gitlab.exceptions.GitlabListError as e:
        print(f"  ⚠️ Could not fetch commits for '{branch}' in '{project.name}': {e}")
        return []


def get_version_tags(project: gitlab.v4.objects.Project, version_prefix: str) -> list:
    """Return tags whose name starts with version_prefix."""
    try:
        all_tags = project.tags.list(all=True)
        return [t for t in all_tags if t.name.startswith(version_prefix)]
    except gitlab.exceptions.GitlabListError as e:
        print(f"  ⚠️ Could not fetch tags for '{project.name}': {e}")
        return []


def build_tag_map(tags: list) -> dict:
    """Build a dict mapping commit SHA → list of tag names."""
    tag_map: dict[str, list[str]] = {}
    for tag in tags:
        sha = tag.commit["id"]
        tag_map.setdefault(sha, []).append(tag.name)
    return tag_map


def process_project(
    project: gitlab.v4.objects.Project,
    branch: str,
    version_prefix: str,
) -> dict:
    """
    Fetch commits & version tags for a project, then return the slice of commits
    starting from the oldest tagged commit (inclusive) up to HEAD.
    """
    commits = get_branch_commits(project, branch)
    tags = get_version_tags(project, version_prefix)
    tag_map = build_tag_map(tags)

    if not commits:
        return {"commits": [], "tag_map": {}}

    # commits are newest→oldest; find the last tagged commit (= oldest tag)
    oldest_tag_index = None
    for i, commit in enumerate(commits):
        if commit.id in tag_map:
            oldest_tag_index = i  # keep updating; last hit is the oldest tag

    if oldest_tag_index is None:
        print(f"  ℹ️ No version tags found for '{project.name}' — skipping.")
        return {"commits": [], "tag_map": {}}
    else:
        sliced = commits[: oldest_tag_index + 1]

    return {"commits": sliced, "tag_map": tag_map}


def get_projects_data(branch: str, gl: gitlab.Gitlab, project_names: list[str], version: str) -> dict:
    project_data: dict = {}
    for project_name in project_names:
        try:
            project = get_project(gl, project_name)
        except ValueError as e:
            print(f"  Error processing project: '{project_name}'...")
            print(f"  ❌ {e}")
            project_data[project_name] = {"commits": [], "tag_map": {}}
            continue

        project_data[project_name] = process_project(project, branch, version)
    return project_data


def find_untagged_projects(project_data: dict) -> list[dict]:
    """
    Return the projects that have new commits (newest) ahead of their latest
    version tag.

    For each such project, the entry contains the project name and the raw tag
    names of the first (newest) tagged commit found behind the new commits:
        {"project_name": str, "tags": list[str]}
    """
    untagged: list[dict] = []
    for project_name, data in project_data.items():
        commits = data["commits"]
        tag_map = data["tag_map"]

        # Find the index of the first (newest) tagged commit
        first_tagged_index = None
        for i, commit in enumerate(commits):
            if commit.id in tag_map:
                first_tagged_index = i
                break

        if first_tagged_index is None or first_tagged_index == 0:
            # No tags at all, or HEAD itself is tagged → no untagged new commits
            continue

        context_commit = commits[first_tagged_index]
        tags = sorted(tag_map.get(context_commit.id, []))
        untagged.append({"project_name": project_name, "tags": tags})

    return untagged

