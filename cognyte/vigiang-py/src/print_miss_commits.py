import gitlab
import urllib3
from dataclasses import dataclass
from dotenv import load_dotenv
from shared.vigiang import get_project_names
from shared import connect_gitlab, get_project, parse_version


@dataclass
class BranchCompareResult:
    project_name: str
    branch_a: str
    branch_b: str
    missing_commits: list  # commits in branch_a not yet in branch_b


def validate_target_branches(branches: list[str], require_multiple: bool = False) -> None:
    """
    Validate that target_branches has the required number of elements and each
    element is a strictly higher version than the one that follows it
    (descending order).

    By default, at least 1 element is required. Pass ``require_multiple=True`` to
    require at least 2 elements.
    """
    minimum = 2 if require_multiple else 1
    if len(branches) < minimum:
        raise ValueError(
            f"target_branches must contain at least {minimum} element(s), got {len(branches)}."
        )
    for i in range(len(branches) - 1):
        v_current = parse_version(branches[i])
        v_next = parse_version(branches[i + 1])
        if v_current <= v_next:
            raise ValueError(
                f"target_branches must be in strictly descending version order, "
                f"but '{branches[i]}' {v_current} is not higher than '{branches[i + 1]}' {v_next}."
            )


def validate_source_branch(source: str, target_branches: list[str]) -> None:
    """
    Validate that source_branch is a strictly lower version than the first
    (highest) element of target_branches.
    """
    v_source = parse_version(source)
    v_first_target = parse_version(target_branches[0])
    if v_source >= v_first_target:
        raise ValueError(
            f"source_branch '{source}' {v_source} must be a lower version than "
            f"the first target branch '{target_branches[0]}' {v_first_target}."
        )


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
            print(f"❌ Branch '{b}' does not exist in project '{project.name}'. Skipping comparison.")
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
    print(f"Project : {result.project_name}")
    print(f"Source  : {result.branch_a}")
    print(f"Target  : {result.branch_b}")

    if not result.missing_commits:
        print("✅ No missing commits — branches are in sync.")
        return

    print(f"⚠️ {len(result.missing_commits)} commit(s) in '{result.branch_a}' "
          f"not yet in '{result.branch_b}':\n")
    for commit in result.missing_commits:
        short_id = commit.get("short_id", commit.get("id", "?")[:8])
        title = commit.get("title", "")
        author = commit.get("author_name", "")
        date = commit.get("created_at", "")[:10]
        print(f"  [{short_id}] {date} {author} — {title}")


def process_source_branch(
    gl: gitlab.Gitlab,
    source_branch: str,
    target_branches: list[str],
) -> None:
    """
    Compare a single source branch against its target branches across every
    relevant project, printing the missing commits for each.
    """
    print(f"{'─' * 120}")
    print(f"Comparing source '{source_branch}' → targets {target_branches}")

    project_names = get_project_names(source_branch)

    for project_name in project_names:
        print(f"{'─' * 60}")
        try:
            project = get_project(gl, project_name)
        except ValueError as e:
            print(f"❌ {e}")
            continue

        branch_b = next(
            (b for b in target_branches if branch_exists(project, b)),
            None,
        )
        if branch_b is None:
            print(f"❌ None of the target branches {target_branches} exist in project '{project_name}'. Skipping.")
            continue

        result = compare_branches(project, source_branch, branch_b)
        if result is not None:
            print_result(result)


def main() -> None:
    print("Starting to compare branches looking for missing commits...")

    # ----------------------------------------------------------------
    # Each entry maps a source branch (commits to send FROM) to its target
    # branches (commits to send TO); the first existing target is used.
    configs: list[tuple[str, list[str]]] = [
        ("version-2.2.0", ["version-2.3.0"]),
        ("version-2.3.0", ["version-3.1.0", "version-3.0.0"]),
        ("version-3.1.0", ["version-3.2.0"]),
    ]
    # ----------------------------------------------------------------

    # Validate every config upfront so an invalid entry stops the run before
    # any GitLab work begins.
    for source_branch, target_branches in configs:
        validate_target_branches(target_branches)
        validate_source_branch(source_branch, target_branches)

    load_dotenv()
    urllib3.disable_warnings(urllib3.exceptions.InsecureRequestWarning)

    gl = connect_gitlab()

    for source_branch, target_branches in configs:
        process_source_branch(gl, source_branch, target_branches)

    print("\nEnding script.")


if __name__ == "__main__":
    main()

