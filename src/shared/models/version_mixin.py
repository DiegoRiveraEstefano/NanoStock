from sqlmodel import Field, SQLModel,Column,Integer


class VersionLockMixin(SQLModel, table=False):
    version: int = Field(sa_column=Column(Integer), default=1)

    @classmethod
    def __declare_last__(cls):
        cls.__mapper__.version_id_col = cls.__table__.c.version
