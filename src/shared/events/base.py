import asyncio
from abc import ABC, abstractmethod
from datetime import datetime
from typing import Callable
from uuid import UUID, uuid4
from pydantic import BaseModel, Field

from src.shared.utc_now import utc_now

class DomainEvent(BaseModel):
    """Base para todos los eventos del sistema."""
    event_id: UUID = Field(default_factory=uuid4)
    occurred_at: datetime = Field(default_factory=utc_now)
    data: dict = Field(default_factory=dict)

class EventBus(ABC):
    """Interfaz abstracta (Puerto) para el bus de eventos."""

    def __init__(self):
        self._subscribers: dict[type[DomainEvent], set[Callable]] = {}
        self._background_tasks: set[asyncio.Task] = set()

    @abstractmethod
    async def publish(self, event: DomainEvent, **kwargs) -> None:
        """Publica un evento al bus."""
        pass

    @abstractmethod
    async def subscribe(self, event_type: type[DomainEvent], handler: Callable) -> None:
        """Se suscribe a un tipo de evento específico."""
        pass

    @abstractmethod
    async def shutdown(self) -> None:
        pass
