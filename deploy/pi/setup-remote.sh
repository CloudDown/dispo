#!/usr/bin/env bash
# Installation API Dispo sur Raspberry Pi (systemd, redémarrage auto)
set -euo pipefail

DISPO_DIR="${DISPO_DIR:-/home/pi/dispo}"
DATA_DIR="${DISPO_DATA:-/var/lib/dispo}"
SERVICE_USER="${SERVICE_USER:-pi}"
SUDO_PASS="${SUDO_PASS:?SUDO_PASS/PI_PASS manquant — export PI_PASS ou oeuil/secrets.env}"
DISPO_PORT="${DISPO_PORT:-8000}"

sudo_cmd() {
  echo "$SUDO_PASS" | sudo -S "$@"
}

echo "==> Dispo API — installation Pi"
echo "    code  : $DISPO_DIR/server"
echo "    data  : $DATA_DIR"
echo "    port  : $DISPO_PORT"
echo

sudo_cmd mkdir -p "$DATA_DIR/uploads"
sudo_cmd chown -R "$SERVICE_USER:$SERVICE_USER" "$DATA_DIR"

cd "$DISPO_DIR/server"

PYTHON=""
if command -v python3.13 >/dev/null 2>&1; then
  PYTHON="$(command -v python3.13)"
elif command -v python3.12 >/dev/null 2>&1; then
  PYTHON="$(command -v python3.12)"
elif command -v python3.11 >/dev/null 2>&1; then
  PYTHON="$(command -v python3.11)"
else
  PYTHON="$(command -v python3)"
fi

echo "==> Python : $($PYTHON --version 2>&1)"

if [[ -d .venv ]] && ! .venv/bin/python -c "import sys; print(sys.version)" &>/dev/null; then
  echo "==> Suppression venv incompatible…"
  rm -rf .venv
fi

if [[ ! -x .venv/bin/python ]]; then
  rm -rf .venv
  "$PYTHON" -m venv .venv
fi

.venv/bin/pip install -q --upgrade pip
.venv/bin/pip install -q -r requirements.txt

echo "==> Service systemd"
UNIT="/tmp/dispo-api.service"
cat > "$UNIT" <<EOF
[Unit]
Description=Dispo API (FastAPI)
After=network-online.target
Wants=network-online.target

[Service]
Type=simple
User=$SERVICE_USER
Group=$SERVICE_USER
WorkingDirectory=$DISPO_DIR/server
Environment=DISPO_DATABASE_URL=sqlite:////$DATA_DIR/dispo.db
Environment=DISPO_UPLOAD_DIR=$DATA_DIR/uploads
Environment=DISPO_DEMO_MODE=1
Environment=DISPO_CORS_ORIGINS=*
Environment=PATH=$DISPO_DIR/server/.venv/bin:/usr/local/bin:/usr/bin:/bin
ExecStart=$DISPO_DIR/server/.venv/bin/uvicorn main:app --host 0.0.0.0 --port $DISPO_PORT
Restart=always
RestartSec=5

[Install]
WantedBy=multi-user.target
EOF

sudo_cmd cp "$UNIT" /etc/systemd/system/dispo-api.service
rm -f "$UNIT"

sudo_cmd systemctl daemon-reload
sudo_cmd systemctl enable dispo-api
sudo_cmd systemctl restart dispo-api

echo
echo "==> Statut"
sleep 2
sudo_cmd systemctl status dispo-api --no-pager -l || true
echo
LAN_IP="$(hostname -I | awk '{print $1}')"
echo "LAN  : http://${LAN_IP}:${DISPO_PORT}/docs"
echo "Logs : journalctl -u dispo-api -f"
