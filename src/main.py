import uuid
from contextlib import asynccontextmanager

from fastapi import FastAPI, Request

from src.api.v1.router import api_v1_router
from src.core.database import create_db_tables, drop_db_tables
from src.core.logging import setup_logging, get_logger
import structlog
from src.shared.events.in_memory_bus import event_bus

setup_logging()
logger = get_logger("main")


@asynccontextmanager
async def lifespan(app: FastAPI):
    await create_db_tables()
    yield
    await drop_db_tables()
    await event_bus.shutdown(wait=True)


app = FastAPI(title="NanoStock", lifespan=lifespan, routes=[api_v1_router.routes])


@app.middleware("http")
async def logging_middleware(request: Request, call_next):
    # Generamos un ID único para toda la traza de la petición
    request_id = str(uuid.uuid4())
    structlog.contextvars.clear_contextvars()
    structlog.contextvars.bind_contextvars(request_id=request_id)

    logger.info("petición_iniciada", path=request.url.path, method=request.method)

    response = await call_next(request)

    logger.info("petición_finalizada", status_code=response.status_code)
    return response

