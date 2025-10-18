package com.example.kitchenapi;

import com.example.kitchenapi.dto.AuthDto;
import com.example.kitchenapi.dto.PantryDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for Kitchen API using Testcontainers.
 * Tests the complete flow: signup -> login -> authenticated API call.
 *
 * Based on spec.md section 8 (Testing with Testcontainers).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
class KitchenApiIntegrationIT {

    /**
     * PostgreSQL container for integration testing.
     * Uses postgres:16-alpine for lightweight testing.
     */
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("kitchen_test")
            .withUsername("test")
            .withPassword("test");

    /**
     * Inject Spring DataSource properties from the running container.
     * This configures the test application to connect to the Testcontainers PostgreSQL instance.
     */
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private TestRestTemplate restTemplate;

    /**
     * Smoke test: Complete authentication and API access flow.
     *
     * This test verifies:
     * 1. User can sign up successfully
     * 2. User can log in and receive a JWT token
     * 3. User can access authenticated endpoints with the JWT token
     * 4. Pantry item creation returns 201 Created
     *
     * Flow: POST /auth/signup -> POST /auth/login -> POST /pantry (with JWT)
     */
    @Test
    void smokeTest_signupLoginAndCreatePantryItem() {
        // Given: Prepare signup request
        String testEmail = "test@example.com";
        String testName = "Test User";
        String testPassword = "password123";

        AuthDto.SignupRequest signupRequest = new AuthDto.SignupRequest(
                testEmail,
                testName,
                testPassword
        );

        // When: Sign up a new user
        ResponseEntity<AuthDto.UserView> signupResponse = restTemplate.postForEntity(
                "/auth/signup",
                signupRequest,
                AuthDto.UserView.class
        );

        // Then: Signup should succeed
        assertThat(signupResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(signupResponse.getBody()).isNotNull();
        assertThat(signupResponse.getBody().email()).isEqualTo(testEmail);
        assertThat(signupResponse.getBody().name()).isEqualTo(testName);
        assertThat(signupResponse.getBody().id()).isNotNull();

        // Given: Prepare login request
        AuthDto.LoginRequest loginRequest = new AuthDto.LoginRequest(
                testEmail,
                testPassword
        );

        // When: Login with the created user
        ResponseEntity<AuthDto.LoginResponse> loginResponse = restTemplate.postForEntity(
                "/auth/login",
                loginRequest,
                AuthDto.LoginResponse.class
        );

        // Then: Login should succeed and return JWT token
        assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(loginResponse.getBody()).isNotNull();
        assertThat(loginResponse.getBody().token()).isNotBlank();
        assertThat(loginResponse.getBody().userId()).isEqualTo(signupResponse.getBody().id());
        assertThat(loginResponse.getBody().email()).isEqualTo(testEmail);
        assertThat(loginResponse.getBody().name()).isEqualTo(testName);

        String jwtToken = loginResponse.getBody().token();

        // Given: Prepare pantry item creation request with JWT token
        PantryDto.CreateRequest pantryRequest = new PantryDto.CreateRequest(
                "onion",
                "1個",
                LocalDate.now().plusDays(7)
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);
        HttpEntity<PantryDto.CreateRequest> requestEntity = new HttpEntity<>(pantryRequest, headers);

        // When: Create pantry item with authenticated request
        ResponseEntity<PantryDto.PantryView> pantryResponse = restTemplate.exchange(
                "/pantry",
                HttpMethod.POST,
                requestEntity,
                PantryDto.PantryView.class
        );

        // Then: Pantry item creation should succeed with 201 Created
        assertThat(pantryResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(pantryResponse.getBody()).isNotNull();
        assertThat(pantryResponse.getBody().id()).isNotNull();
        assertThat(pantryResponse.getBody().ingredientName()).isEqualTo("onion");
        assertThat(pantryResponse.getBody().amount()).isEqualTo("1個");
        assertThat(pantryResponse.getBody().expiresOn()).isEqualTo(LocalDate.now().plusDays(7));
    }

    /**
     * Test that authenticated endpoints reject requests without JWT token.
     * Note: Spring Security returns 403 FORBIDDEN when no authentication is provided
     * for protected endpoints (as opposed to 401 UNAUTHORIZED for invalid credentials).
     */
    @Test
    void shouldReturn403_whenAccessingPantryWithoutToken() {
        // Given: Pantry request without authentication
        PantryDto.CreateRequest pantryRequest = new PantryDto.CreateRequest(
                "onion",
                "1個",
                LocalDate.now().plusDays(7)
        );

        // When: Attempt to create pantry item without JWT token
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/pantry",
                pantryRequest,
                String.class
        );

        // Then: Should receive 403 Forbidden (Spring Security default for anonymous access to protected endpoints)
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    /**
     * Test that login fails with incorrect credentials.
     */
    @Test
    void shouldReturn401_whenLoginWithInvalidCredentials() {
        // Given: Invalid login credentials
        AuthDto.LoginRequest loginRequest = new AuthDto.LoginRequest(
                "nonexistent@example.com",
                "wrongpassword"
        );

        // When: Attempt to login with invalid credentials
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/auth/login",
                loginRequest,
                String.class
        );

        // Then: Should receive 401 Unauthorized
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    /**
     * Test that signup fails when email already exists.
     */
    @Test
    void shouldReturn409_whenSignupWithDuplicateEmail() {
        // Given: Create first user
        String email = "duplicate@example.com";
        AuthDto.SignupRequest firstSignup = new AuthDto.SignupRequest(
                email,
                "First User",
                "password123"
        );
        restTemplate.postForEntity("/auth/signup", firstSignup, AuthDto.UserView.class);

        // When: Attempt to create second user with same email
        AuthDto.SignupRequest secondSignup = new AuthDto.SignupRequest(
                email,
                "Second User",
                "password456"
        );
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/auth/signup",
                secondSignup,
                String.class
        );

        // Then: Should receive 409 Conflict
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }
}
