# Plan Dispo — architecture type Vif

## Décision

Monorepo `mobile_app/` + `server/` (comme Vif), pas de PWA.

## Serveur (fait)

Modules domaine FastAPI :

- `auth` — users, public_id, JWT
- `friends` — ajout par ID
- `groups` — crews + invite codes
- `availability` — toggle dispo fin de journée
- `chat` — messages groupe (gate ≥ 2 dispos)

## Mobile (fait UI locale)

Compose + profil + crew local. Prochaine étape :

1. Couche `data/` Retrofit + TokenStore (pattern Vif)
2. Remplacer DispoRepository local par appels API
3. WebSocket / polling pour chat temps réel (Vif n'a pas ça — à inventer)

## Lancer

```bash
./start.sh                          # API :8000
cd mobile_app && ./gradlew assembleDebug
```
