FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn clean install

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /app/target/gmail-mcp-server-1.0.0.jar .
CMD ["java", "-jar", "gmail-mcp-server-1.0.0.jar"]
