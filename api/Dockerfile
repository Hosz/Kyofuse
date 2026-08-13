# syntax=docker/dockerfile:1

# ---------------------------------------------------------------------------
# Stage 1 — build
# Compiles and packages the application using the official Maven image
# (Temurin JDK 21). This stage is discarded after the build, so its size
# (JDK + Maven + downloaded dependencies) never reaches the runtime image.
# ---------------------------------------------------------------------------
FROM maven:3.9-eclipse-temurin-21-alpine AS build

WORKDIR /build

# Copy only the POM first so the dependency layer is cached independently
# from source changes. As long as pom.xml doesn't change, Docker reuses this
# layer and skips re-downloading the dependency tree on every rebuild.
COPY pom.xml .
RUN mvn -B dependency:go-offline

# Now copy the sources and build the executable jar. Tests are skipped here
# on purpose: they already run in CI / locally via `./mvnw test`, and
# skipping them keeps `docker compose up --build` fast for day-to-day work.
COPY src ./src
RUN mvn -B clean package -DskipTests

# ---------------------------------------------------------------------------
# Stage 2 — runtime
# JRE-only Alpine image (no JDK, no Maven, no build cache): the smallest
# reasonable footprint for actually running the application.
# ---------------------------------------------------------------------------
FROM eclipse-temurin:21-jre-alpine AS runtime

# Dedicated non-root user (security best practice: never run the JVM as root).
RUN addgroup -S kyofuse && adduser -S kyofuse -G kyofuse

WORKDIR /app

COPY --from=build --chown=kyofuse:kyofuse /build/target/*.jar app.jar

USER kyofuse

EXPOSE 8080

# JVM memory/GC tuning is intentionally NOT hardcoded here — it's injected at
# runtime via the JAVA_TOOL_OPTIONS environment variable (see docker-compose.yml),
# so it can be adjusted per environment without rebuilding the image.
ENTRYPOINT ["java", "-jar", "app.jar"]
