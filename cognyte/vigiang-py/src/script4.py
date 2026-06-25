import urllib3
from pathlib import Path
from dotenv import load_dotenv
import gitlab
from gitlab import Gitlab

from shared.environment import get_vigia_ng_path
from shared.vigiang import get_front_project_names, get_back_project_names, get_current_branches
from shared import connect_gitlab, get_project


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


def write_tags_md(
    project_data: dict,  # project_name → {"commits": [...], "tag_map": {...}}
    output_path: Path,
) -> None:
    lines = []
    for project_name, data in project_data.items():
        lines.append(f"\n# {project_name}\n")
        lines.append("```\n")

        commits = data["commits"]
        tag_map = data["tag_map"]

        if not commits:
            lines.append("No commits found.\n")
            lines.append("```\n")
            continue

        for commit in commits:
            sha = commit.id
            short_id = commit.short_id
            date = (commit.authored_date or "")[:10]
            title = commit.title

            tag_names = tag_map.get(sha, [])
            tag_suffix = ""
            if tag_names:
                tag_suffix = " 🏷️ " + ", ".join(sorted(tag_names))

            lines.append(f"[{short_id}] {date} - {title}{tag_suffix}\n")

        lines.append("```\n")

    output_path.parent.mkdir(parents=True, exist_ok=True)
    output_path.write_text("".join(lines), encoding="utf-8")
    print(f"  ✅ Written: {output_path}\n")


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


def print_untagged_new_commits(project_data: dict) -> None:
    """Print projects that have new commits (newest) with no version tag."""
    found_any = False
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

        # Also include the first tagged commit as context (like the example)
        context_commit = commits[first_tagged_index]

        if not found_any:
            print(f"\n{'═' * 60}")
            print("Projects with new commits ahead of latest tag:")
            print(f"{'═' * 60}")
            found_any = True

        tag_names = tag_map.get(context_commit.id, [])
        tag_suffix = "🏷️ " + ", ".join(sorted(tag_names)) if tag_names else ""
        print(f"\n📦 {project_name} {tag_suffix}")

    if not found_any:
        print("\n✅ No projects have untagged new commits.")


def main() -> None:
    print("Starting script4: read tags.\n")

    tasks_folder = Path(get_vigia_ng_path()) / "tasks"
    branches = get_current_branches()

    load_dotenv()
    urllib3.disable_warnings(urllib3.exceptions.InsecureRequestWarning)

    gl = connect_gitlab()

    for branch in branches:
        print(f"\n{'─' * 120}")
        print(f"Branch: {branch}")

        version = ".".join(branch.replace("version-", "").split(".")[:2])
        version_path = tasks_folder / version
        version_path.mkdir(parents=True, exist_ok=True)

        print("  Processing front projects...")
        project_names = get_front_project_names()
        md_path = version_path / f"{version}.tags.front.md"

        projects_data = get_projects_data(branch, gl, project_names, version)
        write_tags_md(projects_data, md_path)
        print_untagged_new_commits(projects_data)
        print("\n")

        print("  Processing back projects...")
        project_names = get_back_project_names(branch)
        md_path = version_path / f"{version}.tags.back.md"

        projects_data = get_projects_data(branch, gl, project_names, version)
        write_tags_md(projects_data, md_path)
        print_untagged_new_commits(projects_data)

    print("\nEnding script4.")


def get_projects_data(branch: str, gl: Gitlab, project_names: list[str], version: str) -> dict:
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


if __name__ == "__main__":
    # todo: split the print untagged commits into a separate script, since it's not needed for the md files
    main()

