"""
/api/v1/quiz — Adaptive Quiz Endpoint (IRT-Powered)
"""

from fastapi import APIRouter, HTTPException
from pydantic import BaseModel
from app.services.irt_engine import irt_engine, IRTItem

router = APIRouter()

# Demo item bank (real version: load from PostgreSQL)
DEMO_ITEMS: list[IRTItem] = [
    IRTItem("q1", "Ko'paytirish", "Matematika", difficulty=-1.0, discrimination=1.2),
    IRTItem("q2", "Bo'lish",       "Matematika", difficulty=0.0,  discrimination=1.5),
    IRTItem("q3", "Kasrlar",       "Matematika", difficulty=1.0,  discrimination=1.8),
    IRTItem("q4", "Ko'rsatkichlar","Matematika", difficulty=2.0,  discrimination=2.0),
    IRTItem("q5", "Logarifm",      "Matematika", difficulty=3.0,  discrimination=2.2),
]


class NextQuestionRequest(BaseModel):
    user_id: str
    theta: float = 0.0
    answered_ids: list[str] = []


class AnswerRequest(BaseModel):
    user_id: str
    question_id: str
    theta: float
    is_correct: bool


@router.post("/next")
def get_next_question(req: NextQuestionRequest):
    """Return the most informative next question for the student's theta."""
    remaining = [i for i in DEMO_ITEMS if i.id not in req.answered_ids]
    item = irt_engine.select_next_item(req.theta, remaining)
    if not item:
        return {"done": True, "message": "Barcha savollar tugadi!"}
    return {
        "done": False,
        "question": {
            "id": item.id,
            "concept": item.concept,
            "difficulty": item.difficulty,
        },
        "current_mastery": round(irt_engine.mastery_score(req.theta) * 100, 1),
    }


@router.post("/answer")
def submit_answer(req: AnswerRequest):
    """Update student theta based on answer."""
    item = next((i for i in DEMO_ITEMS if i.id == req.question_id), None)
    if not item:
        raise HTTPException(status_code=404, detail="Savol topilmadi.")
    result = irt_engine.update_theta(req.theta, item, req.is_correct)
    return {
        "old_theta": round(result.old_theta, 3),
        "new_theta": round(result.new_theta, 3),
        "delta": round(result.delta, 3),
        "new_mastery_pct": round(irt_engine.mastery_score(result.new_theta) * 100, 1),
    }
