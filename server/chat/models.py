from datetime import datetime
from typing import Optional

from sqlmodel import Field, SQLModel

from timeutil import utcnow


class Message(SQLModel, table=True):
    id: Optional[int] = Field(default=None, primary_key=True)
    group_id: int = Field(index=True, foreign_key="group.id")
    author_id: int = Field(index=True, foreign_key="user.id")
    text: str = Field(max_length=2000)
    lat: Optional[float] = None
    lon: Optional[float] = None
    created_at: datetime = Field(default_factory=utcnow)


class SendMessageRequest(SQLModel):
    text: str
    lat: Optional[float] = None
    lon: Optional[float] = None


class MessagePublic(SQLModel):
    id: int
    group_id: int
    author_id: int
    author_public_id: str
    author_name: str
    text: str
    lat: Optional[float] = None
    lon: Optional[float] = None
    created_at: datetime
