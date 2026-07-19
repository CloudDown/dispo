"""
Configuration Dispo — lit d'abord l'environnement, sinon défauts locaux.
Préfixe env : DISPO_* (comme VIF_* dans le projet Vif).
"""
import os
from pathlib import Path

_DEFAULT_SECRET_KEY = "dispo-dev-secret-CHANGE-ME-in-prod"
SECRET_KEY = os.getenv("DISPO_SECRET_KEY", _DEFAULT_SECRET_KEY)
ALGORITHM = "HS256"
ACCESS_TOKEN_EXPIRE_MINUTES = int(os.getenv("DISPO_TOKEN_EXPIRE_MINUTES", str(60 * 24 * 30)))

CORS_ORIGINS = [
    o.strip()
    for o in os.getenv("DISPO_CORS_ORIGINS", "*").split(",")
    if o.strip()
]

DATABASE_URL = os.getenv("DISPO_DATABASE_URL", "sqlite:///./dispo.db")

UPLOAD_DIR = Path(os.getenv("DISPO_UPLOAD_DIR", "./uploads"))

DEMO_MODE = os.getenv("DISPO_DEMO_MODE", "1").strip().lower() in {
    "1", "true", "yes", "on",
}
