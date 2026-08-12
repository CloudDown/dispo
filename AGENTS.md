# Dispo — guide agents

## Layout

- `server/` — FastAPI, port **8000**, env `DISPO_*`
- `mobile_app/` — Android Kotlin + Compose

## Commandes

```bash
./server.sh                 # API + ngrok (défaut)
./server.sh --local         # sans tunnel
./release-github.sh         # APK ngrok (défaut)
./release-github.sh lan     # APK LAN
```

## Conventions

- Routers fins, logique dans `*/service.py`
- Ouvrir **`mobile_app/`** dans Android Studio
