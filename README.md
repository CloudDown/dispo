# Dispo

App Android + API FastAPI — « je suis dispo jusqu'à minuit ».

**APK** : [GitHub Releases](https://github.com/CloudDown/dispo/releases/latest)

## Utilisation

1. **Backend** — sur le PC :
   ```bash
   ./server.sh            # Wi-Fi local (port 8000)
   ./server.sh --ngrok    # + tunnel public (4G / hors Wi-Fi)
   ```

2. **Publier l'APK** :
   ```bash
   ./release-github.sh          # APK → IP LAN
   ./release-github.sh ngrok    # APK → URL ngrok (server --ngrok doit tourner)
   ```

3. **Téléphone** — télécharge l'APK depuis GitHub Releases.
   - LAN : même Wi-Fi que le PC
   - ngrok : marche en 4G aussi

Comptes démo : `LEA001` / `demo` (crew `CREWDEMO`).

## Structure

```
dispo/
├── server.sh
├── release-github.sh
├── server/          # FastAPI
└── mobile_app/      # Android (ouvrir dans Android Studio)
```

Conventions Cursor : [AGENTS.md](AGENTS.md)
