#!/usr/bin/env bash
# Control the drone simulation from the terminal: picks a random parcelle every
# 20s, runs AI analysis on a random dataset image, and stores it in that
# parcelle's image history.
#
# Usage:
#   ./scripts/drone-sim.sh on
#   ./scripts/drone-sim.sh off
#   ./scripts/drone-sim.sh status
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost}"
EMAIL="${DRONE_SIM_EMAIL:-karim@ferme.ma}"
PASSWORD="${DRONE_SIM_PASSWORD:-password123}"
ACTION="${1:-status}"

TOKEN=$(curl -s -X POST "$BASE_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"$EMAIL\",\"motDePasse\":\"$PASSWORD\"}" \
  | python3 -c "import sys,json; print(json.load(sys.stdin)['token'])")

case "$ACTION" in
  on|start)
    curl -s "$BASE_URL/api/simulation/drone/status" -H "Authorization: Bearer $TOKEN" | grep -q '"enabled":true' \
      && { echo "Already running."; exit 0; }
    curl -s -X POST "$BASE_URL/api/simulation/drone/toggle" -H "Authorization: Bearer $TOKEN"
    echo
    echo "Drone simulation started — capturing a random parcelle every ~20s."
    ;;
  off|stop)
    curl -s -X POST "$BASE_URL/api/simulation/drone/toggle" -H "Authorization: Bearer $TOKEN"
    echo
    echo "Drone simulation stopped."
    ;;
  status)
    curl -s "$BASE_URL/api/simulation/drone/status" -H "Authorization: Bearer $TOKEN"
    echo
    ;;
  *)
    echo "Usage: $0 {on|off|status}" >&2
    exit 1
    ;;
esac
