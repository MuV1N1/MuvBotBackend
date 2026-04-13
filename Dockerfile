FROM eclipse-temurin:17.0.18_8-jdk-alpine AS build
WORKDIR /app
COPY . .
RUN ./gradlew bootJar --no-daemon

FROM eclipse-temurin:17.0.18_8-jre-alpine
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar

ENV DISCORD_BOT_TOKEN="your_token_here"
ENV DB_URL="jdbc:postgresql://postgres:8888/postgres"
ENV DB_USER="postgres"
ENV DB_PASS="password"

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
