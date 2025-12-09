# Stage 1: Build the application
FROM gradle:jdk17 AS build
WORKDIR /home/gradle/src
COPY --chown=gradle:gradle . .
RUN gradle build -x test --no-daemon

# Stage 2: Create the final, smaller image
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
ARG JAR_FILE=build/libs/study-0.0.1-SNAPSHOT.jar
COPY --from=build /home/gradle/src/${JAR_FILE} app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
