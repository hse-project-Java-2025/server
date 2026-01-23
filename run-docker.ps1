$ErrorActionPreference = "Stop"

Set-Location $PSScriptRoot

.\gradlew.bat clean build
docker-compose up --build -d