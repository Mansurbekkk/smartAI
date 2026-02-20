"""
/api/v1/chat — Socratic AI Tutor Endpoint
"""

from fastapi import APIRouter, HTTPException, Depends
from pydantic import BaseModel
from app.services.socratic_tutor import tutor_service, ChatSession

router = APIRouter()

# In-memory session store (replace with Redis in production)
_sessions: dict[str, ChatSession] = {}


class ChatRequest(BaseModel):
    user_id: str
    message: str
    subject: str = "Matematika"
    user_age: int = 14
    theta: float = 0.0
    knowledge_summary: str = ""


class ChatResponse(BaseModel):
    reply: str
    metadata: dict | None
    model_used: str


@router.post("/message", response_model=ChatResponse)
async def send_message(req: ChatRequest):
    """Send a message to the Socratic AI tutor."""
    # Get or create session
    session = _sessions.get(req.user_id)
    if not session:
        session = ChatSession(
            user_id=req.user_id,
            subject=req.subject,
            user_age=req.user_age,
            theta=req.theta,
            knowledge_summary=req.knowledge_summary,
        )
        _sessions[req.user_id] = session

    try:
        result = await tutor_service.respond(session, req.message)
        return ChatResponse(**result)
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@router.delete("/session/{user_id}")
async def clear_session(user_id: str):
    """Clear a user's chat session."""
    _sessions.pop(user_id, None)
    return {"status": "cleared"}
