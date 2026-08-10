#!/usr/bin/env bash
# Wrapper → scripts hub. Usage: ./configure-device-api.sh [lan|usb|emulator|url …]
set -euo pipefail
ROOT="$(cd "$(dirname "$0")" && pwd)"
SCRIPTS="$(cd "$ROOT/../.." && pwd)/scripts"
export MOBILE_ROOT="$ROOT"
export API_KEY="dispo.api.base.url"
export API_PORT="${DISPO_API_PORT:-8000}"
export TRAILING_SLASH=yes
exec "$SCRIPTS/configure-device-api.sh" "${1:-lan}" "${@:2}"
