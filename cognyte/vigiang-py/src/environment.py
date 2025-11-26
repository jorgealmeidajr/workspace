import json
from pathlib import Path

WORK_PATH_STR = r"C:\work"
COGNYTE_PATH_STR = WORK_PATH_STR + r"\COGNYTE1"
VIGIANG_PATH_STR = COGNYTE_PATH_STR + r"\VIGIANG"

def get_vigia_ng_path():
    return VIGIANG_PATH_STR

def get_laboratories_vigia_ng():
    input_path = Path(get_vigia_ng_path()) / "laboratories.json"
    with open(input_path, 'r', encoding='utf-8') as f:
        data = json.load(f)
    return data
