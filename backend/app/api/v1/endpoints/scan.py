"""
/api/v1/scan — Smart Scanner Endpoint
"""

from fastapi import APIRouter, UploadFile, File, Form, HTTPException
from app.services.scanner import scanner_service

router = APIRouter()


@router.post("/upload")
async def scan_homework(
    user_id: str = Form(...),
    file: UploadFile = File(...),
):
    """Upload a homework image for OCR and AI analysis."""
    if file.content_type not in ("image/jpeg", "image/png", "image/webp"):
        raise HTTPException(status_code=400, detail="Faqat JPEG/PNG/WEBP formatlar qabul qilinadi.")

    max_size = 10 * 1024 * 1024  # 10 MB
    image_bytes = await file.read()
    if len(image_bytes) > max_size:
        raise HTTPException(status_code=413, detail="Fayl hajmi 10MB dan oshmasligi kerak.")

    try:
        result = await scanner_service.process(image_bytes, user_id)
        return result
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
