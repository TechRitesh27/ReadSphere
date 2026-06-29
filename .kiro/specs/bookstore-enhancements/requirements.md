# Requirements Document

## Introduction

This document captures requirements for three enhancements to the BookStoreSystem Spring Boot application:

1. **Multi-stage Dockerization** — package the application into a lean, production-ready Docker image using a multi-stage build that separates the Maven build stage from the runtime stage.
2. **Caching** — introduce an in-process cache for read-heavy service methods to reduce redundant in-memory iteration on the book store.
3. **Admin-only Delete** — protect the `DELETE /books/{id}` endpoint so that only callers authenticated as administrators can invoke it, while all other endpoints remain publicly accessible.

The system has no database; the book store is an in-memory `CopyOnWriteArrayList` seeded from a CSV file at startup.

---

## Glossary

- **BookStoreSystem**: The Spring Boot REST API application being enhanced.
- **Docker_Build_Stage**: The Maven container step that compiles the source and produces the fat JAR.
- **Docker_Runtime_Stage**: The minimal JRE container step that copies and runs the fat JAR.
- **Cache**: An in-process, Spring-managed cache that stores the results of expensive computations so repeated calls with the same parameters return a stored result without re-executing the computation.
- **Cache_Entry**: A single cached result keyed by the combination of method parameters.
- **Admin**: A user who has been granted the `ROLE_ADMIN` authority within the application's security configuration.
- **Security_Filter**: The Spring Security filter chain that intercepts HTTP requests and enforces access-control rules.
- **Credentials**: A username/password pair that identifies a caller.
- **HTTP_Basic_Auth**: The HTTP authentication scheme where credentials are sent as a Base64-encoded `Authorization` header.

---

## Requirements

### Requirement 1: Multi-stage Docker Build

**User Story:** As a developer, I want a multi-stage Dockerfile for BookStoreSystem, so that the final Docker image contains only the runtime JRE and the application JAR, keeping the image small and free of build tooling.

#### Acceptance Criteria

1. THE Docker_Build_Stage SHALL use an official Maven image with Java 21 to compile the project and produce the executable JAR via `mvn package -DskipTests`.
2. THE Docker_Runtime_Stage SHALL use an official Eclipse Temurin Java 21 JRE slim image as its base.
3. THE Docker_Runtime_Stage SHALL copy only the fat JAR produced by the Docker_Build_Stage, containing no Maven tooling, source files, or intermediate build artifacts.
4. WHEN the Docker image is started, THE BookStoreSystem SHALL start on port 8080 and expose that port.
5. THE Dockerfile SHALL declare a non-root user for running the application process to reduce the container attack surface.
6. WHEN `docker build` is executed against the Dockerfile, THE Docker_Build_Stage SHALL produce a successful build without requiring any pre-installed tooling on the host machine other than Docker itself.
7. IF the Maven build step fails, THEN THE Docker_Build_Stage SHALL terminate with a non-zero exit code, preventing the runtime image from being produced.

---

### Requirement 2: Response Caching for Read Endpoints

**User Story:** As a system operator, I want frequently-called read operations to be cached, so that repeated requests with identical parameters return a stored result instead of re-iterating the full in-memory book list.

#### Acceptance Criteria

1. THE BookStoreSystem SHALL enable Spring's cache abstraction via `@EnableCaching` so that cache annotations on service methods are honoured.
2. WHEN `BookService.getBookById` is called with an id that has been retrieved before, THE Cache SHALL return the stored `BooksResponseDTO` without invoking the underlying lookup logic again.
3. WHEN `ReportService.generateInventoryReport` is called more than once with no intervening mutations, THE Cache SHALL return the stored `InventoryReportDTO` without re-computing the report.
4. WHEN a book is added via `BookService.addBook`, THE Cache SHALL evict all entries associated with the book list and inventory report caches so that subsequent reads reflect the new state.
5. WHEN a book is updated via `BookService.updateBook` with a given id, THE Cache SHALL evict the cache entry for that id and all entries associated with the book list and inventory report caches.
6. WHEN a book is deleted via `BookService.deleteBook` with a given id, THE Cache SHALL evict the cache entry for that id and all entries associated with the book list and inventory report caches.
7. THE BookStoreSystem SHALL use the default Spring Boot in-process `ConcurrentMapCacheManager` (no external cache server required), configured through `application.properties`.
8. WHILE the application is running, THE Cache SHALL remain consistent with the in-memory book store such that a read immediately following a write returns the mutated state.

---

### Requirement 3: Admin-only Delete Endpoint

**User Story:** As a system administrator, I want the `DELETE /books/{id}` endpoint to be restricted to admin users only, so that unauthorised callers cannot remove books from the store.

#### Acceptance Criteria

1. THE BookStoreSystem SHALL include `spring-boot-starter-security` as a dependency to enable the Spring Security filter chain.
2. THE Security_Filter SHALL permit all HTTP requests to `GET /books`, `GET /books/{id}`, `POST /books`, `PUT /books/{id}`, and `GET /reports/inventory` without requiring authentication.
3. THE Security_Filter SHALL require callers to present valid Credentials with the `ROLE_ADMIN` authority to access `DELETE /books/{id}`.
4. WHEN a request to `DELETE /books/{id}` is received without any `Authorization` header, THE Security_Filter SHALL return HTTP 401 Unauthorized.
5. WHEN a request to `DELETE /books/{id}` is received with valid Credentials that do not carry `ROLE_ADMIN`, THE Security_Filter SHALL return HTTP 403 Forbidden.
6. WHEN a request to `DELETE /books/{id}` is received with valid Credentials that carry `ROLE_ADMIN`, THE Security_Filter SHALL forward the request to `BookController` and the deletion SHALL proceed normally.
7. THE BookStoreSystem SHALL define at least one admin user via in-memory `UserDetailsService` configured in `application.properties` (username, BCrypt-encoded password, and role), so that no external identity provider is required.
8. THE BookStoreSystem SHALL use HTTP_Basic_Auth as the authentication mechanism for the delete endpoint.
9. IF Spring Security's default session creation behaviour would interfere with stateless REST semantics, THEN THE Security_Filter SHALL be configured as stateless (no HTTP session created or used).
