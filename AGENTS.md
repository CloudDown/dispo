# Dispo — guide agents

## Layout

- `server/` — FastAPI, port **8000**, env `DISPO_*`
- `mobile_app/` — Android Kotlin + Compose
- Public : `https://dispo.instree.org` (Cloudflare Tunnel hub)

## Commandes

```bash
./server.sh                 # API + assure le tunnel Cloudflare
./server.sh --local         # sans tunnel
./release-github.sh         # APK Cloudflare
./release-github.sh lan     # APK LAN
./run-cable.sh              # install + launch USB
./run-cable.sh --build      # rebuild debug USB puis launch
```
