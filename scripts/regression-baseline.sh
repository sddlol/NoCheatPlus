#!/usr/bin/env bash
set -u

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

START_TS="$(date '+%Y-%m-%d %H:%M:%S %z')"
FAIL=0

echo "== NoCheatPlus Regression Baseline =="
echo "Start: $START_TS"
echo "Repo : $ROOT_DIR"

action() {
  local name="$1"
  shift
  echo ""
  echo "[RUN ] $name"
  if "$@"; then
    echo "[PASS] $name"
  else
    echo "[FAIL] $name"
    FAIL=1
  fi
}

action "Build all modules (skip tests)" mvn -q -DskipTests package
action "Build NCPCore dependency chain (skip tests)" mvn -q -pl NCPCore -am -DskipTests package

END_TS="$(date '+%Y-%m-%d %H:%M:%S %z')"
echo ""
echo "End  : $END_TS"
if [ "$FAIL" -eq 0 ]; then
  echo "RESULT: PASS"
  exit 0
else
  echo "RESULT: FAIL"
  exit 1
fi
