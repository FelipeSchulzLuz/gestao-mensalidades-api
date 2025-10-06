#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT_DIR"

if [ $# -eq 0 ]; then
  echo "No target provided. Defaulting to 'start'"
  make start
else
  make "$@"
fi
