# Smart Scholar AI 🎓

> AI-powered adaptive learning platform for K-12 students

## 📱 Android App (Flutter/Kotlin)
- Adaptive Kids/Teens UI modes
- Socratic AI Chat
- Smart Homework Scanner (OCR)
- Learning Roadmap
- Parent Portal Dashboard

## 🐍 FastAPI Backend
- IRT-powered Adaptive Quiz Engine
- Socratic GPT-4o Tutor Service
- OCR + MathPix + Vision Analysis
- PostgreSQL + Redis + S3

## 🚀 Quick Start

### Backend
```bash
cd backend
pip install -r requirements.txt
cp ../.env.example .env   # fill in your keys
uvicorn app.main:app --reload
```

### API Docs
`http://localhost:8000/docs`

## 🌐 Deployment
- **Backend**: Vercel (Python Serverless)
- **Database**: PostgreSQL (Supabase / Railway)
- **Storage**: AWS S3

## 📊 Tech Stack
| Layer | Technology |
|---|---|
| Mobile | Kotlin + Jetpack Compose |
| Backend | Python + FastAPI |
| AI | OpenAI GPT-4o |
| DB | PostgreSQL + Redis |
| Deploy | Vercel + GitHub Actions |
