from typing import TypeVar, Generic, Type, Any, Sequence, Union
from uuid import uuid4, UUID

from sqlalchemy import GenerativeSelect
from sqlmodel import SQLModel, select, and_
from sqlmodel.ext.asyncio.session import AsyncSession

from src.core.logging import get_logger

T = TypeVar("T", bound=SQLModel)
logger = get_logger(__name__)

class BaseRepository(Generic[T]):
    def __init__(self, session: AsyncSession, model: Type[T]):
        logger.debug(f"Init repository", model=model)
        self.session = session
        self.model = model

    def _get_paginated_queryset(self, offset: int = 0, limit: int = 100, query: GenerativeSelect | None = None) -> GenerativeSelect[T]:
        if query is None:
            query = select(self.model)
        return query.offset(offset).limit(limit)

    def _get_list_queryset(self):
        return select(self.model)

    def _get_filtered_queryset(self, query: GenerativeSelect | None = None, **filters, ) -> GenerativeSelect[T]:
        if query is None:
            query = select(self.model)
        expressions = []
        for field, value in filters.items():
            if hasattr(self.model, field) and value is not None:
                expressions.append(getattr(self.model, field) == value)
        if expressions:
            query = query.where(and_(*expressions))
        return query


    async def get_by_id(self, id: Union[int | str | UUID]) -> T | None:
        return await self.session.get(self.model, id)

    async def list_all(self) -> Sequence[T]:
        results = await self.session.exec(self._get_list_queryset())
        return results.all()

    async def paginate_list(self, offset: int = 0, limit: int = 100) -> Sequence[T]:
        results = await self.session.exec(self._get_paginated_queryset(offset, limit))
        return results.all()

    async def find_by(self, **filters):
        query = self._get_filtered_queryset(query=None,**filters)
        result = await self.session.exec(query)
        return result.all()

    async def paginated_find_by(self, offset: int = 0, limit: int = 100, **filters) -> Sequence[T]:
        query = self._get_paginated_queryset(offset=offset, limit=limit)
        query = self._get_filtered_queryset(query=query, **filters)
        results = await self.session.exec(query)
        return results.all()


    async def create(self, entity: T, commit: bool = True) -> T:
        self.session.add(entity)
        if commit:
            await self.session.commit()
            await self.session.refresh(entity)

        return entity

    async def delete(self, entity: T, commit: bool = True) -> None:
        await self.session.delete(entity)
        if commit:
            await self.session.commit()


    async def update(self, entity: T, commit: bool = True, **kwargs) -> T:
        if hasattr(entity, "version"):
            kwargs["version"] = entity.version + 1

        await self.session.merge(entity, **kwargs)
        if commit:
            await self.session.commit()
            await self.session.refresh(entity)

        return entity

    async def get_or_create(self, id: Union[int | str | UUID], commit: bool = True, defaults: dict[str, Any] | None = None) -> T | None:
        rs = await self.get_by_id(id)
        if rs is None:
            if defaults is None:
                defaults = {}
            new_entity = self.model(**defaults)
            rs = await self.create(new_entity, commit)
        return rs
