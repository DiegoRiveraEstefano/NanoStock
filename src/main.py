import uuid
from contextlib import asynccontextmanager

from fastapi import FastAPI, Request, APIRouter
from starlette.responses import FileResponse
from starlette.staticfiles import StaticFiles

from src.api.v1.router import api_v1_router
from src.core.consts import TEMPLATE_PATH, STATIC_PATH
from src.core.database import create_db_tables, drop_db_tables
from src.core.logging import setup_logging, get_logger
import structlog
from src.shared.events.in_memory_bus import event_bus

setup_logging()
logger = get_logger("main")


@asynccontextmanager
async def lifespan(app: FastAPI):
    logger.info("init app")
    logger.debug("create db tables")
    await create_db_tables()
    logger.debug("tables created")
    yield
    logger.info("shutdown app")
    logger.debug("close db tables")
    await drop_db_tables()
    logger.debug("tables dropped")
    logger.debug("closing event bus")
    await event_bus.shutdown(wait=True)
    logger.debug("event bus closed")

app = FastAPI(title="NanoStock", lifespan=lifespan)
app.include_router(api_v1_router, prefix="/api")

app.mount("/static", StaticFiles(directory=STATIC_PATH), name="static")

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

@app.get("/")
async def serve_spa():
    return FileResponse(TEMPLATE_PATH / "index.html")

@app.get("/login")
async def serve_spa():
    return FileResponse(TEMPLATE_PATH / "login.html")

@app.get("/dashboard/{full_path:path}")
async def serve_spa(full_path: str):
    return FileResponse(TEMPLATE_PATH / "dashboard.html")

