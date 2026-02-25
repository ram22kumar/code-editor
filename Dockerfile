# Build stage
FROM gradle:8.5-jdk17 AS build
WORKDIR /app

# Copy gradle wrapper and build files first (for caching)
COPY gradlew gradlew.bat ./
COPY gradle ./gradle
COPY build.gradle settings.gradle ./

# Download dependencies (cached layer)
RUN gradle dependencies --no-daemon || true

# Copy source code
COPY src ./src

# Build the application with more memory
RUN gradle clean build -x test --no-daemon --stacktrace

# Run stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy the jar file
COPY --from=build /app/build/libs/*.jar app.jar

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Run the app
ENTRYPOINT ["java", "-Dserver.port=${PORT:-8080}", "-jar", "app.jar"]