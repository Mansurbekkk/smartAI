"""
Smart Scholar AI — FastAPI Backend
Author: System Architect
"""

from fastapi import FastAPI, Request
from fastapi.middleware.cors import CORSMiddleware
from fastapi.middleware.gzip import GZipMiddleware
from fastapi.responses import JSONResponse
import sentry_sdk
import time

from app.core.config import settings
from app.api.v1 import router as api_v1_router

# ---------------------------------------------------------------------------
# Sentry (Error Tracking)
# ---------------------------------------------------------------------------
if settings.SENTRY_DSN:
    sentry_sdk.init(dsn=settings.SENTRY_DSN, traces_sample_rate=0.1)

# ---------------------------------------------------------------------------
# App Instance
# ---------------------------------------------------------------------------
app = FastAPI(
    title="Smart Scholar AI",
    description="Adaptive AI-powered Educational Backend",
    version="1.0.0",
    docs_url="/docs" if settings.DEBUG else None,
    redoc_url=None,
)

# ---------------------------------------------------------------------------
# Middleware
# ---------------------------------------------------------------------------
app.add_middleware(GZipMiddleware, minimum_size=1000)
app.add_middleware(
    CORSMiddleware,
    allow_origins=settings.CORS_ORIGINS,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

@app.middleware("http")
async def add_process_time_header(request: Request, call_next):
    start = time.perf_counter()
    response = await call_next(request)
    response.headers["X-Process-Time"] = f"{time.perf_counter() - start:.4f}s"
    return response

# ---------------------------------------------------------------------------
# Routers
# ---------------------------------------------------------------------------
app.include_router(api_v1_router, prefix="/api/v1")

# ---------------------------------------------------------------------------
# Healthcheck
# ---------------------------------------------------------------------------
@app.get("/health", tags=["infra"])
async def health():
    return {"status": "ok", "service": "smart-scholar-ai"}
