"""
Dispo — API Backend
===================
Lancer : uvicorn main:app --reload --host 0.0.0.0 --port 8000
Doc     : http://localhost:8000/docs

Architecture inspirée de Vif : modules domaine (router + service + models).
"""
from contextlib import asynccontextmanager

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from config import CORS_ORIGINS, DEMO_MODE, UPLOAD_DIR
from db import create_tables

# Import modèles pour SQLModel.metadata
import auth.models  # noqa: F401
import friends.models  # noqa: F401
import groups.models  # noqa: F401
import availability.models  # noqa: F401
import chat.models  # noqa: F401

from auth.router import router as auth_router
from friends.router import router as friends_router
from groups.router import router as groups_router
from availability.router import router as availability_router
from chat.router import router as chat_router


@asynccontextmanager
async def lifespan(app: FastAPI):
    UPLOAD_DIR.mkdir(parents=True, exist_ok=True)
    create_tables()
    if DEMO_MODE:
        from seed_dev import seed

        seed()
        print("🎬 Mode démo actif — comptes LEA001 / MAX002 / SAM003 (mdp: demo)")
    print("✅ Dispo API démarrée — http://localhost:8000/docs")
    yield


app = FastAPI(
    title="Dispo API",
    version="0.1.0",
    lifespan=lifespan,
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=CORS_ORIGINS if CORS_ORIGINS != ["*"] else ["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(auth_router)
app.include_router(friends_router)
app.include_router(groups_router)
app.include_router(availability_router)
app.include_router(chat_router)


@app.get("/")
def root():
    return {"app": "dispo", "docs": "/docs", "health": "/health"}


@app.get("/health")
def health():
    return {"status": "ok"}


@app.get("/config")
def config():
    return {"demo_mode": DEMO_MODE}
