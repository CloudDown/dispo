#!/usr/bin/env bash
# Build APK + publie sur GitHub Releases.
# Usage: ./release-github.sh [lan|ngrok|usb|emulator] [version]
set -euo pipefail
ROOT="$(cd "$(dirname "$0")" && pwd)"
SCRIPTS="$(cd "$ROOT/.." && pwd)/scripts"
MODE="lan"
VER=""
if [[ "${1:-}" =~ ^(lan|ngrok|usb|emulator)$ ]]; then
  MODE="$1"
  VER="${2:-}"
else
  VER="${1:-}"
fi
export MOBILE_ROOT="$ROOT/mobile_app"
export API_KEY="dispo.api.base.url"
export API_PORT="${DISPO_API_PORT:-8000}"
export TRAILING_SLASH=yes
exec "$SCRIPTS/release-github.sh" CloudDown/dispo Dispo "$MODE" "$VER"
