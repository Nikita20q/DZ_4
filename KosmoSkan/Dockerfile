FROM gradle:8.5-jdk21 AS builder
WORKDIR /app

COPY build.gradle.kts settings.gradle.kts ./
COPY src ./src
RUN gradle build -x test --no-daemon

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

RUN mkdir -p /app/uploads

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]