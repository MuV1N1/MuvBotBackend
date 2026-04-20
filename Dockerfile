FROM eclipse-temurin:17.0.18_8-jdk-alpine AS build
WORKDIR /app

COPY gradlew .
COPY gradle ./gradle
COPY build.gradle settings.gradle ./
COPY src ./src

RUN chmod +x gradlew
RUN ./gradlew bootJar --no-daemon

FROM eclipse-temurin:17.0.18_8-jre-alpine
WORKDIR /app

ARG OPENAI_API_KEY
ENV OPENAI_API_KEY=$OPENAI_API_KEY

ARG DB_URL
ENV DB_URL=$DB_URL

ARG DB_USER
ENV DB_USER=$DB_USER

ARG DB_PASS
ENV DB_PASS=$DB_PASS

ARG DISCORD_BOT_TOKEN
ENV DISCORD_BOT_TOKEN=$DISCORD_BOT_TOKEN

ARG DISCORD_CLIENT_ID
ENV DISCORD_CLIENT_ID=$DISCORD_CLIENT_ID

ARG DISCORD_CLIENT_SECRET
ENV DISCORD_CLIENT_SECRET=$DISCORD_CLIENT_SECRET

ARG DISCORD_REDIRECT_URI
ENV DISCORD_REDIRECT_URI=$DISCORD_REDIRECT_URI

RUN addgroup -S app && adduser -S app -G app
USER app

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
