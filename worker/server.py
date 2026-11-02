"""
FootballClips Cloud Run Worker Server
FastAPI Application Entrypoint with Structured Middleware & Microservice Routing.
"""

import asyncio
import os
import uuid
from contextlib import asynccontextmanager

from fastapi import FastAPI, Request
from fastapi.middleware.cors import CORSMiddleware
from loguru import logger
from starlette.middleware.base import BaseHTTPMiddleware

from worker.main import WorkerOrchestrator
from worker.routers import broadcast_router, health_router, tasks_router, websub_router

orchestrator = WorkerOrchestrator()
_background_worker_task: asyncio.Task | None = None


@asynccontextmanager
async def lifespan(app: FastAPI):
    global _background_worker_task
    logger.info("[CloudRun Server] Starting FootballClips Worker Service...")
    mode = os.getenv("WORKER_MODE", "continuous").lower()

    if mode == "continuous":
        logger.info("[CloudRun Server] Launching background continuous orchestrator task...")
        _background_worker_task = asyncio.create_task(orchestrator.start())
    else:
        logger.info("[CloudRun Server] Running in on-demand HTTP mode (Cloud Scheduler / Webhooks).")

    yield

    logger.info("[CloudRun Server] Shutting down Worker Service...")
    orchestrator.is_running = False
    if _background_worker_task and not _background_worker_task.done():
        _background_worker_task.cancel()
        try:
            await _background_worker_task
        except asyncio.CancelledError:
            pass


class CorrelationIdMiddleware(BaseHTTPMiddleware):
    async def dispatch(self, request: Request, call_next):
        corr_id = request.headers.get("X-Correlation-ID") or uuid.uuid4().hex[:12]
        with logger.contextualize(trace_id=corr_id):
            response = await call_next(request)
            response.headers["X-Correlation-ID"] = corr_id
            return response


app = FastAPI(
    title="FootballClips Worker Service",
    description="Enterprise asynchronous football fixture, live score, and highlight synchronization engine",
    version="2.1.0",
    lifespan=lifespan,
)

app.add_middleware(CorrelationIdMiddleware)
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(health_router)
app.include_router(broadcast_router)
app.include_router(websub_router)
app.include_router(tasks_router)
