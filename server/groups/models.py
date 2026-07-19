from datetime import datetime
from typing import Optional

from sqlmodel import Field, SQLModel

from timeutil import utcnow


class Group(SQLModel, table=True):
    id: Optional[int] = Field(default=None, primary_key=True)
    name: str = Field(max_length=64)
    invite_code: str = Field(unique=True, index=True, max_length=12)
    created_by: int = Field(foreign_key="user.id")
    created_at: datetime = Field(default_factory=utcnow)


class GroupMember(SQLModel, table=True):
    id: Optional[int] = Field(default=None, primary_key=True)
    group_id: int = Field(index=True, foreign_key="group.id")
    user_id: int = Field(index=True, foreign_key="user.id")
    joined_at: datetime = Field(default_factory=utcnow)


class CreateGroupRequest(SQLModel):
    name: str


class JoinGroupRequest(SQLModel):
    invite_code: str


class GroupMemberPublic(SQLModel):
    user_id: int
    public_id: str
    display_name: str
    avatar_color: int
    dispo: bool = False


class GroupPublic(SQLModel):
    id: int
    name: str
    invite_code: str
    members: list[GroupMemberPublic] = []
