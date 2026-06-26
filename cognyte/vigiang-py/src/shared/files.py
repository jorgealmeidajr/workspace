from pathlib import Path


def write_content(output_path: Path, content: str) -> bool:
    output_path.parent.mkdir(parents=True, exist_ok=True)

    if output_path.exists():
        current_content = output_path.read_text(encoding="utf-8")
        if current_content == content:
            return False
        output_path.write_text(content, encoding="utf-8")
        print(f"  ✏️ Modified: {output_path}\n")
        return True

    output_path.write_text(content, encoding="utf-8")
    print(f"  ✅ Created: {output_path}\n")
    return True

