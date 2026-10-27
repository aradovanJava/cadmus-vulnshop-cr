# CADMUS-CR-14 · CR1 · Task 0 — environment check (VM1, Windows 11)
#
#   powershell -ExecutionPolicy Bypass -File tools\verify-env.ps1
#
# Run this first. If anything below reports MISSING, tell your instructor
# before starting the lab rather than losing lab time to a broken toolchain.

$ErrorActionPreference = "Continue"
$ok = $true

function Check($name, $cmd, $args) {
    Write-Host -NoNewline ("{0,-16}" -f $name)
    try {
        $out = & $cmd $args 2>&1 | Select-Object -First 1
        Write-Host "OK    $out" -ForegroundColor Green
    } catch {
        Write-Host "MISSING" -ForegroundColor Red
        $script:ok = $false
    }
}

Write-Host "`nCADMUS-CR-14 CR1 - environment check`n" -ForegroundColor Cyan

Check "java"    "java"    "-version"
Check "mvn"     "mvn"     "-v"
Check "git"     "git"     "--version"
Check "docker"  "docker"  "--version"
Check "snyk"    "snyk"    "--version"
Check "node"    "node"    "--version"

Write-Host -NoNewline ("{0,-16}" -f "sonarqube")
try {
    $r = Invoke-WebRequest -Uri "http://localhost:9000/api/system/status" -UseBasicParsing -TimeoutSec 5
    Write-Host "OK    $($r.Content)" -ForegroundColor Green
} catch {
    Write-Host "NOT RUNNING - start it with: docker compose up -d sonarqube" -ForegroundColor Yellow
}

Write-Host ""
if ($ok) { Write-Host "Toolchain ready." -ForegroundColor Green }
else     { Write-Host "Something is missing - raise it now." -ForegroundColor Red }
