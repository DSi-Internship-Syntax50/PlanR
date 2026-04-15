# === Stage 1: Build the application ===
# We use the alpine version of Maven for the build stage as well
FROM maven:3-eclipse-temurin-25-alpine AS build
WORKDIR /app

# Copy the pom.xml and source code
COPY pom.xml .
COPY src ./src

# Build the jar file, skipping tests to speed up the build
RUN mvn clean package -DskipTests

# === Stage 2: Run the application ===
# We use the lightweight alpine JRE for the final running container
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app

# Copy the built jar from the build stage
COPY --from=build /app/target/*.jar app.jar

# Expose the standard Spring Boot port
EXPOSE 8080

# Command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]