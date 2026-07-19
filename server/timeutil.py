from datetime import datetime, timezone


def utcnow() -> datetime:
    """UTC naïf (compatible SQLite / SQLModel)."""
    return datetime.now(timezone.utc).replace(tzinfo=None)
