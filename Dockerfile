# Multi-stage build optimized for Render free tier (256MB RAM)

# Stage 1: Build
FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /app

# Copy pom.xml first to leverage Docker cache for dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source and build
COPY src ./src
RUN mvn package -DskipTests -Dspring-boot.build-info.additional-properties.build.time= && \
    # Extract the layered jar for faster startup
    java -Djarmode=layertools -jar target/*.jar extract --destination extracted

# Stage 2: Runtime (minimal image)
FROM eclipse-temurin:17-jre-alpine

# Add non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

WORKDIR /app

# Copy layered application (faster startup)
COPY --from=build /app/extracted/dependencies/ ./
COPY --from=build /app/extracted/spring-boot-loader/ ./
COPY --from=build /app/extracted/snapshot-dependencies/ ./
COPY --from=build /app/extracted/application/ ./

# Expose port
EXPOSE 8080

# Health check for container orchestration
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# JVM options optimized for 256MB RAM limit on Render free tier
# -XX:MaxRAMPercentage=75 uses 75% of available RAM for heap
# -XX:+UseSerialGC is most memory-efficient for small heaps
# -Xss256k reduces thread stack size
# -XX:+TieredCompilation -XX:TieredStopAtLevel=1 for faster startup
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 \
    -XX:+UseSerialGC \
    -Xss256k \
    -XX:+TieredCompilation \
    -XX:TieredStopAtLevel=1 \
    -Djava.security.egd=file:/dev/./urandom"

# Use the Spring Boot layered launcher for optimal startup
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher"]
