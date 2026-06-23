from dataclasses import dataclass, field
from typing import List
import git


###################################################################################################

@dataclass
class FileChange:
    old_path: str
    new_path: str
    new_file: bool
    deleted_file: bool
    renamed_file: bool
    diff: str


@dataclass
class CommitChanges:
    sha: str
    title: str
    author: str
    files: List[FileChange] = field(default_factory=list)


def get_commits_from_hash(
    repo: git.Repo,
    branch: str,
    start_hash: str
) -> List[git.Commit]:
    """Return commits on `branch` starting from (and including) `start_hash`, oldest-first."""
    print(f"[INFO] Fetching commits on '{branch}' from hash '{start_hash}'...")
    try:
        # rev range: from start_hash up to the tip of the branch (inclusive)
        commits = list(repo.iter_commits(f"{start_hash}^..{branch}"))  # newest-first
    except git.GitCommandError:
        print(f"[ERROR] Commit '{start_hash}' not found on branch '{branch}'.")
        return []

    print(f"[INFO] {len(commits)} commit(s) found.")
    return list(reversed(commits))  # oldest-first


def get_file_changes(commit: git.Commit) -> List[FileChange]:
    """Return the list of file changes for a given commit."""
    # Diff against first parent (or empty tree for root commits)
    if commit.parents:
        diffs = commit.parents[0].diff(commit, create_patch=True)
    else:
        diffs = commit.diff(git.NULL_TREE, create_patch=True)

    result = []
    for d in diffs:
        try:
            diff_text = d.diff.decode("utf-8", errors="replace") if d.diff else ""
        except Exception:
            diff_text = ""
        result.append(FileChange(
            old_path=d.a_path,
            new_path=d.b_path,
            new_file=d.new_file,
            deleted_file=d.deleted_file,
            renamed_file=d.renamed_file,
            diff=diff_text,
        ))
    return result


def get_commit_changes(
    repo: git.Repo,
    branch: str,
    start_hash: str
) -> List[CommitChanges]:
    """Get all commits from `start_hash` on `branch`, with their file-level diffs."""
    commits = get_commits_from_hash(repo, branch, start_hash)
    total = len(commits)
    result = []
    for i, commit in enumerate(commits, start=1):
        print(f"[INFO] Processing commit {i}/{total}: {commit.hexsha[:8]} - {commit.summary}")
        files = get_file_changes(commit)
        result.append(CommitChanges(
            sha=commit.hexsha,
            title=commit.summary,
            author=str(commit.author),
            files=files,
        ))
    return result


def print_file_diff(diff: str) -> None:
    """Print a unified diff with clear markers for added/removed/context lines."""
    for line in diff.splitlines():
        if line.startswith("@@"):
            print(f"    \033[36m{line}\033[0m") # cyan  — hunk header
        elif line.startswith("+"):
            print(f"    \033[32m{line}\033[0m") # green — added
        elif line.startswith("-"):
            print(f"    \033[31m{line}\033[0m") # red   — removed
        else:
            print(f"    {line}")                # white — context


def print_commit_changes(commit_changes: List[CommitChanges]) -> None:
    for cc in commit_changes:
        print(f"\n{'='*80}")
        print(f"Commit : {cc.sha}")
        print(f"Title  : {cc.title}")
        print(f"Author : {cc.author}")
        print(f"Files changed: {len(cc.files)}")
        for fc in cc.files:
            label = (
                "[NEW]"     if fc.new_file     else
                "[DELETED]" if fc.deleted_file else
                "[RENAMED]" if fc.renamed_file else
                "[MODIFIED]"
            )
            print(f"\n  {label} {fc.new_path}")
            if fc.diff:
                print_file_diff(fc.diff)
            else:
                print("    (no diff available)")


###################################################################################################


def main() -> None:
    print("starting script6: get commits and changes from a local repository.")

    REPO_PATH  = r"C:\work\vigiang\2.3\front-2.3\vigia_ng_workflow"
    BRANCH     = "version-2.3.0"
    START_HASH = "a13eef69"

    repo = git.Repo(REPO_PATH)

    commit_changes = get_commit_changes(repo, BRANCH, START_HASH)
    print_commit_changes(commit_changes)

    print("\nending script6.")


if __name__ == "__main__":
    main()

