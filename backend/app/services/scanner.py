"""
Smart Scanner Service — OCR + LaTeX + LLM Analysis Pipeline
Tesseract → MathPix → GPT-4o Vision → Learning Roadmap
"""

import base64
import io
import json
import uuid
import boto3
import requests
import pytesseract
from PIL import Image, ImageEnhance
from openai import AsyncOpenAI

from app.core.config import settings

client = AsyncOpenAI(api_key=settings.OPENAI_API_KEY)
s3 = boto3.client(
    "s3",
    aws_access_key_id=settings.AWS_ACCESS_KEY_ID,
    aws_secret_access_key=settings.AWS_SECRET_ACCESS_KEY,
    region_name=settings.AWS_REGION,
)


class SmartScannerService:

    # ------------------------------------------------------------------
    # Main pipeline
    # ------------------------------------------------------------------
    async def process(self, image_bytes: bytes, user_id: str) -> dict:
        """Full OCR → LaTeX → Analysis → Roadmap pipeline."""

        # Step 1: Upload to S3 (30-day TTL enforced via lifecycle policy)
        s3_key = f"scans/{user_id}/{uuid.uuid4()}.jpg"
        self._upload_to_s3(image_bytes, s3_key)

        # Step 2: Preprocess image
        img = self._preprocess(image_bytes)

        # Step 3: Tesseract — general text
        raw_text = pytesseract.image_to_string(img, lang="eng+uzb")

        # Step 4: MathPix — mathematical expressions → LaTeX
        latex = await self._mathpix_ocr(image_bytes)

        # Step 5: GPT-4o Vision — semantic analysis
        analysis = await self._llm_analyze(image_bytes, raw_text, latex)

        # Step 6: Generate Learning Roadmap
        roadmap = self._build_roadmap(analysis)

        return {
            "raw_text": raw_text.strip(),
            "latex": latex,
            "analysis": analysis,
            "roadmap": roadmap,
            "scan_ref": s3_key,
        }

    # ------------------------------------------------------------------
    # Helpers
    # ------------------------------------------------------------------
    def _preprocess(self, image_bytes: bytes) -> Image.Image:
        """Enhance image for better OCR accuracy."""
        img = Image.open(io.BytesIO(image_bytes)).convert("L")  # Grayscale
        img = ImageEnhance.Contrast(img).enhance(2.0)
        img = ImageEnhance.Sharpness(img).enhance(2.0)
        return img

    async def _mathpix_ocr(self, image_bytes: bytes) -> str:
        """Extract LaTeX from mathematical expressions via MathPix."""
        try:
            b64 = base64.b64encode(image_bytes).decode()
            response = requests.post(
                "https://api.mathpix.com/v3/text",
                json={
                    "src": f"data:image/jpeg;base64,{b64}",
                    "formats": ["latex_styled"],
                    "data_options": {"include_latex": True},
                },
                headers={
                    "app_id": settings.MATHPIX_APP_ID,
                    "app_key": settings.MATHPIX_APP_KEY,
                },
                timeout=10,
            )
            return response.json().get("latex_styled", "")
        except Exception:
            return ""

    async def _llm_analyze(self, image_bytes: bytes, text: str, latex: str) -> dict:
        """GPT-4o Vision: identify subject, concepts, and errors."""
        b64 = base64.b64encode(image_bytes).decode()
        prompt = f"""
Analyze this student homework image.

Extracted text: {text[:500]}
Extracted LaTeX: {latex[:300]}

Return a JSON object ONLY:
{{
  "subject": "...",
  "grade_estimate": 7,
  "concepts": ["...", "..."],
  "errors": [
    {{"type": "conceptual|procedural|factual", "description": "...", "location": "line N"}}
  ],
  "difficulty_b": 0.5,
  "overall_assessment": "..."
}}
"""
        response = await client.chat.completions.create(
            model=settings.OPENAI_MODEL,
            messages=[{
                "role": "user",
                "content": [
                    {"type": "text", "text": prompt},
                    {"type": "image_url", "image_url": {"url": f"data:image/jpeg;base64,{b64}"}},
                ],
            }],
            response_format={"type": "json_object"},
            max_tokens=512,
        )
        try:
            return json.loads(response.choices[0].message.content)
        except Exception:
            return {}

    def _build_roadmap(self, analysis: dict) -> list:
        """Convert error analysis into ordered learning roadmap steps."""
        roadmap = []
        errors = analysis.get("errors", [])
        for i, err in enumerate(errors):
            roadmap.append({
                "step": i + 1,
                "topic": err.get("description", ""),
                "type": "Quiz" if i == 0 else "AI_Chat" if i % 2 == 0 else "Tutorial",
                "gap_type": err.get("type", "conceptual"),
                "status": "todo",
            })
        return roadmap

    def _upload_to_s3(self, image_bytes: bytes, key: str):
        try:
            s3.put_object(
                Bucket=settings.AWS_S3_BUCKET,
                Key=key,
                Body=image_bytes,
                ContentType="image/jpeg",
                ServerSideEncryption="AES256",  # Encryption at rest
            )
        except Exception:
            pass  # Non-blocking: scan still proceeds without S3


# ---------------------------------------------------------------------------
# Singleton
# ---------------------------------------------------------------------------
scanner_service = SmartScannerService()
