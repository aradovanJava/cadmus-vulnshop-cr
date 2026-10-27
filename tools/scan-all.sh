#!/usr/bin/env bash
# CADMUS-CR-14 · CR1 · Linux/macOS equivalent of the PowerShell scripts.
set -euo pipefail
cd "$(dirname "$0")/.."

: "${SONAR_TOKEN:?Set SONAR_TOKEN first (SonarQube > My Account > Security)}"

echo "[1/4] Build"
mvn -B -DskipTests clean compile

echo "[2/4] Dependency graph"
mkdir -p lab
mvn -B dependency:tree -DoutputFile=lab/dependency-tree.txt

echo "[3/4] SonarQube"
mvn -B sonar:sonar -Dsonar.host.url=http://localhost:9000 -Dsonar.token="$SONAR_TOKEN"

echo "[4/4] Snyk"
snyk test --all-projects --json-file-output=lab/snyk-deps.json || true
snyk code test --json-file-output=lab/snyk-code.json || true

echo
echo "Open http://localhost:9000 and select cadmus-vulnshop-cr."
echo "Remember: Issues and Security Hotspots are separate tabs."
