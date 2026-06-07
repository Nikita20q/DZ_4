FROM gradle:8.5-jdk21 AS builder
WORKDIR /app

COPY build.gradle.kts settings.gradle.kts ./
COPY src ./src
RUN gradle build -x test --no-daemon

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN apk add --no-cache fontconfig ttf-dejavu

COPY --from=builder /app/build/libs/*.jar app.jar

RUN mkdir -p /app/wordclouds

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]