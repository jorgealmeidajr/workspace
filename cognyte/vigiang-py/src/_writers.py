import argparse

import write_mrs
import write_tags
import write_jiras


def main(initial_version: str | None = None) -> None:
    print("Starting all writers...")

    print(f"{'─' * 120}")
    print("Step 1/3: Merge Requests")
    write_mrs.main(initial_version)

    print(f"{'─' * 120}")
    print("Step 2/3: Tags")
    write_tags.main(initial_version)

    print(f"{'─' * 120}")
    print("Step 3/3: Jiras")
    write_jiras.main(initial_version)

    print(f"{'─' * 120}")
    print("All writers completed.")


# usage example: python src/_writers.py 2.3
if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Run all writers per branch.")
    parser.add_argument(
        "version",
        nargs="?",
        default=None,
        help="Optional initial version (e.g. '2.3'). If omitted, all current branches are processed.",
    )
    args = parser.parse_args()
    main(args.version)

