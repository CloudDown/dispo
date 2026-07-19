from fastapi import APIRouter, Depends, HTTPException, status
from sqlmodel import Session

from auth.models import User
from auth.service import find_user_by_public_id, get_current_user
from db import get_session
from friends.models import AddFriendRequest, FriendPublic
from friends.service import (
    add_friendship,
    is_currently_dispo,
    list_friend_ids,
    remove_friendship,
)

router = APIRouter(prefix="/friends", tags=["friends"])


@router.get("", response_model=list[FriendPublic])
def list_friends(
    session: Session = Depends(get_session),
    current_user: User = Depends(get_current_user),
):
    ids = list_friend_ids(session, current_user.id)
    out: list[FriendPublic] = []
    for uid in ids:
        user = session.get(User, uid)
        if not user or not user.actif:
            continue
        out.append(
            FriendPublic(
                user_id=user.id,
                public_id=user.public_id,
                display_name=user.display_name,
                avatar_color=user.avatar_color,
                dispo=is_currently_dispo(session, user.id),
            )
        )
    return out


@router.post("", response_model=FriendPublic, status_code=status.HTTP_201_CREATED)
def add_friend(
    req: AddFriendRequest,
    session: Session = Depends(get_session),
    current_user: User = Depends(get_current_user),
):
    other = find_user_by_public_id(session, req.public_id)
    if not other or not other.actif:
        raise HTTPException(404, "Utilisateur introuvable")
    if other.id == current_user.id:
        raise HTTPException(400, "C'est ton propre ID")
    add_friendship(session, current_user.id, other.id)
    return FriendPublic(
        user_id=other.id,
        public_id=other.public_id,
        display_name=other.display_name,
        avatar_color=other.avatar_color,
        dispo=is_currently_dispo(session, other.id),
    )


@router.delete("/{public_id}", status_code=status.HTTP_204_NO_CONTENT)
def remove_friend(
    public_id: str,
    session: Session = Depends(get_session),
    current_user: User = Depends(get_current_user),
):
    other = find_user_by_public_id(session, public_id)
    if not other:
        raise HTTPException(404, "Utilisateur introuvable")
    if not remove_friendship(session, current_user.id, other.id):
        raise HTTPException(404, "Pas dans ton crew")
