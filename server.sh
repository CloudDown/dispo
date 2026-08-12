#!/usr/bin/env bash
# Lance l'API Dispo en local (port 8000). Option : --ngrok (tunnel public 4G).
set -euo pipefail
ROOT="$(cd "$(dirname "$0")" && pwd)"
SCRIPTS="$(cd "$ROOT/.." && pwd)/scripts"
PORT="${DISPO_PORT:-8000}"
USE_NGROK=0
for arg in "$@"; do
  case "$arg" in --ngrok|ngrok) USE_NGROK=1 ;; esac
done
[[ "${NGROK:-0}" == "1" ]] && USE_NGROK=1

cd "$ROOT/server"

PYTHON=""
if [ -x "$HOME/.local/share/mise/installs/python/3.13.14/bin/python" ]; then
  PYTHON="$HOME/.local/share/mise/installs/python/3.13.14/bin/python"
elif command -v python3.13 >/dev/null 2>&1; then
  PYTHON="$(command -v python3.13)"
else
  PYTHON="$(command -v python3)"
fi

if [ ! -d .venv ]; then
  "$PYTHON" -m venv .venv
  .venv/bin/pip install -U pip
  .venv/bin/pip install -r requirements.txt
fi

export DISPO_DEMO_MODE="${DISPO_DEMO_MODE:-1}"

LAN_IP="$(ip -4 route get 1.1.1.1 2>/dev/null | awk '{for(i=1;i<=NF;i++) if($i=="src") print $(i+1)}' | head -1 || true)"
echo "Dispo API — http://localhost:${PORT}/docs"
[[ -n "$LAN_IP" ]] && echo "Téléphone (Wi-Fi) : http://${LAN_IP}:${PORT}/"

if [[ "$USE_NGROK" == "1" ]]; then
  # shellcheck disable=SC1091
  source "$SCRIPTS/with-ngrok.sh"
  start_ngrok "$PORT"
  echo "Téléphone (4G / hors Wi-Fi) : ${NGROK_URL}/"
  echo "  → rebuild APK : ./release-github.sh ngrok"
fi
echo

if [[ "$USE_NGROK" == "1" ]]; then
  # Pas d'exec : le trap ngrok doit tuer le tunnel à l'arrêt.
  .venv/bin/uvicorn main:app --reload --host 0.0.0.0 --port "$PORT"
else
  exec .venv/bin/uvicorn main:app --reload --host 0.0.0.0 --port "$PORT"
fi
