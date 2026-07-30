#!/usr/bin/env bash
# Copie le serveur Dispo sur la Pi puis lance setup-remote.sh
set -euo pipefail

PI_HOST="${PI_HOST:-192.168.2.170}"
PI_USER="${PI_USER:-pi}"
PI_DIR="${PI_DIR:-/home/pi/dispo}"
ROOT="$(cd "$(dirname "$0")/../.." && pwd)"

echo "==> rsync vers ${PI_USER}@${PI_HOST}:${PI_DIR}"
rsync -avz --delete \
  --exclude .git \
  --exclude .venv \
  --exclude __pycache__ \
  --exclude mobile_app \
  --exclude 'server/dispo.db' \
  --exclude 'server/uploads' \
  "$ROOT/" "${PI_USER}@${PI_HOST}:${PI_DIR}/"

echo "==> Installation distante"
ssh "${PI_USER}@${PI_HOST}" "chmod +x ${PI_DIR}/deploy/pi/setup-remote.sh && ${PI_DIR}/deploy/pi/setup-remote.sh"

echo "==> OK — http://${PI_HOST}:8000/docs"
