from sqlmodel import Field, SQLModel


class SoftDeleteMixin(SQLModel, table=False):
    is_active: bool = Field(default=True)
