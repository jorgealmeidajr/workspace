from dataclasses import dataclass, field
from pathlib import Path
from typing import List
import git

from shared.environment import get_vigia_ng_path


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
    date: str
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
            date=commit.committed_datetime.strftime("%Y-%m-%d %H:%M:%S"),
            files=files,
        ))
    return result


def write_commit_changes_md(commit_changes: List[CommitChanges], output_path: Path) -> None:
    """Write commit changes to a markdown file."""
    with open(output_path, "w", encoding="utf-8") as f:
        for cc in commit_changes:
            f.write(f"# [{cc.sha[:8]}]\n")
            f.write(f"- {cc.date}\n")
            f.write(f"- {cc.author}\n")
            f.write(f"- {cc.title}\n")
            for fc in cc.files:
                label = (
                    "[NEW]"      if fc.new_file     else
                    "[DELETED]"  if fc.deleted_file else
                    "[RENAMED]"  if fc.renamed_file else
                    "[MODIFIED]"
                )
                f.write(f"\n## {fc.new_path} {label}\n")
                f.write("```\n")
                f.write(fc.diff if fc.diff else "(no diff available)")
                f.write("\n```\n")
            f.write("\n\n")
    print(f"[INFO] Written to {output_path}")


###################################################################################################


def main() -> None:
    print("starting script5: get commits and changes from a local repository.")

    # todo: create a setup json
    REPO_PATH  = r"C:\work\vigiang\3.1\front-3.1\vigia_ng_webviewer"
    BRANCH     = "version-3.1.0"
    START_HASH = "8f1e099a"

    repo = git.Repo(REPO_PATH)

    tasks_path = Path(get_vigia_ng_path()) / "tasks"

    version = ".".join(BRANCH.replace("version-", "").split(".")[:2])
    version_path = tasks_path / version
    version_path.mkdir(parents=True, exist_ok=True)

    commits_path = version_path / "commits"
    commits_path.mkdir(parents=True, exist_ok=True)

    project_name = Path(REPO_PATH).name
    output_file = commits_path / f"{project_name}.commits.md"

    commit_changes = get_commit_changes(repo, BRANCH, START_HASH)
    write_commit_changes_md(commit_changes, output_file)

    print("\nending script5.")


if __name__ == "__main__":
    main()

