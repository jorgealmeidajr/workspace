import urllib3
from dotenv import load_dotenv

from controller import UpdateLaboratoriesController


def main() -> None:
    print("Starting to update LABORATORIES...")

    SOURCE_BRANCH = "version-3.1.0"
    PREVIOUS_BRANCHES = ["version-3.0.0"]

    load_dotenv()
    urllib3.disable_warnings(urllib3.exceptions.InsecureRequestWarning)

    controller = UpdateLaboratoriesController(SOURCE_BRANCH, PREVIOUS_BRANCHES)

    controller.load_data()

    answer = input("\nDo you want to update the laboratories? yes(y) or no(n)? ").strip().lower()
    if answer in {"y", "yes"}:
        controller.execute()

    print("\nEnding script.")


if __name__ == "__main__":
    main()

