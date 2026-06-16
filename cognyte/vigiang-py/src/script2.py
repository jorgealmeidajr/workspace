import gitlab
import urllib3
from dataclasses import dataclass
from dotenv import load_dotenv
from vigiang import get_project_names
from shared import connect_gitlab, get_project


@dataclass
class BranchCompareResult:
    project_name: str
    branch_a: str
    branch_b: str
    missing_commits: list  # commits in branch_a not yet in branch_b


def branch_exists(project: gitlab.v4.objects.Project, branch: str) -> bool:
    try:
        project.branches.get(branch)
        return True
    except gitlab.exceptions.GitlabGetError:
        return False


def compare_branches(
    project: gitlab.v4.objects.Project,
    branch_a: str,
    branch_b: str,
) -> BranchCompareResult | None:
    """
    Compare branch_a against branch_b.
    Returns the list of commits that are in branch_a but NOT yet in branch_b
    (i.e. commits that need to be sent from branch_a to branch_b).
    Returns None if one or both branches do not exist.
    """
    missing_branches = [b for b in (branch_a, branch_b) if not branch_exists(project, b)]
    if missing_branches:
        for b in missing_branches:
            print(f"❌  Branch '{b}' does not exist in project '{project.name}'. Skipping comparison.")
        return None

    # GitLab compare: from=branch_b, to=branch_a  →  gives commits in A not in B
    comparison = project.repository_compare(branch_b, branch_a)
    missing = comparison.get("commits", [])
    return BranchCompareResult(
        project_name=project.name,
        branch_a=branch_a,
        branch_b=branch_b,
        missing_commits=missing,
    )


def print_result(result: BranchCompareResult) -> None:
    print(f"\nProject : {result.project_name}")
    print(f"Source  : {result.branch_a}")
    print(f"Target  : {result.branch_b}")

    if not result.missing_commits:
        print("✅  No missing commits — branches are in sync.")
        return

    print(f"⚠️  {len(result.missing_commits)} commit(s) in '{result.branch_a}' "
          f"not yet in '{result.branch_b}':\n")
    for commit in result.missing_commits:
        short_id = commit.get("short_id", commit.get("id", "?")[:8])
        title = commit.get("title", "")
        author = commit.get("author_name", "")
        date = commit.get("created_at", "")[:10]
        print(f"  [{short_id}] {date} {author} — {title}")


def main() -> None:
    print("Starting script2: compare branches for missing commits.\n")

    # ── Configuration ────────────────────────────────────────────────────────
    # todo: use the array of branches
    # rules: the array must have at least 2 elements, and versions must be in order asc
    BRANCH_A = "version-2.3.0"  # Source branch (commits to send FROM)
    BRANCH_B = "version-3.1.0"  # Target branch (commits to send TO)

    PROJECT_NAMES = get_project_names(BRANCH_A)
    # ─────────────────────────────────────────────────────────────────────────

    load_dotenv()
    urllib3.disable_warnings(urllib3.exceptions.InsecureRequestWarning)

    gl = connect_gitlab()

    for project_name in PROJECT_NAMES:
        print(f"\n{'─' * 60}")
        try:
            project = get_project(gl, project_name)
        except ValueError as e:
            print(f"❌  {e}")
            continue
        result = compare_branches(project, BRANCH_A, BRANCH_B)
        if result is not None:
            print_result(result)

    print("\nEnding script2: compare branches for missing commits.")


if __name__ == "__main__":
    main()

