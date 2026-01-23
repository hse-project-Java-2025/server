#!/bin/bash
set -e

./gradlew clean build
docker-compose up --build -d postgres app
