# Build stage
FROM gradle:8.5-jdk17 AS build
WORKDIR /app

# Copy all project files
COPY . .

# Build the application
RUN gradle clean build -x test --no-daemon

# Run stage - Use eclipse-temurin instead of openjdk
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy the built jar from build stage
COPY --from=build /app/build/libs/*.jar app.jar

# Expose port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-Dserver.port=$PORT", "-jar", "app.jar"]