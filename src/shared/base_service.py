import logging
from typing import Generic, TypeVar, Any, Sequence

from sqlmodel import SQLModel

from src.shared.base_repository import BaseRepository
from src.shared.events.base import EventBus
from src.shared.schemas.pagination import PaginationParams, PaginatedResponse
from src.core.logging import get_logger
from pydantic import BaseModel, TypeAdapter, ValidationError

# T: Schema base de validacion, R: Repositorio, E: entity orm
T = TypeVar("T", bound=BaseModel)
E = TypeVar("E", bound=SQLModel)
R = TypeVar("R", bound=BaseRepository)

logger = get_logger(__name__)


class BaseService(Generic[T, E, R]):
    def __init__(self, repository: R, event_bus: EventBus):
        self.repository = repository
        self.event_bus = event_bus
        self._schema: type[T] = T
        self._list_adapter = TypeAdapter(list[T])
        self._dict_adapter = TypeAdapter(dict[str, T])
        self._pagination_type = type
        self.logger: logging.Logger = logger.bind(service=self.__class__.__name__)

    def _validate_result(self, entity: E) -> T | None:
        try:
            return self._schema.model_validate(entity)
        except ValidationError as e:
            self.logger.error(e)
            return None

    def _validate_list(self, entity_list: Sequence[T]) -> list[T] | None:
        try:
            return self._list_adapter.validate_python(entity_list)
        except ValidationError as e:
            self.logger.error(e)
            return None

    def _validate_dict(self, entity_dict: dict[str, T]) -> dict[str, T] | None:
        try:
            return self._dict_adapter.validate_python(entity_dict)
        except ValidationError as e:
            self.logger.error(e)
            return None

    async def get_by_id(self, id: Any) -> T | None:
        self.logger.debug("service_get_by_id", id=id)
        result = await self.repository.get_by_id(id)
        return self._validate_result(result)

    async def get_paginated(self, params: PaginationParams, **filters) -> PaginatedResponse[T]:
        """Obtiene una lista paginada y estructurada para el frontend."""
        self.logger.info("service_get_paginated", offset=params.offset, limit=params.limit, filters=filters)

        # En una implementación real, podrías necesitar un método 'count' en el repo
        # Para este ejemplo, obtenemos los items
        items = await self.repository.paginated_find_by(
            offset=params.offset,
            limit=params.limit,
            **filters
        )

        # Nota: Aquí se podría llamar a un método count() del repositorio para obtener el total real
        return PaginatedResponse(
            page=params.offset,
            previous_page=params.offset - 1 if params.offset > 0 else 0,
            next_page=params.offset + 1,
            limit=params.limit,
            total=-1,
            items=self._validate_list(items),
        )

    async def delete_entity(self, id: Any) -> bool:
        entity = await self.repository.get_by_id(id)
        if not entity:
            self.logger.warning("delete_failed_not_found", id=id)
            return False

        await self.repository.delete(entity)
        self.logger.info("entity_deleted_successfully", id=id)
        return True
