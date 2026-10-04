# Module Coverage Spec

## Requirement: Module 2 — Spring Core / DI
All services use constructor injection. No field injection.

## Requirement: Module 3 — REST APIs
All controllers use @RestController, @RequestMapping, @GetMapping, @PostMapping, @PutMapping.
Response entities use correct HTTP status codes (201 Created, 200 OK, 404, 400).

## Requirement: Module 4 — Validation
DTOs are Java records with Bean Validation annotations (@NotBlank, @NotNull, @Min, @Email).
@Valid on controller method parameters triggers validation.

## Requirement: Module 5 — Spring Data JPA
Entities use JPA annotations (@Entity, @Table, @Column, @ManyToOne, @OneToMany).
Repositories extend JpaRepository with derived query methods.

## Requirement: Module 6 — Error Handling
GlobalExceptionHandler with @RestControllerAdvice returns RFC 7807 ProblemDetail.
ResourceNotFoundException → 404, BusinessException → 400, MethodArgumentNotValidException → 400.

## Requirement: Module 7 — Testing
Test pyramid: @SpringBootTest smoke test, @WebMvcTest slice tests, @DataJpaTest slice test,
unit tests with Mockito. Security tests verify role-based access (FARMER vs ASSESSOR).

## Requirement: Module 8 — Security
JWT-based stateless auth. SecurityConfig with BCrypt, method security (@PreAuthorize).
FARMER role files claims; ASSESSOR role assesses claims. JwtAuthFilter extracts Bearer token.

## Requirement: Module 9 — Configuration & Profiles
AppProperties record with @ConfigurationProperties. application.yml + application-dev.yml.
Profile-based logging (dev: SQL logging, DEBUG).

## Requirement: Module 10 — Actuator & Monitoring
DatabaseHealthIndicator implements HealthIndicator. Actuator endpoints exposed: health, info, metrics.
show-details: always.
