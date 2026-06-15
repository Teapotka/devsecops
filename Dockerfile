# syntax=docker/dockerfile:1

FROM gradle:9.5.1-jdk21 AS build
WORKDIR /home/gradle/src

COPY build.gradle.kts settings.gradle.kts ./
COPY src src

RUN --mount=type=cache,target=/home/gradle/.gradle \
    gradle bootJar --no-daemon -x test

FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app

RUN groupadd -r appuser && useradd -r -g appuser appuser

COPY --from=build /home/gradle/src/build/libs/devsecops-*.jar app.jar
RUN chown appuser:appuser app.jar

USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
