import write_mrs
import write_tags
import write_jiras


def main() -> None:
    print("Starting all writers...")

    print(f"{'─' * 120}")
    print("Step 1/3: Merge Requests")
    write_mrs.main()

    print(f"{'─' * 120}")
    print("Step 2/3: Tags")
    write_tags.main()

    print(f"{'─' * 120}")
    print("Step 3/3: Jiras")
    write_jiras.main()

    print(f"{'─' * 120}")
    print("All writers completed.")


if __name__ == "__main__":
    main()

