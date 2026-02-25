# Build stage
FROM gradle:8.14-jdk17 AS build
WORKDIR /app
COPY . .
RUN gradle clean build -x test --no-daemon

# Run stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080

# Use shell to properly expand PORT variable
CMD sh -c "java -Dserver.port=\${PORT:-8080} -jar app.jar"