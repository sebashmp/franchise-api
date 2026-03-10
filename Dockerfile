# ─── Stage 1: Build ───────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app

# Copy only dependency files first to leverage Docker layer caching
COPY gradlew settings.gradle build.gradle ./
COPY gradle gradle

RUN ./gradlew dependencies --no-daemon || true

# Copy source and build the fat JAR (skip tests — CI already ran them)
COPY src src
RUN ./gradlew bootJar --no-daemon -x test

# ─── Stage 2: Runtime ─────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine AS runtime
WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
