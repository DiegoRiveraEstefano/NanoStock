import logging
import sys
import structlog
from typing import Any

from structlog import BoundLogger

from src.core.config import settings

def setup_logging():
    # Procesadores compartidos
    processors: list[Any] = [
        structlog.contextvars.merge_contextvars,
        structlog.processors.add_log_level,
        structlog.processors.StackInfoRenderer(),
        structlog.dev.set_exc_info,
        structlog.processors.TimeStamper(fmt="iso"),
        structlog.processors.format_exc_info,
    ]

    if settings.is_prod:
        # Configuración para PRODUCCIÓN (JSON para máquinas)
        processors.append(structlog.processors.dict_tracebacks)
        processors.append(structlog.processors.JSONRenderer())
    else:
        # Configuración para DESARROLLO (Legible para humanos)
        processors.append(structlog.dev.ConsoleRenderer())


    structlog.configure(
        processors=processors,
        logger_factory=structlog.PrintLoggerFactory(),
        cache_logger_on_first_use=True,
    )

def get_logger(name: str) -> BoundLogger:
    """Retorna un logger con el nombre del módulo pre-inyectado."""
    return structlog.get_logger().bind(module=name)
