#!/usr/bin/env bash
# Build APK + publie sur GitHub Releases. Usage: ./release-github.sh [version]
set -euo pipefail
ROOT="$(cd "$(dirname "$0")" && pwd)"
SCRIPTS="$(cd "$ROOT/.." && pwd)/scripts"
export MOBILE_ROOT="$ROOT/mobile_app"
export API_KEY="dispo.api.base.url"
export API_PORT="${DISPO_API_PORT:-8000}"
export TRAILING_SLASH=yes
exec "$SCRIPTS/release-github.sh" CloudDown/dispo Dispo lan "${1:-}"
