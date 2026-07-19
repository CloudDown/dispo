from datetime import datetime
from typing import Optional

from sqlmodel import Field, SQLModel

from timeutil import utcnow


class User(SQLModel, table=True):
    id: Optional[int] = Field(default=None, primary_key=True)
    public_id: str = Field(unique=True, index=True, max_length=12)
    display_name: str = Field(max_length=64)
    hashed_password: str
    avatar_color: int = 0
    actif: bool = True
    created_at: datetime = Field(default_factory=utcnow)


class RegisterRequest(SQLModel):
    display_name: str
    password: str
    public_id: Optional[str] = None  # optionnel ; généré si absent


class LoginRequest(SQLModel):
    public_id: str
    password: str


class TokenResponse(SQLModel):
    access_token: str
    token_type: str = "bearer"
    user_id: int
    public_id: str
    display_name: str
    avatar_color: int


class UserPublic(SQLModel):
    id: int
    public_id: str
    display_name: str
    avatar_color: int
    created_at: datetime


class ProfileUpdateRequest(SQLModel):
    display_name: Optional[str] = None
    avatar_color: Optional[int] = None
