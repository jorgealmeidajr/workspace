import pytest

from write_jiras import extract_jira_keys


@pytest.mark.parametrize(
    "text, expected",
    [
        # Underscore-prefixed key inside a branch name (leading "77_fix_").
        ("Merge branch '77_fix_vrp_2242_unifique'", ["VRP-2242"]),
        # Underscore variant in a commit message.
        ("#77 fix: fix vrp_2242_unifique", ["VRP-2242"]),
        # Canonical uppercase-hyphen form.
        ("VRP-2242 done", ["VRP-2242"]),
        # Lowercase hyphen form is normalized.
        ("vrp-2242 lower", ["VRP-2242"]),
        # NISR prefix, both underscore and hyphen, deduped and ordered.
        ("NISR_15 and nisr-9", ["NISR-15", "NISR-9"]),
        # Single-space separator, normalized to hyphen form.
        ("NISR 15", ["NISR-15"]),
        # Multiple spaces collapse to a single hyphen.
        ("NISR  15", ["NISR-15"]),
        # Lowercase with a space is normalized/uppercased.
        ("nisr 15", ["NISR-15"]),
        # Numeric-only fragments must not match.
        ("77_fix should not match", []),
        ("fix_2242 should not match", []),
        # Prefix glued to preceding letters must not match.
        ("servrp_2242 should not match", []),
        # Empty / falsy input.
        ("", []),
    ],
)
def test_extract_jira_keys(text, expected):
    assert extract_jira_keys(text) == expected


def test_extract_jira_keys_deduplicates_and_preserves_order():
    text = "vrp_2242 then VRP-2242 again, plus nisr-9 and NISR_9"
    assert extract_jira_keys(text) == ["VRP-2242", "NISR-9"]

