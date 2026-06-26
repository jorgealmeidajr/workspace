import urllib3
from dotenv import load_dotenv

from shared.environment import get_laboratories_vigia_ng
from shared import connect_gitlab


def get_active_laboratories() -> list[dict]:
    """Return only the laboratories flagged as active."""
    laboratories = get_laboratories_vigia_ng()
    return [lab for lab in laboratories if lab.get("active")]


def main() -> None:
    print("Starting to update LABORATORIES...")

    CURRENT_BRANCH = "version-3.1.0"
    PREVIOUS_BRANCHES = ["version-3.0.0"]

    load_dotenv()
    urllib3.disable_warnings(urllib3.exceptions.InsecureRequestWarning)

    gl = connect_gitlab()

    laboratories = get_active_laboratories()

    print("\nEnding script.")


if __name__ == "__main__":
    main()

