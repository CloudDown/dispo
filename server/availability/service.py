from datetime import datetime, timedelta

from sqlmodel import Session, select

from availability.models import Availability
from timeutil import utcnow


def end_of_local_day_utc_approx() -> datetime:
    """Fin de journée approximative : + jusqu'à minuit UTC+local offset via now+reste.
    Pour le MVP : expire dans les heures restantes jusqu'à 24h (max fin de journée locale
    gérée côté client ; serveur borne à 24h).
    """
    now = utcnow()
    # Expire au prochain minuit UTC (simple et prévisible pour le MVP)
    next_midnight = (now + timedelta(days=1)).replace(
        hour=0, minute=0, second=0, microsecond=0
    )
    return next_midnight


def set_dispo(session: Session, user_id: int, active: bool, group_id: int | None = None) -> Availability:
    # Désactive les dispos actives existantes pour cet user (+ groupe si fourni)
    q = select(Availability).where(
        Availability.user_id == user_id,
        Availability.active == True,  # noqa: E712
    )
    if group_id is not None:
        q = q.where(Availability.group_id == group_id)
    for row in session.exec(q).all():
        row.active = False
        session.add(row)

    row = Availability(
        user_id=user_id,
        group_id=group_id,
        active=active,
        expires_at=end_of_local_day_utc_approx() if active else utcnow(),
    )
    session.add(row)
    session.commit()
    session.refresh(row)
    return row


def count_active_in_group(session: Session, group_id: int) -> int:
    now = utcnow()
    rows = session.exec(
        select(Availability).where(
            Availability.group_id == group_id,
            Availability.active == True,  # noqa: E712
            Availability.expires_at > now,
        )
    ).all()
    # Un user = une dispo
    return len({r.user_id for r in rows})
