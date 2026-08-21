# Dispo

App Android + API FastAPI — « je suis dispo jusqu'à minuit ».

**APK** : [GitHub Releases](https://github.com/CloudDown/dispo/releases/latest)  
**API publique** : https://dispo.instree.org

## Utilisation

```bash
./server.sh              # API locale + tunnel Cloudflare
./release-github.sh      # APK GitHub + mise à jour in-app (popup téléphone)
./run-cable.sh           # installe + lance sur téléphone USB (adb)
./run-cable.sh --build   # rebuild debug (API usb) puis lance
```

Après `./release-github.sh`, laisse `./server.sh` tourner : au prochain lancement, l'app propose d'installer la nouvelle version.

LAN seul : `./server.sh --local` puis `./release-github.sh lan`.

Comptes démo : `LEA001` / `demo` (crew `CREWDEMO`).

Conventions Cursor : [AGENTS.md](AGENTS.md)
