from datetime import datetime
from typing import Optional

from sqlmodel import Field, SQLModel

from timeutil import utcnow


class Availability(SQLModel, table=True):
    id: Optional[int] = Field(default=None, primary_key=True)
    user_id: int = Field(index=True, foreign_key="user.id")
    group_id: Optional[int] = Field(default=None, index=True, foreign_key="group.id")
    active: bool = True
    expires_at: datetime
    created_at: datetime = Field(default_factory=utcnow)


class AvailabilityToggleResponse(SQLModel):
    active: bool
    expires_at: Optional[datetime] = None
    dispo_count_in_group: int = 0
