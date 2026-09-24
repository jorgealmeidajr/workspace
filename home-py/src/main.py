"""Playwright script to interact with a website.

Configuration is read from a `.env` file (see `.env.example`):
    TARGET_URL  - the site to open
    HEADLESS    - "true"/"false", run the browser without a visible window
    LINKEDIN_EMAIL    - optional login user
    LINKEDIN_PASSWORD - optional login password

Run it with:
    python src/main.py
"""

import os

from dotenv import load_dotenv
from playwright.sync_api import Page, sync_playwright


def _env_bool(name: str, default: bool = True) -> bool:
    value = os.getenv(name)
    if value is None:
        return default
    return value.strip().lower() in {"1", "true", "yes", "on"}


def interact(page: Page) -> None:
    """Interact with the target site.

    Replace the body of this function with your own steps. The example below
    navigates to the configured URL and prints the page title.
    """
    target_url = os.getenv("TARGET_URL", "https://example.com")

    page.set_default_timeout(30_000)
    page.goto(target_url)
    page.wait_for_load_state("load")

    print(f"Opened: {page.url}")
    print(f"Title:  {page.title()}")

    email_input = page.locator("input[type='email']:visible").first
    email_input.wait_for(state="visible")
    email_id = email_input.get_attribute("id")

    password_input = page.locator("input[type='password']:visible").first
    password_input.wait_for(state="visible")
    password_id = password_input.get_attribute("id")

    print(f"Email input id:    {email_id}")
    print(f"Password input id: {password_id}")

    page.fill(f"[id='{email_id}']", os.getenv("LINKEDIN_EMAIL", ""))
    page.fill(f"[id='{password_id}']", os.getenv("LINKEDIN_PASSWORD", ""))

    buttons = page.locator("button[type='button']:visible")
    print(f"Found {buttons.count()} buttons:")

    entrar_button = None
    for i in range(buttons.count()):
        button = buttons.nth(i)
        button_text = button.inner_text()
        print(f"  {i}: {button_text}")
        if button_text.strip() == "Entrar":
            entrar_button = button

    if entrar_button is not None:
        entrar_button.click()

    page.wait_for_timeout(2000)


def main() -> None:
    load_dotenv()
    headless = _env_bool("HEADLESS", default=True)

    with sync_playwright() as p:
        browser = p.chromium.launch(headless=headless, slow_mo=500)
        context = browser.new_context()
        page = context.new_page()
        try:
            interact(page)
        finally:
            context.close()
            browser.close()


if __name__ == "__main__":
    main()
