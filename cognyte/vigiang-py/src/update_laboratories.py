import urllib3
from dotenv import load_dotenv

from controller import UpdateLaboratoriesController


def main() -> None:
    print("Starting to update LABORATORIES...")

    SOURCE_BRANCH = "version-3.1.0"
    PREVIOUS_BRANCHES = ["version-3.0.0"]
    #NEXT_TAG = ""
    CURRENT_BRANCH = "version-3.2.0"

    load_dotenv()
    urllib3.disable_warnings(urllib3.exceptions.InsecureRequestWarning)

    controller = UpdateLaboratoriesController(SOURCE_BRANCH, PREVIOUS_BRANCHES)

    result = controller.execute()

    answer = input("\nDo you want to create the new tags? yes(y) or no(n)? ").strip().lower()
    if answer in {"y", "yes"}:
        print("Tags will be created...")

    print("\nEnding script.")


if __name__ == "__main__":
    main()

