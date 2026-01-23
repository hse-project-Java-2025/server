$ErrorActionPreference = "Stop"

Set-Location $PSScriptRoot

.\gradlew.bat clean build
docker-compose down -v
docker-compose up --build -d