from sqlmodel import Field, SQLModel
from datetime import datetime
from datetime import UTC

class DatetimeMixin(SQLModel):
    created_at: datetime = Field(default_factory=lambda: datetime.now(UTC), allow_mutation=False)
    updated_at: datetime = Field(default_factory=lambda: datetime.now(UTC))
