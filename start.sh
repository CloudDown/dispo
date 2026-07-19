#!/usr/bin/env bash
# Démarre l'API Dispo en local (layout type Vif).
set -euo pipefail
ROOT="$(cd "$(dirname "$0")" && pwd)"
cd "$ROOT/server"

# Python 3.13+ recommandé (3.14 casse FastAPI/SQLModel pour l'instant)
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
exec .venv/bin/uvicorn main:app --reload --host 0.0.0.0 --port 8000
