# Stage 1 — builder
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /build

COPY pom.xml .


RUN --mount=type=cache,target=/root/.m2 \
    mvn dependency:go-offline -q

COPY src/ src/

RUN --mount=type=cache,target=/root/.m2 \
    mvn package -DskipTests

# Stage 2 — runtime
FROM eclipse-temurin:21-jre-alpine AS runtime

RUN addgroup -S appgroup && \
    adduser -S -G appgroup -u 1001 appuser

WORKDIR /app

COPY --from=builder /build/target/BookStoreSystem-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

USER appuser

# Start the application.
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
