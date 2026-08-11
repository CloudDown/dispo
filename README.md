# Dispo

App Android + API FastAPI — « je suis dispo jusqu'à minuit ».

**APK** : [GitHub Releases](https://github.com/CloudDown/dispo/releases/latest)

## Utilisation

1. **Backend** — sur le PC :
   ```bash
   ./server.sh
   ```
   → `http://localhost:8000/docs`

2. **Publier l'APK** — après des changements mobile :
   ```bash
   ./release-github.sh
   ```

3. **Téléphone** — télécharge l'APK depuis GitHub Releases, même Wi-Fi que le PC.

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
