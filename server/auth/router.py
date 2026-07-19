from fastapi import APIRouter, Depends, HTTPException, status
from sqlmodel import Session, select

from auth.models import (
    LoginRequest,
    ProfileUpdateRequest,
    RegisterRequest,
    TokenResponse,
    User,
    UserPublic,
)
from auth.service import (
    create_token,
    find_user_by_public_id,
    generate_public_id,
    get_current_user,
    hash_password,
    verify_password,
)
from db import get_session

router = APIRouter(prefix="/auth", tags=["auth"])


def _token_for(user: User) -> TokenResponse:
    return TokenResponse(
        access_token=create_token(user.id),
        user_id=user.id,
        public_id=user.public_id,
        display_name=user.display_name,
        avatar_color=user.avatar_color,
    )


@router.post("/register", response_model=TokenResponse, status_code=status.HTTP_201_CREATED)
def register(req: RegisterRequest, session: Session = Depends(get_session)):
    name = req.display_name.strip()
    if len(name) < 1:
        raise HTTPException(400, "Nom requis")
    if len(req.password) < 4:
        raise HTTPException(400, "Mot de passe trop court (min. 4)")

    if req.public_id:
        public_id = req.public_id.strip().upper()
        if session.exec(select(User).where(User.public_id == public_id)).first():
            raise HTTPException(400, "Cet ID est déjà pris")
    else:
        # Génère un ID unique
        for _ in range(20):
            public_id = generate_public_id()
            if not session.exec(select(User).where(User.public_id == public_id)).first():
                break
        else:
            raise HTTPException(500, "Impossible de générer un ID")

    user = User(
        public_id=public_id,
        display_name=name[:64],
        hashed_password=hash_password(req.password),
        avatar_color=0,
    )
    session.add(user)
    session.commit()
    session.refresh(user)
    return _token_for(user)


@router.post("/login", response_model=TokenResponse)
def login(req: LoginRequest, session: Session = Depends(get_session)):
    user = find_user_by_public_id(session, req.public_id)
    if not user or not verify_password(req.password, user.hashed_password):
        raise HTTPException(status.HTTP_401_UNAUTHORIZED, "ID ou mot de passe incorrect")
    if not user.actif:
        raise HTTPException(status.HTTP_403_FORBIDDEN, "Compte désactivé")
    return _token_for(user)


@router.get("/me", response_model=UserPublic)
def me(current_user: User = Depends(get_current_user)):
    return current_user


@router.patch("/me", response_model=UserPublic)
def update_me(
    req: ProfileUpdateRequest,
    session: Session = Depends(get_session),
    current_user: User = Depends(get_current_user),
):
    if req.display_name is not None:
        name = req.display_name.strip()
        if not name:
            raise HTTPException(400, "Nom invalide")
        current_user.display_name = name[:64]
    if req.avatar_color is not None:
        current_user.avatar_color = abs(req.avatar_color) % 6
    session.add(current_user)
    session.commit()
    session.refresh(current_user)
    return current_user


@router.get("/users/{public_id}", response_model=UserPublic)
def lookup_user(public_id: str, session: Session = Depends(get_session)):
    user = find_user_by_public_id(session, public_id)
    if not user or not user.actif:
        raise HTTPException(404, "Utilisateur introuvable")
    return user
