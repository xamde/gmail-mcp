# Stage 1: Build the application
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Create the final image
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
# TODO: Add volume mounting for configuration and tokens directories
# e.g., VOLUME /app/config
# e.g., VOLUME /app/tokens
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
