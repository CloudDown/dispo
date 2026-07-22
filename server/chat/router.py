from fastapi import APIRouter, Depends, HTTPException
from sqlmodel import Session, select

from auth.models import User
from auth.service import get_current_user
from availability.service import count_active_in_group
from chat.models import Message, MessagePublic, SendMessageRequest
from db import get_session
from groups.service import ensure_member

router = APIRouter(prefix="/chat", tags=["chat"])


def _to_public(session: Session, msg: Message) -> MessagePublic:
    author = session.get(User, msg.author_id)
    return MessagePublic(
        id=msg.id,
        group_id=msg.group_id,
        author_id=msg.author_id,
        author_public_id=author.public_id if author else "????",
        author_name=author.display_name if author else "?",
        text=msg.text,
        lat=msg.lat,
        lon=msg.lon,
        created_at=msg.created_at,
    )


@router.get("/{group_id}/messages", response_model=list[MessagePublic])
def list_messages(
    group_id: int,
    session: Session = Depends(get_session),
    current_user: User = Depends(get_current_user),
):
    ensure_member(session, group_id, current_user.id)
    rows = session.exec(
        select(Message)
        .where(Message.group_id == group_id)
        .order_by(Message.created_at)
    ).all()
    return [_to_public(session, m) for m in rows]


@router.post("/{group_id}/messages", response_model=MessagePublic, status_code=201)
def send_message(
    group_id: int,
    req: SendMessageRequest,
    session: Session = Depends(get_session),
    current_user: User = Depends(get_current_user),
):
    ensure_member(session, group_id, current_user.id)
    # Chat déverrouillé dès 1 dispo dans le groupe
    if count_active_in_group(session, group_id) < 1:
        raise HTTPException(403, "Il faut être dispo pour chatter")

    text = req.text.strip()
    if not text and req.lat is None:
        raise HTTPException(400, "Message vide")
    if len(text) > 2000:
        raise HTTPException(400, "Message trop long")

    msg = Message(
        group_id=group_id,
        author_id=current_user.id,
        text=text or "📍 Lieu partagé",
        lat=req.lat,
        lon=req.lon,
    )
    session.add(msg)
    session.commit()
    session.refresh(msg)
    return _to_public(session, msg)
