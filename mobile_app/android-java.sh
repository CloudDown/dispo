#!/usr/bin/env bash
set -euo pipefail
SCRIPTS="$(cd "$(dirname "$0")/../.." && pwd)/scripts"
# shellcheck disable=SC1091
source "$SCRIPTS/android-java.sh"
