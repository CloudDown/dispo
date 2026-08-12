# Dispo — guide agents

## Layout

- `server/` — FastAPI, port **8000**, env `DISPO_*`
- `mobile_app/` — Android Kotlin + Compose (ouvrir ce dossier dans Android Studio)

## Commandes

```bash
./server.sh                              # API locale
./server.sh --ngrok                      # API + tunnel public
./release-github.sh                      # APK LAN
./release-github.sh ngrok                # APK ngrok (tunnel déjà actif)
cd mobile_app && ./gradlew assembleDebug
```

## Conventions

- Routers fins, logique dans `*/service.py`
- Entrypoint : `server/main.py`, config : `server/config.py`
- Pas de secrets versionnés (`server/.env.example`)
