# ===== Stage 1: Build with Maven =====
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace

# Copy pom and download dependencies first (cache)
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 mvn -q -e -DskipTests dependency:go-offline

# Copy source and build
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -q -e -DskipTests package

# ===== Stage 2: Run with JRE =====
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/target/*.jar app.jar

# Default port
EXPOSE 8080

# JVM opts and Spring profiles configurable at runtime
ENV JAVA_OPTS="-XX:+UseZGC -XX:MaxRAMPercentage=75.0" \
    SPRING_PROFILES_ACTIVE=default

ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar app.jar"]


