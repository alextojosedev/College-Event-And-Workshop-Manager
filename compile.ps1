Write-Host "====================================================" -ForegroundColor Cyan
Write-Host "Compiling College Event and Workshop Manager..." -ForegroundColor Cyan
Write-Host "====================================================" -ForegroundColor Cyan

if (-not (Test-Path "bin")) {
    New-Item -ItemType Directory -Path "bin" | Out-Null
}

$sources = (Get-ChildItem -Path "src/main/java", "src/test/java" -Recurse -Filter "*.java").FullName
javac -encoding UTF-8 -cp "lib/*" -d bin $sources

if ($LASTEXITCODE -eq 0) {
    Copy-Item -Path "src/main/resources/*" -Destination "bin" -Recurse -Force
    Write-Host "[SUCCESS] Compilation completed successfully into bin/" -ForegroundColor Green
} else {
    Write-Host "[ERROR] Compilation failed!" -ForegroundColor Red
}
