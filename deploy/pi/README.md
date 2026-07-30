# Dispo API sur Raspberry Pi

Serveur FastAPI en **systemd** (`dispo-api.service`) — redémarrage auto, données dans `/var/lib/dispo`.

## Prérequis Pi

- Raspberry Pi OS **64-bit**, Python 3.11+
- Port **8000** libre (Instree utilise 1488 — pas de conflit)
- SSH activé

## Déploiement automatique (depuis ton PC)

```bash
cd /chemin/vers/dispo
python3 deploy/pi/deploy_paramiko.py
```

Variables : `PI_PASS` **obligatoire** (`oeuil/secrets.env` ou `export`). Optionnelles : `PI_HOST`, `PI_USER`, `PI_DIR`, `PI_DATA`.

## Déploiement manuel

```bash
# PC — copie (sans mobile_app)
rsync -avz --exclude .git --exclude .venv --exclude mobile_app \
  ./ pi@192.168.2.170:/home/pi/dispo/

# Pi
ssh pi@192.168.2.170
cd ~/dispo && chmod +x deploy/pi/setup-remote.sh && ./deploy/pi/setup-remote.sh
```

Ou en une commande :

```bash
./deploy/pi/deploy-from-dev.sh
```

## Résultat

| Élément | Chemin / URL |
|---------|----------------|
| Code | `/home/pi/dispo/server` |
| SQLite + uploads | `/var/lib/dispo/` |
| Service | `dispo-api.service` |
| Docs API | `http://192.168.2.170:8000/docs` |
| Health | `http://192.168.2.170:8000/health` |

```bash
sudo systemctl status dispo-api
journalctl -u dispo-api -f
```

Comptes démo (mode par défaut) : `LEA001` / `MAX002` / `SAM003` — mdp `demo`, groupe `CREWDEMO`.

## Tunnel public (ngrok)

Sur la Pi, l’API LAN tourne via `dispo-api.service`. Pour l’exposer hors du Wi‑Fi :

```bash
# Prérequis : ngrok installé + authtoken dans ~/.config/ngrok/ngrok.yml
sudo cp deploy/systemd/dispo-ngrok.service.example /etc/systemd/system/dispo-ngrok.service
sudo systemctl daemon-reload
sudo systemctl enable --now dispo-ngrok
curl -s http://127.0.0.1:4040/api/tunnels | jq -r '.tunnels[0].public_url'
```

L’URL ngrok change à chaque redémarrage (plan gratuit), sauf domaine réservé ngrok.

## Mobile (LAN)

Quand Retrofit sera branché, pointer l’app vers :

```text
http://192.168.2.170:8000/
```

## Production / sécurité

Éditer le service systemd pour :

```ini
Environment=DISPO_DEMO_MODE=0
Environment=DISPO_SECRET_KEY=…
Environment=DISPO_CORS_ORIGINS=http://192.168.2.0/24
```

Puis :

```bash
sudo systemctl daemon-reload && sudo systemctl restart dispo-api
```
