from datetime import datetime
from typing import Optional

from sqlmodel import Field, SQLModel

from timeutil import utcnow


class Friendship(SQLModel, table=True):
    """Lien bidirectionnel normalisé : user_low_id < user_high_id."""

    id: Optional[int] = Field(default=None, primary_key=True)
    user_low_id: int = Field(index=True, foreign_key="user.id")
    user_high_id: int = Field(index=True, foreign_key="user.id")
    created_at: datetime = Field(default_factory=utcnow)


class AddFriendRequest(SQLModel):
    public_id: str


class FriendPublic(SQLModel):
    user_id: int
    public_id: str
    display_name: str
    avatar_color: int
    dispo: bool = False
