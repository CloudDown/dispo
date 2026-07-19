from sqlmodel import Session, select

from auth.models import User
from friends.models import Friendship
from availability.models import Availability
from timeutil import utcnow


def _pair(a: int, b: int) -> tuple[int, int]:
    return (a, b) if a < b else (b, a)


def are_friends(session: Session, a: int, b: int) -> bool:
    low, high = _pair(a, b)
    return (
        session.exec(
            select(Friendship).where(
                Friendship.user_low_id == low,
                Friendship.user_high_id == high,
            )
        ).first()
        is not None
    )


def add_friendship(session: Session, a: int, b: int) -> Friendship:
    low, high = _pair(a, b)
    existing = session.exec(
        select(Friendship).where(
            Friendship.user_low_id == low,
            Friendship.user_high_id == high,
        )
    ).first()
    if existing:
        return existing
    link = Friendship(user_low_id=low, user_high_id=high)
    session.add(link)
    session.commit()
    session.refresh(link)
    return link


def remove_friendship(session: Session, a: int, b: int) -> bool:
    low, high = _pair(a, b)
    link = session.exec(
        select(Friendship).where(
            Friendship.user_low_id == low,
            Friendship.user_high_id == high,
        )
    ).first()
    if not link:
        return False
    session.delete(link)
    session.commit()
    return True


def list_friend_ids(session: Session, user_id: int) -> list[int]:
    rows = session.exec(
        select(Friendship).where(
            (Friendship.user_low_id == user_id) | (Friendship.user_high_id == user_id)
        )
    ).all()
    ids: list[int] = []
    for row in rows:
        ids.append(row.user_high_id if row.user_low_id == user_id else row.user_low_id)
    return ids


def is_currently_dispo(session: Session, user_id: int) -> bool:
    row = session.exec(
        select(Availability).where(
            Availability.user_id == user_id,
            Availability.active == True,  # noqa: E712
            Availability.expires_at > utcnow(),
        )
    ).first()
    return row is not None
