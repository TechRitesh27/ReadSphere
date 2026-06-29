# Design Document — Multi-stage Dockerization (Requirement 1)

## Overview

This document covers the technical design for packaging **BookStoreSystem** as a lean, production-ready Docker image using a multi-stage build. The goal is a final image that contains only a minimal JRE and the application fat JAR, with no Maven tooling, source code, or intermediate build artifacts included.

The application is a pure in-memory Spring Boot 4.1 / Java 21 REST API with no external runtime dependencies (no database, no message broker). This makes the runtime image particularly simple: copy one JAR, expose one port, run one process.

### Key Design Decisions

| Decision | Choice | Rationale |
|---|---|---|
| Build base image | `maven:3.9-eclipse-temurin-21` | Official Maven image pre-bundled with Temurin JDK 21; no host tooling required |
| Runtime base image | `eclipse-temurin:21-jre-alpine` | Smallest official Temurin JRE 21 image; Alpine reduces final image size significantly |
| Non-root user strategy | Create a dedicated `appuser` (UID 1001) inside the runtime stage | Follows least-privilege principle; avoids writing a separate user-creation layer |
| `.dockerignore` | Exclude `target/`, `.git/`, `.idea/`, IDE configs | Keeps build context minimal; prevents stale local build artifacts from interfering |
| Maven dependency cache | Mount `/root/.m2` as a BuildKit cache mount | Avoids re-downloading dependencies on every build without persisting the cache in the image |

---

## Architecture

The build is split into exactly two stages, following the standard multi-stage pattern:

```
┌─────────────────────────────────────────┐
│  Stage 1: builder                        │
│  FROM maven:3.9-eclipse-temurin-21       │
│                                          │
│  1. Copy pom.xml + src/                  │
│  2. RUN mvn package -DskipTests          │
│     → produces target/                  │
│        BookStoreSystem-0.0.1-SNAPSHOT.jar│
└──────────────────┬──────────────────────┘
                   │  COPY --from=builder (JAR only)
┌──────────────────▼──────────────────────┐
│  Stage 2: runtime                        │
│  FROM eclipse-temurin:21-jre-alpine      │
│                                          │
│  1. Create non-root user (appuser:1001)  │
│  2. Copy JAR → /app/app.jar              │
│  3. EXPOSE 8080                          │
│  4. USER appuser                         │
│  5. ENTRYPOINT ["java", "-jar",          │
│                 "/app/app.jar"]          │
└─────────────────────────────────────────┘
```

The runtime stage never sees Maven, source files, or test output. Docker's layer isolation guarantees this at the build level.

---

## Components and Interfaces

### Dockerfile

Single file at the repository root. Contains both stages.

**Stage 1 — `builder`**

```dockerfile
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /build
# Copy dependency manifest first to exploit layer caching
COPY pom.xml .
# Download dependencies in a separate layer (cache-friendly)
RUN --mount=type=cache,target=/root/.m2 \
    mvn dependency:go-offline -q
# Copy source and build
COPY src/ src/
RUN --mount=type=cache,target=/root/.m2 \
    mvn package -DskipTests
```

- `dependency:go-offline` is run before copying source so the dependency download layer is re-used on incremental source changes.
- `--mount=type=cache,target=/root/.m2` uses BuildKit cache mounts to avoid re-downloading Maven artifacts while keeping them out of the final image. Requires BuildKit (enabled by default in Docker 23+).
- If `mvn package` exits non-zero, Docker stops immediately with a non-zero exit code — no special handling needed (Requirement 1.7 is satisfied by default shell semantics).

**Stage 2 — `runtime`**

```dockerfile
FROM eclipse-temurin:21-jre-alpine AS runtime
# Create non-root user
RUN addgroup -S appgroup && adduser -S -G appgroup -u 1001 appuser
WORKDIR /app
# Copy only the fat JAR from the build stage
COPY --from=builder /build/target/BookStoreSystem-0.0.1-SNAPSHOT.jar app.jar
# Expose application port
EXPOSE 8080
# Drop to non-root user
USER appuser
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
```

- `addgroup`/`adduser` are the Alpine equivalents of `groupadd`/`useradd`. Using `-S` creates a system user with no password/login shell.
- `COPY --from=builder` pulls only the named file; nothing else from Stage 1 is carried over.
- `EXPOSE 8080` is metadata only — actual port binding is done at `docker run -p 8080:8080`.
- `USER appuser` is the last configuration instruction before `ENTRYPOINT`; the JVM process runs as UID 1001.

### .dockerignore

```
target/
.git/
.github/
.idea/
.vscode/
.junie/
.kiro/
*.md
mvnw.cmd
```

Rationale:
- `target/` — prevents a locally-built JAR from leaking into the build context and potentially overriding the in-container build result. Also the largest directory; excluding it keeps context transfer fast.
- `.git/`, `.github/` — not needed inside the container; reduces context size.
- IDE folders (`.idea/`, `.vscode/`, `.junie/`, `.kiro/`) — developer tooling, irrelevant to the build.
- `*.md`, `mvnw.cmd` — documentation and Windows wrapper; not referenced by the Dockerfile.

`mvnw` is **not** excluded because the Maven image already has Maven installed and the Dockerfile uses `mvn` directly — the wrapper is simply never copied because the Dockerfile copies `pom.xml` and `src/` explicitly.

---

## Data Models

No application data models are affected by this feature. The Dockerfile is purely an operational artifact.

The only file paths that matter at build time:

| Path (inside builder stage) | Description |
|---|---|
| `/build/pom.xml` | Maven project descriptor |
| `/build/src/` | Application source tree |
| `/build/target/BookStoreSystem-0.0.1-SNAPSHOT.jar` | Fat JAR produced by `spring-boot-maven-plugin` |

| Path (inside runtime stage) | Description |
|---|---|
| `/app/app.jar` | The fat JAR, renamed for brevity |

---

## Error Handling

### Maven build failures (Requirement 1.7)

`RUN mvn package -DskipTests` executes in the default `set -e` shell context that Docker uses for `RUN` instructions. Any non-zero exit from Maven propagates as a build failure. No `|| true` or error suppression is used anywhere in the Dockerfile.

Consequence: if the Java source has a compile error, a missing dependency, or any other Maven failure, `docker build` exits with a non-zero code and no image is tagged.

### JVM startup failures

If the application fails to start (e.g., port already bound on the host), the container exits with a non-zero code. This is standard Docker/JVM behaviour and requires no special Dockerfile handling.

### Non-root user creation failures

The `addgroup`/`adduser` commands in the runtime stage will fail the build if they cannot create the user (e.g., UID conflict). This is intentional — it prevents silently running as root if user setup breaks.

---

## Correctness Properties

> **PBT Applicability Assessment**
>
> All seven acceptance criteria for Requirement 1 describe declarative Dockerfile configuration or one-shot integration checks (does the image build? does the container start? is the correct base image declared?). None of these involve a pure function whose behaviour varies meaningfully with arbitrary inputs. Running property-based tests with 100+ iterations would provide no additional confidence over 1–3 targeted checks. Therefore, the Correctness Properties section is omitted and testing relies entirely on smoke and integration tests, as described in the Testing Strategy below.

---

## Testing Strategy

Since PBT does not apply, verification is split across **smoke tests** (static checks on the Dockerfile and built image) and **integration tests** (actually building and running the image).

### Smoke Tests (static / fast)

These can be automated in CI without running a full Docker build.

| Test | Criterion | Method |
|---|---|---|
| Build stage uses official Maven + Java 21 image | 1.1 | Parse `Dockerfile`, assert first `FROM` matches `maven:*-eclipse-temurin-21` |
| Runtime stage uses Temurin JRE slim image | 1.2 | Parse `Dockerfile`, assert second `FROM` matches `eclipse-temurin:21-jre*` |
| Non-root `USER` instruction present | 1.5 | Parse `Dockerfile`, assert `USER` instruction exists with a non-root value |
| Maven build failure propagates | 1.7 | Introduce a deliberate syntax error in a source file, run `docker build`, assert exit code ≠ 0 |

### Integration Tests (require Docker)

These validate runtime behaviour and should run in CI on environments with Docker available.

| Test | Criterion | Method |
|---|---|---|
| Image builds without host tooling | 1.6 | Run `docker build` on a clean CI agent (only Docker installed), assert exit code 0 |
| Runtime stage contains only the JAR | 1.3 | `docker run --rm <image> ls /app` — assert only `app.jar` present; `docker run --rm <image> which mvn` — assert not found |
| Application starts and serves on port 8080 | 1.4 | `docker run -d -p 8080:8080 <image>`, poll `GET http://localhost:8080/actuator/health`, expect HTTP 200 |

### Manual Verification Checklist

For local development, the following commands are sufficient:

```bash
# Build
docker build -t bookstore:local .

# Verify image size (should be well under 300 MB)
docker images bookstore:local

# Verify no Maven in runtime
docker run --rm bookstore:local which mvn   # should return non-zero

# Verify only JAR in /app
docker run --rm bookstore:local ls -la /app

# Run and test health endpoint
docker run -d -p 8080:8080 --name bss bookstore:local
curl http://localhost:8080/actuator/health
docker stop bss && docker rm bss
```
