#!/usr/bin/env bash
# Installe + lance Dispo sur le téléphone USB.
# Usage: ./run-cable.sh [--build]
set -euo pipefail
ROOT="$(cd "$(dirname "$0")" && pwd)"
SCRIPTS="$(cd "$ROOT/.." && pwd)/scripts"
export MOBILE_ROOT="$ROOT/mobile_app"
export APP_ID="com.dispo.app"
export APP_ACTIVITY=".MainActivity"
export API_KEY="dispo.api.base.url"
export API_PORT="${DISPO_API_PORT:-8000}"
export API_APP="dispo"
export TRAILING_SLASH=yes
exec "$SCRIPTS/run-cable.sh" "$@"
