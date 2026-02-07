from datetime import datetime
from datetime import UTC


def utc_now() -> datetime:
    return datetime.now(UTC)
