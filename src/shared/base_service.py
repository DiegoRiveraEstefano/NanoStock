import logging
from typing import Generic, TypeVar, Any, Sequence
from src.shared.base_repository import BaseRepository
from src.shared.events.base import EventBus
from src.shared.schemas.pagination import PaginationParams, PaginatedResponse
from src.core.logging import get_logger

# T: Modelo de DB (SQLModel), R: Repositorio
T = TypeVar("T")
R = TypeVar("R", bound=BaseRepository)

logger = get_logger(__name__)


class BaseService(Generic[T, R]):
    def __init__(self, repository: R, event_bus: EventBus):
        self.repository = repository
        self.event_bus = event_bus
        self.logger: logging.Logger = logger.bind(service=self.__class__.__name__)

    async def get_by_id(self, id: Any) -> T | None:
        self.logger.debug("service_get_by_id", id=id)
        return await self.repository.get_by_id(id)

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
            items=items
        )

    async def delete_entity(self, id: Any) -> bool:
        entity = await self.repository.get_by_id(id)
        if not entity:
            self.logger.warning("delete_failed_not_found", id=id)
            return False

        await self.repository.delete(entity)
        self.logger.info("entity_deleted_successfully", id=id)
        return True
