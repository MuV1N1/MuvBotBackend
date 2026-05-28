# ── Stage 1: Build ──────────────────────────────────────────────────────────
FROM eclipse-temurin:17.0.18_8-jdk-alpine AS builder

WORKDIR /app


# Copy Gradle wrapper & dependency declarations first (layer caching)
COPY gradlew .
COPY gradle/ gradle/
COPY build.gradle .
COPY settings.gradle .

# Pre-download dependencies (cached unless build.gradle changes)
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon --quiet || true

# Copy source and build the fat JAR
COPY src/ src/
RUN ./gradlew bootJar --no-daemon -x test

# ── Stage 2: Runtime ─────────────────────────────────────────────────────────
FROM eclipse-temurin:17.0.18_8-jre-alpine

WORKDIR /app

# Runtime env vars (set by Docker -e or GitLab CI secrets – no defaults here)
ENV OPENAI_API_KEY="" \
    DB_URL="" \
    DB_USER="" \
    DB_PASS="" \
    DISCORD_BOT_TOKEN="" \
    DISCORD_CLIENT_ID="" \
    DISCORD_CLIENT_SECRET="" \
    DISCORD_REDIRECT_URI="" \
    CORS_ALLOWED_ORIGINS=""

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
