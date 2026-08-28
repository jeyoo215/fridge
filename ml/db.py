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
    config = _load_properties(SECRET_PROPERTIES_PATH)   # 1. secret 먼저 (비밀번호처럼 메인엔 없는 값 채움)
    config.update(_load_properties(PROPERTIES_PATH))    # 2. 메인 파일이 최종 우선권 (Spring과 동일하게)

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