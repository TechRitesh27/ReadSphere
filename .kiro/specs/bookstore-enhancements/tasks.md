# Tasks — Multi-stage Dockerization (Requirement 1)

## Implementation Plan

Scoped to Requirement 1 only. All tasks produce the `Dockerfile` and `.dockerignore` at the repository root, with no changes to application source code.

---

- [x] 1. Create `.dockerignore`
  - Create `.dockerignore` at the repository root
  - Exclude `target/`, `.git/`, `.github/`, `.idea/`, `.vscode/`, `.junie/`, `.kiro/`, `*.md`, `mvnw.cmd`
  - Verify the file does NOT exclude `mvnw`, `pom.xml`, or `src/`
  - **Acceptance**: `.dockerignore` exists at repo root with all specified exclusions

- [x] 2. Create the multi-stage `Dockerfile`
  - [x] 2.1 Write the builder stage
    - Add `FROM maven:3.9-eclipse-temurin-21 AS builder`
    - Set `WORKDIR /build`
    - `COPY pom.xml .`
    - Add `RUN --mount=type=cache,target=/root/.m2 mvn dependency:go-offline -q`
    - `COPY src/ src/`
    - Add `RUN --mount=type=cache,target=/root/.m2 mvn package -DskipTests`
    - **Acceptance**: Stage compiles the project and produces `target/BookStoreSystem-0.0.1-SNAPSHOT.jar` inside the builder container
  - [x] 2.2 Write the runtime stage
    - Add `FROM eclipse-temurin:21-jre-alpine AS runtime`
    - Add `RUN addgroup -S appgroup && adduser -S -G appgroup -u 1001 appuser` to create the non-root user
    - Set `WORKDIR /app`
    - Add `COPY --from=builder /build/target/BookStoreSystem-0.0.1-SNAPSHOT.jar app.jar`
    - Add `EXPOSE 8080`
    - Add `USER appuser`
    - Add `ENTRYPOINT ["java", "-jar", "/app/app.jar"]`
    - **Acceptance**: Runtime stage image contains only `app.jar` under `/app`; `mvn` is not present; process runs as UID 1001

- [x] 3. Verify the Docker build end-to-end
  - Run `docker build -t bookstore:local .` from the repository root
  - Confirm exit code is 0 and image is tagged
  - Run `docker run --rm bookstore:local ls -la /app` — assert only `app.jar` is present
  - Run `docker run --rm bookstore:local which mvn` — assert command not found (non-zero exit)
  - Run `docker run -d -p 8080:8080 --name bss bookstore:local`, then `curl http://localhost:8080/actuator/health`, expect HTTP 200 JSON response
  - Clean up: `docker stop bss && docker rm bss`
  - **Acceptance**: All verification commands pass; application is reachable on port 8080

- [x] 4. Verify non-root user constraint
  - Run `docker run --rm bookstore:local id`
  - Confirm output shows `uid=1001(appuser)` — not `uid=0(root)`
  - **Acceptance**: Container process runs as UID 1001, not root

- [x] 5. Verify Maven build-failure propagation
  - Temporarily introduce a compile error in any `.java` source file (e.g., add `INVALID_SYNTAX;` to `BookStoreSystemApplication.java`)
  - Run `docker build -t bookstore:fail-test .`
  - Confirm `docker build` exits with a non-zero code and no image is produced
  - Revert the temporary change
  - **Acceptance**: Docker build terminates non-zero when Maven compilation fails; runtime image is not created
