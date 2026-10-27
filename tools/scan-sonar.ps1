# CADMUS-CR-14 · CR1 · Task 2 — static analysis
#
#   powershell -ExecutionPolicy Bypass -File tools\scan-sonar.ps1 -Token sqp_xxx
#
# Generate the token in SonarQube: My Account > Security > Generate Token
# (type: Global Analysis Token).
#
# The scan takes roughly two to four minutes on the lab VM. Start it, then
# begin reading source code while it runs - do not sit and watch it.

param(
    [Parameter(Mandatory=$true)][string]$Token,
    [string]$SonarUrl = "http://localhost:9000"
)

Push-Location (Join-Path $PSScriptRoot "..")

Write-Host "`n[1/2] Building (tests skipped - we are analysing, not verifying)..." -ForegroundColor Cyan
mvn -B -DskipTests clean compile
if ($LASTEXITCODE -ne 0) { Write-Host "Build failed. Fix this before scanning." -ForegroundColor Red; Pop-Location; exit 1 }

Write-Host "`n[2/2] Running SonarQube analysis..." -ForegroundColor Cyan
mvn -B sonar:sonar `
    "-Dsonar.host.url=$SonarUrl" `
    "-Dsonar.token=$Token"

if ($LASTEXITCODE -eq 0) {
    Write-Host "`nDone. Open $SonarUrl and select the cadmus-vulnshop-cr project." -ForegroundColor Green
    Write-Host "Go to Issues, then filter by Software Quality = Security." -ForegroundColor Green
    Write-Host "Then look at Security Hotspots SEPARATELY - they are a different tab" -ForegroundColor Yellow
    Write-Host "and a different kind of claim. Both are in scope for this lab." -ForegroundColor Yellow
}
Pop-Location
