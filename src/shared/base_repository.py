import logging
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
        self.logger: logging.Logger = logger.bind(repository=self.__class__.__name__)

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
            self.logger.debug("applying_filters", model=self.model.__name__, filters=list(filters.keys()))
            query = query.where(and_(*expressions))
        return query


    async def get_by_id(self, id: Union[int | str | UUID]) -> T | None:
        self.logger.debug("fetch_entity_by_id", model=self.model.__name__, id=id)
        return await self.session.get(self.model, id)

    async def list_all(self) -> Sequence[T]:
        self.logger.info("listing_all_entities", model=self.model.__name__)
        results = await self.session.exec(self._get_list_queryset())
        return results.all()

    async def paginate_list(self, offset: int = 0, limit: int = 100) -> Sequence[T]:
        self.logger.debug("listing_paginated_entities", model=self.model.__name__, offset=offset, limit=limit)
        results = await self.session.exec(self._get_paginated_queryset(offset, limit))
        return results.all()

    async def find_by(self, **filters):
        self.logger.info("searching_entities", model=self.model.__name__, criteria=filters)
        query = self._get_filtered_queryset(query=None,**filters)
        result = await self.session.exec(query)
        return result.all()

    async def paginated_find_by(self, offset: int = 0, limit: int = 100, **filters) -> Sequence[T]:
        self.logger.info("searching_paginated_entities", model=self.model.__name__, offset=offset, limit=limit,
                    criteria=filters)
        query = self._get_paginated_queryset(offset=offset, limit=limit)
        query = self._get_filtered_queryset(query=query, **filters)
        results = await self.session.exec(query)
        return results.all()


    async def create(self, entity: T, commit: bool = True) -> T:
        try:
            self.session.add(entity)
            if commit:
                await self.session.commit()
                await self.session.refresh(entity)
                self.logger.info("entity_created", model=self.model.__name__, commit=True)
            else:
                self.logger.debug("entity_added_to_session", model=self.model.__name__, commit=False)
            return entity
        except Exception as e:
            self.logger.error("create_entity_failed", model=self.model.__name__, error=str(e))
            await self.session.rollback()
            raise

    async def delete(self, entity: T, commit: bool = True) -> None:
        try:
            entity_id = getattr(entity, "id", "unknown")
            await self.session.delete(entity)
            if commit:
                await self.session.commit()
                self.logger.warning("entity_deleted", model=self.model.__name__, id=entity_id, commit=True)
            else:
                self.logger.debug("entity_marked_for_deletion", model=self.model.__name__, id=entity_id, commit=False)
        except Exception as e:
            self.logger.error("delete_entity_failed", model=self.model.__name__, error=str(e))
            await self.session.rollback()
            raise


    async def update(self, entity: T, commit: bool = True, **kwargs) -> T:
        try:
            entity_id = getattr(entity, "id", "unknown")
            if hasattr(entity, "version"):
                entity.version += 1 # type: ignore
                self.logger.debug("version_incremented", model=self.model.__name__, id=entity_id, new_version=entity.version)

            await self.session.merge(entity)
            if commit:
                await self.session.commit()
                await self.session.refresh(entity)
                self.logger.info("entity_updated", model=self.model.__name__, id=entity_id, commit=True)
            return entity
        except Exception as e:
            self.logger.error("update_entity_failed", model=self.model.__name__, id=entity_id, error=str(e))
            await self.session.rollback()
            raise

    async def get_or_create(self, id: Union[int | str | UUID], commit: bool = True, defaults: dict[str, Any] | None = None) -> T | None:
        rs = await self.get_by_id(id)
        if rs is None:
            self.logger.info("entity_not_found_creating_new", model=self.model.__name__, id=id)
            if defaults is None:
                defaults = {}
            new_entity = self.model(**defaults)
            # Aseguramos que el ID sea el buscado si no viene en defaults
            if not hasattr(new_entity, "id") or getattr(new_entity, "id") is None:
                 setattr(new_entity, "id", id)
            rs = await self.create(new_entity, commit)
        return rs
