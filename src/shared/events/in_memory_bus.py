import asyncio
from typing import Callable

from src.core.logging import get_logger
from src.shared.events.base import EventBus, DomainEvent

logger = get_logger(__name__)

class InMemoryEventBus(EventBus):

    async def publish(self, event: DomainEvent, wait: bool = False, **kwargs) -> None:

        event_type = type(event)
        if event_type not in self._subscribers:
            logger.debug("dispatch event with no _subscribers", event_type=event_type)
            return

        subscribers = self._subscribers[event_type]
        if wait:
            logger.debug("dispatch event with await", event_type=event_type)
            await asyncio.gather(
                *(self._safe_wrapper(cb, **kwargs) for cb in subscribers),
                return_exceptions=True
            )

        else:
            logger.debug("dispatch event with not await", event_type=event_type)
            for cb in subscribers:
                task = asyncio.create_task(self._safe_wrapper(cb, **kwargs))
                self._background_tasks.add(task)
                task.add_done_callback(self._background_tasks.discard)

        logger.debug("end event dispatch", event_type=event_type)


    async def subscribe(self, event_type: type[DomainEvent], handler: Callable) -> None:
        logger.info(f"subscribe new handler:", event_type=event_type, handler=handler)
        if event_type not in self._subscribers:
            logger.debug("new event registered", event_type=event_type)
            self._subscribers[event_type] = set()
        if handler in self._subscribers[event_type]:
            logger.warning("duplicate handler attempt to register", event_type=event_type, handler=handler)
            return

        self._subscribers[event_type].add(handler)
        logger.info("added new handler", event_type=event_type, handler=handler)

    async def shutdown(self, wait: bool = False) -> None:
        """Espera a que todas las tareas en segundo plano terminen antes de cerrar."""
        logger.info("init shutdown")

        if not self._background_tasks:
            logger.info("shutdown event bus with no background tasks")
            logger.info("event bus closed")
            return

        if wait:
            try:
                logger.info("shutdown event bus with wait", tasks_number=len(
                    self._background_tasks
                ))
                await asyncio.gather(*self._background_tasks, return_exceptions=True)
            except Exception as e:
                logger.exception("shutdown event await, with exception", exc_info=e)
            finally:
                for task in self._background_tasks:
                    task.cancel()
        else:
            try:
                logger.info("shutdown event bus, with cancel tasks", tasks_number=len(
                    self._background_tasks
                ))
                for task in self._background_tasks:
                    logger.debug(f"cancel task", task=task)
                    task.cancel()
                    logger.debug("task canceled", task=task)
            except Exception as e:
                logger.exception("shutdown event no await, with exception", exc_info=e)

        logger.info("event bus closed")

    async def _safe_wrapper(self, callback: Callable, **kwargs):
        """Envuelve la ejecución para aislar errores."""
        try:
            await callback(**kwargs)
        except Exception as e:
            logger.exception("dispatch event with exception", exc_info=e)
