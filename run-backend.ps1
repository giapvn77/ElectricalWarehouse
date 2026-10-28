$root = $PSScriptRoot
. (Join-Path $root ".env.local.ps1")
Set-Location (Join-Path $root "backend")
.\mvnw.cmd spring-boot:run
