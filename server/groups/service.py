import secrets
import string

from fastapi import HTTPException
from sqlmodel import Session, select

from auth.models import User
from friends.service import are_friends, is_currently_dispo
from groups.models import Group, GroupMember, GroupMemberPublic, GroupPublic

_CODE_ALPHABET = string.ascii_uppercase + string.digits


def _invite_code() -> str:
    return "".join(secrets.choice(_CODE_ALPHABET) for _ in range(8))


def to_public(session: Session, group: Group) -> GroupPublic:
    member_rows = session.exec(
        select(GroupMember).where(GroupMember.group_id == group.id)
    ).all()
    members: list[GroupMemberPublic] = []
    for row in member_rows:
        user = session.get(User, row.user_id)
        if not user or not user.actif:
            continue
        members.append(
            GroupMemberPublic(
                user_id=user.id,
                public_id=user.public_id,
                display_name=user.display_name,
                avatar_color=user.avatar_color,
                dispo=is_currently_dispo(session, user.id),
            )
        )
    return GroupPublic(
        id=group.id,
        name=group.name,
        invite_code=group.invite_code,
        members=members,
    )


def ensure_member(session: Session, group_id: int, user_id: int) -> GroupMember:
    row = session.exec(
        select(GroupMember).where(
            GroupMember.group_id == group_id,
            GroupMember.user_id == user_id,
        )
    ).first()
    if not row:
        raise HTTPException(403, "Tu n'es pas membre de ce groupe")
    return row


def create_group(session: Session, owner: User, name: str) -> Group:
    clean = name.strip()[:64]
    if not clean:
        raise HTTPException(400, "Nom de groupe requis")
    for _ in range(20):
        code = _invite_code()
        if not session.exec(select(Group).where(Group.invite_code == code)).first():
            break
    else:
        raise HTTPException(500, "Impossible de générer un code")

    group = Group(name=clean, invite_code=code, created_by=owner.id)
    session.add(group)
    session.commit()
    session.refresh(group)
    session.add(GroupMember(group_id=group.id, user_id=owner.id))
    session.commit()
    return group


def add_friend_to_group(
    session: Session,
    group_id: int,
    actor: User,
    friend_public_id: str,
) -> Group:
    ensure_member(session, group_id, actor.id)
    group = session.get(Group, group_id)
    if not group:
        raise HTTPException(404, "Groupe introuvable")

    handle = friend_public_id.strip().lstrip("@").lower()
    friend = session.exec(select(User).where(User.public_id == handle)).first()
    if not friend or not friend.actif:
        raise HTTPException(404, "Utilisateur introuvable")
    if friend.id == actor.id:
        raise HTTPException(400, "Tu es déjà dans le groupe")
    if not are_friends(session, actor.id, friend.id):
        raise HTTPException(400, "Vous devez être amis pour l'ajouter")

    existing = session.exec(
        select(GroupMember).where(
            GroupMember.group_id == group_id,
            GroupMember.user_id == friend.id,
        )
    ).first()
    if existing:
        return group

    session.add(GroupMember(group_id=group_id, user_id=friend.id))
    session.commit()
    session.refresh(group)
    return group


def leave_group(session: Session, group_id: int, user: User) -> None:
    row = ensure_member(session, group_id, user.id)
    session.delete(row)
    session.commit()
    remaining = session.exec(
        select(GroupMember).where(GroupMember.group_id == group_id)
    ).first()
    if not remaining:
        group = session.get(Group, group_id)
        if group:
            session.delete(group)
            session.commit()
