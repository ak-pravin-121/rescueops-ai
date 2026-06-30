# ---- Build stage ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B dependency:go-offline
COPY src ./src
RUN mvn -B clean package -DskipTests

# ---- Run stage ----
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Run as a non-root user (good practice; also satisfies some platform security scans)
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=build /app/target/rescueops-ai-backend.jar app.jar
EXPOSE 8080

# -XX:MaxRAMPercentage keeps the JVM heap within Render's free/starter instance memory
# (512MB-1GB), instead of the JVM guessing based on the host's full physical memory.
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-XX:+UseContainerSupport", "-jar", "app.jar"]
