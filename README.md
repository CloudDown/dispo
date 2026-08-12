# Dispo

App Android + API FastAPI — « je suis dispo jusqu'à minuit ».

**APK** : [GitHub Releases](https://github.com/CloudDown/dispo/releases/latest)

## Utilisation

```bash
./server.sh              # API + ngrok (4G OK)
./release-github.sh      # APK → URL ngrok courante
```

Sans tunnel : `./server.sh --local` puis `./release-github.sh lan` (même Wi-Fi).

Comptes démo : `LEA001` / `demo` (crew `CREWDEMO`).

> L’URL ngrok change à chaque redémarrage → refaire `./release-github.sh` après un nouveau `./server.sh`.

## Structure

```
dispo/
├── server.sh
├── release-github.sh
├── server/
└── mobile_app/
```

Conventions Cursor : [AGENTS.md](AGENTS.md)
