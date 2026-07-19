from fastapi import APIRouter, Depends, HTTPException
from sqlmodel import Session

from auth.models import User
from auth.service import get_current_user
from availability.models import AvailabilityToggleResponse
from availability.service import count_active_in_group, set_dispo
from db import get_session
from friends.service import is_currently_dispo
from groups.service import ensure_member

router = APIRouter(prefix="/availability", tags=["availability"])


@router.post("/toggle", response_model=AvailabilityToggleResponse)
def toggle(
    group_id: int | None = None,
    session: Session = Depends(get_session),
    current_user: User = Depends(get_current_user),
):
    if group_id is not None:
        ensure_member(session, group_id, current_user.id)

    currently = is_currently_dispo(session, current_user.id)
    row = set_dispo(session, current_user.id, active=not currently, group_id=group_id)
    count = count_active_in_group(session, group_id) if group_id is not None else 0
    return AvailabilityToggleResponse(
        active=row.active,
        expires_at=row.expires_at if row.active else None,
        dispo_count_in_group=count,
    )


@router.get("/me", response_model=AvailabilityToggleResponse)
def my_status(
    group_id: int | None = None,
    session: Session = Depends(get_session),
    current_user: User = Depends(get_current_user),
):
    active = is_currently_dispo(session, current_user.id)
    count = 0
    if group_id is not None:
        ensure_member(session, group_id, current_user.id)
        count = count_active_in_group(session, group_id)
    return AvailabilityToggleResponse(
        active=active,
        expires_at=None,
        dispo_count_in_group=count,
    )
