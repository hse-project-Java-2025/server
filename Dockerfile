FROM eclipse-temurin:21-jdk-alpine AS builder

RUN apk add --no-cache bash

WORKDIR /app

COPY gradlew gradlew.bat settings.gradle build.gradle ./
COPY gradle ./gradle
COPY src ./src

RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew bootJar --no-daemon
FROM eclipse-temurin:21-jdk-alpine

RUN apk add --no-cache ffmpeg curl

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

ENV JAVA_OPTS=""

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]