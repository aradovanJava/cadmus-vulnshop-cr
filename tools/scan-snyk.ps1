# CADMUS-CR-14 · CR1 · Task 3 — dependency analysis
#
#   powershell -ExecutionPolicy Bypass -File tools\scan-snyk.ps1
#
# Requires a one-off authentication: snyk auth
# A free account is sufficient for this lab.

Push-Location (Join-Path $PSScriptRoot "..")

Write-Host "`n[1/3] Resolving the full dependency graph..." -ForegroundColor Cyan
mvn -B dependency:tree -DoutputFile=lab\dependency-tree.txt
Write-Host "      Written to lab\dependency-tree.txt" -ForegroundColor Green
Write-Host "      Count the DECLARED dependencies in pom.xml, then count the" -ForegroundColor Yellow
Write-Host "      RESOLVED artefacts in that file. Note both numbers." -ForegroundColor Yellow

Write-Host "`n[2/3] Snyk Open Source (dependencies)..." -ForegroundColor Cyan
snyk test --all-projects --json-file-output=lab\snyk-deps.json
snyk test --all-projects

Write-Host "`n[3/3] Snyk Code (static analysis of first-party source)..." -ForegroundColor Cyan
snyk code test --json-file-output=lab\snyk-code.json
snyk code test

Write-Host "`nBoth reports saved under lab\. Compare Snyk Code with the" -ForegroundColor Green
Write-Host "SonarQube findings - they will not agree, and the disagreement is" -ForegroundColor Green
Write-Host "itself a finding worth recording." -ForegroundColor Green
Pop-Location
