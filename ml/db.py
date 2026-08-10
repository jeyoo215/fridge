import os
import re

PROPERTIES_PATH = os.path.join(
    os.path.dirname(__file__), "..", "backend", "src", "main", "resources", "application.properties"
)


def load_db_config():
    config = {}
    with open(PROPERTIES_PATH, encoding="utf-8") as f:
        for line in f:
            line = line.strip()
            if not line or line.startswith("#") or "=" not in line:
                continue
            key, value = line.split("=", 1)
            config[key.strip()] = value.strip()

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