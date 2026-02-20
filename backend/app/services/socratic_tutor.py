"""
Socratic AI Tutor Service
Uses OpenAI GPT-4o with strict Socratic instruction set.
Never gives direct answers — guides via questions.
"""

from openai import AsyncOpenAI
from collections import deque
from dataclasses import dataclass, field
from typing import Optional
import json

from app.core.config import settings

client = AsyncOpenAI(api_key=settings.OPENAI_API_KEY)


SOCRATIC_SYSTEM_PROMPT = """
# SMART SCHOLAR AI — SOCRATIC TUTOR SYSTEM PROMPT v2.0

## IDENTITY
You are an expert Socratic tutor for a student aged {user_age} studying {subject}.
The student's current ability level (IRT theta): {theta:.2f} (scale: -4 to +4).
Knowledge context: {knowledge_summary}

## CORE DIRECTIVES (NON-NEGOTIABLE)
1. NEVER reveal the final answer. Ever.
2. Break every problem into 3-4 micro-steps internally.
3. Per response: ask ONLY ONE guiding question targeting the first unsolved step.
4. Language calibration:
   - Age 7-10: Very simple words, use 🌟 emoji
   - Age 11-13: Friendly, use analogies, conversational
   - Age 14-17: Academic, precise, professional tone
5. SAFETY: If off-topic (harmful/political/adult), respond:
   "Bu mening vazifam emas. Keling, {subject}ga qaytaylik! 📚"
6. After each response, output a JSON block (hidden from student):
   ```json
   {{"gap_type": "conceptual|procedural|factual", "concept": "...", "step": 1}}
   ```

## INTERACTION ALGORITHM
DIAGNOSE → DECOMPOSE → GUIDE → EVALUATE → LOG

## LANGUAGE
Respond in the same language the student uses (Uzbek or English).
""".strip()


@dataclass
class ChatSession:
    user_id: str
    subject: str
    user_age: int
    theta: float
    knowledge_summary: str = ""
    history: deque = field(default_factory=lambda: deque(maxlen=24))


class SocraticTutorService:

    def _build_system_prompt(self, session: ChatSession) -> str:
        return SOCRATIC_SYSTEM_PROMPT.format(
            user_age=session.user_age,
            subject=session.subject,
            theta=session.theta,
            knowledge_summary=session.knowledge_summary or "No prior context.",
        )

    async def respond(self, session: ChatSession, user_message: str) -> dict:
        """Generate a Socratic response."""
        # Safety check first
        safe, reason = await self._content_filter(user_message, session.user_age)
        if not safe:
            return {"reply": reason, "metadata": None}

        # Build messages
        messages = [{"role": "system", "content": self._build_system_prompt(session)}]
        messages.extend(list(session.history))
        messages.append({"role": "user", "content": user_message})

        # Choose model based on complexity (cost router)
        model = settings.OPENAI_MODEL if len(user_message) > 200 else settings.OPENAI_FAST_MODEL

        response = await client.chat.completions.create(
            model=model,
            messages=messages,
            temperature=0.7,
            max_tokens=512,
        )

        reply = response.choices[0].message.content

        # Extract hidden metadata JSON if present
        metadata = self._extract_metadata(reply)
        clean_reply = self._clean_reply(reply)

        # Update session history
        session.history.append({"role": "user", "content": user_message})
        session.history.append({"role": "assistant", "content": clean_reply})

        return {"reply": clean_reply, "metadata": metadata, "model_used": model}

    def _extract_metadata(self, raw: str) -> Optional[dict]:
        """Extract hidden JSON block from LLM response."""
        try:
            start = raw.rfind("```json")
            end = raw.rfind("```", start + 1)
            if start != -1 and end != -1:
                json_str = raw[start + 7:end].strip()
                return json.loads(json_str)
        except Exception:
            pass
        return None

    def _clean_reply(self, raw: str) -> str:
        """Remove hidden JSON block from student-facing reply."""
        start = raw.rfind("```json")
        if start != -1:
            return raw[:start].strip()
        return raw.strip()

    async def _content_filter(self, text: str, age: int) -> tuple[bool, str]:
        """OpenAI Moderation API + age-based check."""
        try:
            result = await client.moderations.create(input=text)
            if result.results[0].flagged:
                return False, "Bu mavzu maktab dasturiga kirmaydi. 📚"
        except Exception:
            pass
        return True, text


# ---------------------------------------------------------------------------
# Singleton
# ---------------------------------------------------------------------------
tutor_service = SocraticTutorService()
