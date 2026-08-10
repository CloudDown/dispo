# Dispo — guide agents / contributeurs

Architecture inspirée de **Vif**.

## Layout monorepo

```
dispo/
├── mobile_app/     # Android Kotlin + Jetpack Compose
├── server/         # FastAPI + SQLModel (SQLite/Postgres)
├── dev/            # .env.example
├── start.sh        # lance l'API en local
└── AGENTS.md
```

## Serveur

- Entrée : `server/main.py`
- Config env préfixe `DISPO_*` : `server/config.py`
- Un domaine = dossier avec `router.py` (mince) + `service.py` + `models.py`
- Modules : `auth`, `friends`, `groups`, `availability`, `chat`
- Auth : JWT Bearer (`Authorization: Bearer …`)
- Doc interactive : `http://localhost:8000/docs`

### Lancer

```bash
./start.sh
# ou
cd server && python3 -m venv .venv && .venv/bin/pip install -r requirements.txt
.venv/bin/uvicorn main:app --reload --host 0.0.0.0 --port 8000
```

### Comptes démo (si `DISPO_DEMO_MODE=1`)

| ID  | Mot de passe | Nom |
|-----|--------------|-----|
| lea | demo         | Léa |
| max | demo         | Max |
| sam | demo         | Sam |

Groupe démo : code invite `CREWDEMO`.

## Mobile

Android Studio : ouvrir **`mobile_app/`** (pas la racine). Config Run : `app`.

```bash
cd mobile_app
./release-github.sh
./configure-device-api.sh lan   # Wi-Fi — ou usb / emulator / url
```

Modes API : `lan` · `usb` · `emulator` · `url` — clé `dispo.api.base.url` dans `local.properties`.

L'app est branchée au serveur via Retrofit (`mobile_app/.../data/`) + écran login.

## Conventions

1. Routers thin — logique dans `service.py`
2. Erreurs HTTP via `HTTPException`
3. Timestamps UTC naïfs (`timeutil.utcnow`)
4. Pas de secrets dans le dépôt (`dev/.env.example` seulement)
