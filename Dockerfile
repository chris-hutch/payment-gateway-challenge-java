# ---- Build stage ----
FROM gradle:8.5-jdk17 AS build

WORKDIR /app

COPY build.gradle settings.gradle ./

COPY gradle gradle

RUN gradle dependencies --no-daemon || true

COPY src src

RUN gradle bootJar --no-daemon

# ---- Runtime stage ----
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

RUN groupadd -r gateway && useradd -r -g gateway gateway
USER gateway

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8090

HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
    CMD wget -qO- http://localhost:8090/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]