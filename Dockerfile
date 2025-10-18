# syntax=docker/dockerfile:1

# ---------- Build stage: compile the Spring Boot app ----------
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app

# Leverage build cache for dependencies
COPY pom.xml ./
RUN --mount=type=cache,target=/root/.m2 mvn -q -DskipTests dependency:go-offline

# Copy sources and build
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -q -DskipTests clean package

# ---------- Runtime stage: slim JRE image ----------
FROM eclipse-temurin:17-jre-alpine

ENV APP_HOME=/app \
    JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+UseStringDeduplication -Djava.security.egd=file:/dev/./urandom"

WORKDIR ${APP_HOME}

# Create non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring \
    && mkdir -p ${APP_HOME}/uploads \
    && chown -R spring:spring ${APP_HOME}

# Copy the fat jar from the build stage
COPY --from=build /app/target/*.jar app.jar

USER spring

# Spring Boot default HTTP port
EXPOSE 8080

# Healthcheck (optional) – can be overridden at runtime
# HEALTHCHECK --interval=30s --timeout=3s --start-period=30s CMD wget -qO- http://localhost:8080/actuator/health || exit 1

# Allow passing extra JVM args via JAVA_OPTS
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar app.jar"]
