# Sprint 1 Testing Log - 09/15/2025

This log details the attempts, problems, and solutions for creating and running the initial JUnit tests for the repository layer.

## Attempt 1: Initial Test Execution

*   **Attempt**: After creating the initial set of JUnit integration tests for the repositories, the first test run was executed using `mvnw test`.
*   **Problem**: The tests failed with two main errors:
    1.  `java.lang.IllegalStateException: Unable to find a @SpringBootConfiguration...` in `GoldenBridgeApplicationTests`.
    2.  `org.postgresql.util.PSQLException: ERROR: null value in column "created_at" of relation "users" violates not-null constraint` in all repository tests.
*   **Solution**:
    1.  The `GoldenBridgeApplicationTests` class was in the wrong package (`com.goldenbridge.golden_bridge`) and could not find the main application configuration. It was moved to the correct package (`com.goldenbridge.app`).
    2.  The `created_at` column was not being populated because JPA auditing was not enabled for the test context. A new `TestJpaConfig` class with the `@EnableJpaAuditing` annotation was created and imported into each test class.

## Attempt 2: Post-Fix Test Execution

*   **Attempt**: After fixing the initial issues, the tests were run again.
*   **Problem**: The build failed with compilation errors. The errors indicated syntax issues in the test files, specifically `class, interface, enum, or record expected`. This was caused by incorrect placement of the `@Import` annotation.
*   **Solution**: All the repository test files were corrected to fix the syntax errors.

## Attempt 3: Post-Syntax-Fix Test Execution

*   **Attempt**: With the syntax errors fixed, the tests were executed again.
*   **Problem**: The tests failed with a `org.springframework.beans.factory.support.BeanDefinitionOverrideException` for the `jpaAuditingHandler` bean. This was because the `@EnableJpaAuditing` annotation was present in both the main application configuration (`JpaConfig`) and the test configuration (`TestJpaConfig`), causing a conflict.
*   **Solution**: The `@EnableJpaAuditing` annotation was removed from the main `JpaConfig` class, leaving it only in the test configuration.

## Attempt 4: Final Test Execution

*   **Attempt**: After resolving the bean override issue, the tests were run one last time.
*   **Problem**: None.
*   **Solution**: All 11 tests passed successfully.

## JWT Authentication Implementation & Testing - 09/16/2025

This section details the implementation of JWT authentication, including encountered errors and their resolutions.

### Implementation Steps:

1.  **Added JJWT Dependencies:** Added `jjwt-api`, `jjwt-impl`, and `jjwt-jackson` to `pom.xml`.
2.  **Created Security Components:**
    *   `UserDetailsServiceImpl`: Implements `UserDetailsService` to load user data.
    *   `JwtTokenProvider`: Utility for JWT generation and validation.
    *   `JwtAuthenticationFilter`: Intercepts requests to validate JWTs.
    *   `SecurityConfig`: Configures Spring Security, including `PasswordEncoder`, `AuthenticationManager`, and `SecurityFilterChain`.
    *   `AuthController`: Provides `/api/auth/login` endpoint for token issuance and `/api/auth/protected` for testing.
    *   `AuthRequest` and `AuthResponse` DTOs.
3.  **Flyway Migration for Test User:** Created `V2__add_test_user.sql` to insert a `testuser` with a BCrypt-hashed password into the database.

### Encountered Problems and Solutions:

1.  **Problem: `pom.xml` Syntax Error (Malformed XML)**
    *   **Details**: After manually adding JJWT dependencies, the `pom.xml` became invalid due to extra characters and incorrect indentation.
    *   **Solution**: Corrected the XML syntax by replacing the malformed dependency block with clean, properly formatted XML.

2.  **Problem: `cannot find symbol method parserBuilder()` (JJWT Compilation Error)**
    *   **Details**: The application failed to compile, indicating `parserBuilder()` was not found in `io.jsonwebtoken.Jwts`. This suggested a version mismatch or dependency resolution issue.
    *   **Solution**: Downgraded JJWT version from `0.12.3` to `0.11.5` in `pom.xml` by defining a `<jjwt.version>` property. This resolved the compilation error.

3.  **Problem: `duplicate key value violates unique constraint "users_username_key"` (Test Failures)**
    *   **Details**: After adding `V2__add_test_user.sql`, repository tests (`UserRepositoryTest`, `UserPreferencesRepositoryTest`, `GarminCredentialsRepositoryTest`) failed because their `@BeforeEach` methods tried to insert a user with the same username (`testuser`) that Flyway had already inserted.
    *   **Solution**: Modified the `setUp()` methods in these test classes to use unique usernames (e.g., `testuser_repo_test`, `testuser_prefs_test`, `testuser_garmin_test`) for their test data, preventing conflicts.

4.  **Problem: `duplicate key value violates unique constraint "users_email_key"` (Test Failures)**
    *   **Details**: Following the username fix, `UserRepositoryTest` failed due to a duplicate email (`test@example.com`), as the `email` column also has a unique constraint.
    *   **Solution**: Modified `UserRepositoryTest` to use unique email addresses (e.g., `test_repo_test@example.com`, `test2_repo_test@example.com`) for its test users.

5.  **Problem: `cannot find symbol class GetMapping` (Compilation Error)**
    *   **Details**: After adding the `/api/auth/protected` endpoint with `@GetMapping`, the compiler reported `GetMapping` as an unknown symbol.
    *   **Solution**: Added the missing `import org.springframework.web.bind.annotation.GetMapping;` statement to `AuthController.java`.

6.  **Problem: `HTTP/1.1 403 Forbidden` on Protected Endpoint (Runtime Error)**
    *   **Details**: Even with a valid token, requests to `/api/auth/protected` were denied. Initial investigation showed `SecurityConfig` was permitting all `/api/auth/**` endpoints.
    *   **Solution 1 (Partial)**: Modified `SecurityConfig` to specifically permit only `/api/auth/login` and require authentication for `anyRequest()`.
    *   **Solution 2 (Final)**: The `403` persisted. It was identified that `UserDetailsServiceImpl` was returning `Collections.emptyList()` for user authorities. Modified `UserDetailsServiceImpl` to return `Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))` to provide a default role. Explicitly added `DaoAuthenticationProvider` to `SecurityConfig` to ensure proper `AuthenticationManager` configuration.

### Final Verification:

*   All Maven `clean install` operations now complete successfully, with all tests passing.
*   Manual `curl` commands confirm:
    *   `POST /api/auth/login` successfully returns a JWT token.
    *   `GET /api/auth/protected` with a valid JWT token successfully returns "You have accessed a protected resource!", confirming proper authentication and authorization.

## Python Script Execution Environment Setup - 09/16/2025

This section details the setup of the Python script execution environment, including encountered errors and their resolutions.

### Implementation Steps:

1.  **Created Python Script:** `python-scripts/hello.py` to print a greeting.
2.  **Configured Docker Compose:** Confirmed `python-env` service in `docker-compose.yml` mounts `python-scripts` volume.
3.  **Created Java Service:** `PythonScriptService.java` to execute Python scripts using `docker-compose exec`.
4.  **Created Java Controller:** `PythonController.java` to expose a protected REST endpoint `/api/python/hello`.

### Encountered Problems and Solutions:

1.  **Problem: Python script execution failed (Empty `curl` response, `exit code 1` in Java logs)**
    *   **Details**: Initial attempts to execute the Python script via `PythonScriptService` resulted in an empty response from the `curl` command and `Python script execution failed with exit code: 1` in the Java application logs. Direct `docker-compose exec golden-bridge-python python /app/python-scripts/hello.py Gemini` also failed with `exit code 1` and no output.
    *   **Attempts & Debugging**: Tried `python --version`, `python3 --version`, `sh`, `bash` inside the container via `docker-compose exec`, all failing with `exit code 1` and no output. Confirmed containers were `Up` using `docker-compose ps`. Suspected PATH issues or `docker-compose exec` limitations.
    *   **Solution 1 (Partial)**: Identified that `python` and `python3` were not in the default PATH for non-interactive shells. Modified `PythonScriptService.java` to use the full path `/usr/local/bin/python3` for the interpreter.
    *   **Solution 2 (Partial)**: Added `sys.stdout.flush()` to `python-scripts/hello.py` to ensure immediate output flushing.
    *   **Solution 3 (Final)**: The issue persisted. Realized `Runtime.getRuntime().exec()` has limitations with complex commands and process I/O. Refactored `PythonScriptService.java` to use `ProcessBuilder` for more robust command execution and output capture, including `processBuilder.redirectErrorStream(true)`.

### Final Verification:

*   Manual `curl` command to `GET /api/python/hello?name=Gemini` with a valid JWT token now successfully returns "Hello, Gemini from Python!".

## Test Coverage Improvement - 09/16/2025

This section details efforts to improve test coverage, specifically for the `JwtTokenProvider` class.

### Implementation Steps:

1.  **Created Unit Test Class:** `src/test/java/com/goldenbridge/app/security/JwtTokenProviderTest.java`.
2.  **Added Tests for `JwtTokenProvider`:** Included tests for `generateToken`, `getUsernameFromToken`, and `validateToken` methods.

### Encountered Problems and Solutions:

1.  **Problem: `WeakKeyException` in `JwtTokenProviderTest`**
    *   **Details**: Tests in `JwtTokenProviderTest` failed with `io.jsonwebtoken.security.WeakKeyException: The signing key's size is 400 bits which is not secure enough for the HS512 algorithm.` This occurred because the `testSecret` defined in the test class was too short (50 characters) for the HS512 algorithm, which requires a key of at least 512 bits (64 bytes).
    *   **Solution**: Modified `JwtTokenProviderTest.java` to use a longer `testSecret` string (at least 64 characters).

2.  **Problem: `ExpiredJwtException` in `JwtTokenProviderTest`**
    *   **Details**: After fixing the `WeakKeyException`, the `validateToken_shouldReturnFalseForExpiredToken` test failed with `io.jsonwebtoken.ExpiredJwtException`. This happened because the `Jwts.parserBuilder()` was throwing an exception when encountering an expired token, rather than `validateToken` returning `false` gracefully.
    *   **Solution**: Modified `JwtTokenProvider.java` to wrap the token parsing/validation logic in `validateToken` (and `getUsernameFromToken`) with `try-catch` blocks to handle `ExpiredJwtException`, `SignatureException`, `MalformedJwtException`, `UnsupportedJwtException`, and `IllegalArgumentException` gracefully. This ensures `validateToken` returns `false` for invalid/expired tokens without throwing exceptions.

3.  **Problem: `cannot find symbol` errors in `JwtTokenProvider.java` (Compilation Error)**
    *   **Details**: After adding `try-catch` blocks and logger, the compiler reported numerous `cannot find symbol` errors for classes like `Component`, `Key`, `UserDetails`, `Value`, `Jwts`, `Claims`, `Date`, `SignatureAlgorithm`, `Keys`, `Function`, `Logger`, `LoggerFactory`, and various JWT exception types. This was due to missing import statements.
    *   **Solution**: Added all necessary import statements to the top of `JwtTokenProvider.java`.

### Final Verification:

*   All Maven `clean install` operations now complete successfully, with all tests passing.
*   Overall test coverage increased to 46%.