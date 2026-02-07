import uuid
from ulid import ULID
from sqlmodel import Field, SQLModel
from datetime import datetime
from datetime import UTC

def create_uuid_from_ulid():
    return ULID.from_datetime(datetime.now(UTC)).to_uuid4()

class UlidMixin(SQLModel):

    id: uuid.UUID = Field(default_factory=create_uuid_from_ulid, primary_key=True)


