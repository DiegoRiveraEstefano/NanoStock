from sqlmodel import Field, SQLModel
from pydantic import EmailStr
from src.shared.models.datetime_mixin import DatetimeMixin
from src.shared.models.soft_delete_mixin import SoftDeleteMixin
from src.shared.models.ulid_mixin import UlidMixin
from src.shared.models.version_mixin import VersionLockMixin


class User(UlidMixin, SoftDeleteMixin, DatetimeMixin, VersionLockMixin, table=True):

    __tablename__ = "user"

    username: str = Field(regex=r'^[a-zA-Z0-9_-]+$', max_length=100, unique=True, index=True)
    password: str = Field(max_length=256)
    email: EmailStr = Field(max_length=128)

