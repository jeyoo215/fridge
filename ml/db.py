import os
import re

BASE_DIR = os.path.join(os.path.dirname(__file__), "..", "backend", "src", "main", "resources")
PROPERTIES_PATH = os.path.join(BASE_DIR, "application.properties")
SECRET_PROPERTIES_PATH = os.path.join(BASE_DIR, "application-secret.properties")


def _load_properties(path):
    props = {}
    if not os.path.exists(path):
        return props
    with open(path, encoding="utf-8") as f:
        for line in f:
            line = line.strip()
            if not line or line.startswith("#") or "=" not in line:
                continue
            key, value = line.split("=", 1)
            props[key.strip()] = value.strip()
    return props


def load_db_config():
    config = _load_properties(PROPERTIES_PATH)
    config.update(_load_properties(SECRET_PROPERTIES_PATH))  # secret 파일 값으로 덮어씀 (Spring의 config.import와 동일한 우선순위)

    url = config.get("spring.datasource.url", "")
    match = re.search(r"jdbc:mysql://([^:/]+):?(\d+)?/([^?]+)", url)
    host, port, database = match.group(1), match.group(2) or "3306", match.group(3)

    return {
        "host": host,
        "port": int(port),
        "database": database,
        "user": config.get("spring.datasource.username"),
        "password": config.get("spring.datasource.password"),
    }