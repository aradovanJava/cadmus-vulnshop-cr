# CADMUS-CR-14 · CR1 · start the application under test
#
#   powershell -ExecutionPolicy Bypass -File tools\run-app.ps1
#
# The application binds to 127.0.0.1 only. Do not change server.address.
# It is deliberately exploitable, including at least one remote code
# execution path, and must never be reachable off this host.

Push-Location (Join-Path $PSScriptRoot "..")
Write-Host "`nStarting VulnShop on http://127.0.0.1:8080/shop" -ForegroundColor Cyan
Write-Host "Training accounts: alice / bob / carol / adminjoe" -ForegroundColor Cyan
Write-Host "Ctrl+C to stop.`n" -ForegroundColor Cyan
mvn -B -DskipTests spring-boot:run
Pop-Location
