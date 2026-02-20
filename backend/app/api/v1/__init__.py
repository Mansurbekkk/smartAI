"""API v1 router — aggregates all endpoint modules."""
from fastapi import APIRouter

from app.api.v1.endpoints import chat, scan, quiz, users, parent

router = APIRouter()
router.include_router(users.router, prefix="/users", tags=["users"])
router.include_router(chat.router,  prefix="/chat",  tags=["chat"])
router.include_router(scan.router,  prefix="/scan",  tags=["scan"])
router.include_router(quiz.router,  prefix="/quiz",  tags=["quiz"])
router.include_router(parent.router, prefix="/parent", tags=["parent"])
