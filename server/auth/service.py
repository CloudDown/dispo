import secrets
import string
from datetime import timedelta
from typing import Optional

import bcrypt
from fastapi import Depends, HTTPException, status
from fastapi.security import OAuth2PasswordBearer
from jose import JWTError, jwt
from sqlmodel import Session, select

from auth.models import User
from config import ACCESS_TOKEN_EXPIRE_MINUTES, ALGORITHM, SECRET_KEY
from db import get_session
from timeutil import utcnow

oauth2_scheme = OAuth2PasswordBearer(tokenUrl="/auth/login")
_BCRYPT_MAX_BYTES = 72
_ID_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"


def generate_public_id(length: int = 6) -> str:
    return "".join(secrets.choice(_ID_ALPHABET) for _ in range(length))


def hash_password(password: str) -> str:
    return bcrypt.hashpw(
        password.encode("utf-8")[:_BCRYPT_MAX_BYTES],
        bcrypt.gensalt(),
    ).decode("ascii")


def verify_password(plain: str, hashed: str) -> bool:
    try:
        return bcrypt.checkpw(
            plain.encode("utf-8")[:_BCRYPT_MAX_BYTES],
            hashed.encode("ascii"),
        )
    except ValueError:
        return False


def create_token(user_id: int) -> str:
    expire = utcnow() + timedelta(minutes=ACCESS_TOKEN_EXPIRE_MINUTES)
    payload = {"sub": str(user_id), "exp": expire}
    return jwt.encode(payload, SECRET_KEY, algorithm=ALGORITHM)


def decode_token(token: str) -> dict:
    try:
        return jwt.decode(token, SECRET_KEY, algorithms=[ALGORITHM])
    except JWTError as exc:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Token invalide",
        ) from exc


def get_current_user(
    token: str = Depends(oauth2_scheme),
    session: Session = Depends(get_session),
) -> User:
    payload = decode_token(token)
    user = session.get(User, int(payload["sub"]))
    if not user or not user.actif:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Utilisateur introuvable",
        )
    return user


def find_user_by_public_id(session: Session, public_id: str) -> Optional[User]:
    """Lookup insensible à la casse (anciens seeds type LEA001)."""
    from sqlalchemy import func

    pid = public_id.strip().lower()
    if not pid:
        return None
    return session.exec(select(User).where(func.lower(User.public_id) == pid)).first()
