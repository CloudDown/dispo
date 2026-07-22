from fastapi import APIRouter, Depends, HTTPException, status
from sqlmodel import Session, select

from auth.models import User
from auth.service import get_current_user
from db import get_session
from groups.models import (
    AddMemberRequest,
    CreateGroupRequest,
    Group,
    GroupMember,
    JoinGroupRequest,
    GroupPublic,
)
from groups.service import add_friend_to_group, create_group, ensure_member, leave_group, to_public

router = APIRouter(prefix="/groups", tags=["groups"])


@router.get("", response_model=list[GroupPublic])
def my_groups(
    session: Session = Depends(get_session),
    current_user: User = Depends(get_current_user),
):
    memberships = session.exec(
        select(GroupMember).where(GroupMember.user_id == current_user.id)
    ).all()
    out: list[GroupPublic] = []
    for m in memberships:
        group = session.get(Group, m.group_id)
        if group:
            out.append(to_public(session, group))
    return out


@router.post("", response_model=GroupPublic, status_code=status.HTTP_201_CREATED)
def create(
    req: CreateGroupRequest,
    session: Session = Depends(get_session),
    current_user: User = Depends(get_current_user),
):
    group = create_group(session, current_user, req.name)
    return to_public(session, group)


@router.post("/join", response_model=GroupPublic)
def join(
    req: JoinGroupRequest,
    session: Session = Depends(get_session),
    current_user: User = Depends(get_current_user),
):
    code = req.invite_code.strip().upper()
    group = session.exec(select(Group).where(Group.invite_code == code)).first()
    if not group:
        raise HTTPException(404, "Code invalide")
    existing = session.exec(
        select(GroupMember).where(
            GroupMember.group_id == group.id,
            GroupMember.user_id == current_user.id,
        )
    ).first()
    if not existing:
        session.add(GroupMember(group_id=group.id, user_id=current_user.id))
        session.commit()
    return to_public(session, group)


@router.get("/{group_id}", response_model=GroupPublic)
def get_group(
    group_id: int,
    session: Session = Depends(get_session),
    current_user: User = Depends(get_current_user),
):
    ensure_member(session, group_id, current_user.id)
    group = session.get(Group, group_id)
    if not group:
        raise HTTPException(404, "Groupe introuvable")
    return to_public(session, group)


@router.post("/{group_id}/members", response_model=GroupPublic)
def add_member(
    group_id: int,
    req: AddMemberRequest,
    session: Session = Depends(get_session),
    current_user: User = Depends(get_current_user),
):
    group = add_friend_to_group(session, group_id, current_user, req.public_id)
    return to_public(session, group)


@router.delete("/{group_id}/members/me", status_code=status.HTTP_204_NO_CONTENT)
def leave(
    group_id: int,
    session: Session = Depends(get_session),
    current_user: User = Depends(get_current_user),
):
    leave_group(session, group_id, current_user)
    return None
