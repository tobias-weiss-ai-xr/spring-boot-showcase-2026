# modules Specification

## Purpose
CropGuard is a demo application whose structure maps 1:1 to the modules of the Spring Boot course at courses.graphwiz.ai/springboot, so each course module is demonstrable in the codebase. This spec captures the course-module coverage contract.

## Requirements

### Requirement: Module 1 — Spring Boot foundations
The application SHALL provide a runnable entry point (`@SpringBootApplication`) with a `spring-boot-maven-plugin`-built executable.

#### Scenario: Application starts
- **WHEN** `mvn spring-boot:run` is executed
- **THEN** the app boots on port 8080 with an embedded web server

### Requirement: Module 2 — Spring Core / Dependency Injection
All services and controllers SHALL use constructor injection; field injection is not used.

#### Scenario: Constructor-injected components
- **WHEN** any service or controller is inspected
- **THEN** its dependencies are injected via constructor parameters

### Requirement: Module 3 — REST APIs
Controllers SHALL be annotated with `@RestController`, `@RequestMapping`, and HTTP mapping annotations, returning correct status codes (201 Created, 200 OK, 404 Not Found, 400 Bad Request).

#### Scenario: REST endpoint conventions
- **WHEN** a resource is created via POST
- **THEN** the endpoint returns 201 Created with the created resource

### Requirement: Module 4 — Bean Validation
DTOs SHALL be Java records with Bean Validation annotations (`@NotBlank`, `@NotNull`, `@Min`, `@Email`) and controller parameters SHALL be validated with `@Valid`.

#### Scenario: Invalid DTO rejected
- **WHEN** a request body violates a validation constraint
- **THEN** the API responds with 400 Bad Request listing the constraint violations

### Requirement: Module 5 — Spring Data JPA
Entities SHALL use JPA annotations (`@Entity`, `@Table`, `@Column`, `@ManyToOne`, `@OneToMany`) and repositories SHALL extend `JpaRepository` with derived query methods.

#### Scenario: Derived query execution
- **WHEN** a repository's derived query method is invoked
- **THEN** the matching entities are returned from the database

### Requirement: Module 6 — Error Handling
A `@RestControllerAdvice` SHALL return RFC 7807 `ProblemDetail` for errors: `ResourceNotFoundException` → 404, `BusinessException` → 400, `MethodArgumentNotValidException` → 400.

#### Scenario: Not-found resource
- **WHEN** a client requests a non-existent resource
- **THEN** the API returns a 404 ProblemDetail body

### Requirement: Module 7 — Testing
The test suite SHALL cover the pyramid: `@SpringBootTest` smoke test, `@WebMvcTest` and `@DataJpaTest` slice tests, and Mockito unit tests, including role-based access (FARMER vs ASSESSOR).

#### Scenario: Full test suite
- **WHEN** `mvn test` is executed
- **THEN** unit, slice, and integration tests pass

### Requirement: Module 8 — Security
Authentication SHALL be JWT-based and stateless: `SecurityConfig` uses BCrypt and method security (`@PreAuthorize`), `JwtAuthFilter` extracts Bearer tokens, FARMERs file claims and ASSESSORs assess claims.

#### Scenario: Role-restricted assessment
- **WHEN** a user without the ASSESSOR role calls the assess endpoint
- **THEN** the API responds 403 Forbidden

### Requirement: Module 9 — Configuration & Profiles
Configuration SHALL use an `AppProperties` record with `@ConfigurationProperties`, plus `application.yml` and `application-dev.yml`; the dev profile SHALL enable SQL logging at DEBUG.

#### Scenario: Dev profile logging
- **WHEN** the `dev` profile is active
- **THEN** SQL statements are logged at DEBUG level

### Requirement: Module 10 — Actuator & Monitoring
A custom `DatabaseHealthIndicator` SHALL report database health, and actuator endpoints (health, info, metrics) SHALL be exposed with `show-details: always`.

#### Scenario: Health endpoint
- **WHEN** `GET /actuator/health` is called
- **THEN** it reports UP including the database component detail
