# Dispo

App Android + API FastAPI — « je suis dispo jusqu'à minuit ».

**APK** : [GitHub Releases](https://github.com/CloudDown/dispo/releases/latest)  
**API publique** : https://dispo.instree.org

## Utilisation

```bash
./server.sh              # API locale + tunnel Cloudflare
./release-github.sh      # APK → https://dispo.instree.org/
```

LAN seul : `./server.sh --local` puis `./release-github.sh lan`.

Comptes démo : `LEA001` / `demo` (crew `CREWDEMO`).

Conventions Cursor : [AGENTS.md](AGENTS.md)
