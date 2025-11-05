package com.example.kitchenapi.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AuthDto 単体テスト")
class AuthDtoTest {

    @Test
    @DisplayName("AuthDto - コンストラクタが動作する")
    void authDto_ConstructorWorks() {
        // When
        AuthDto authDto = new AuthDto();

        // Then
        assertThat(authDto).isNotNull();
    }

    @Test
    @DisplayName("SignupRequest - 正しくインスタンス化できる")
    void signupRequest_CanBeInstantiated() {
        // Given
        String email = "test@example.com";
        String name = "Test User";
        String password = "password123";

        // When
        AuthDto.SignupRequest request = new AuthDto.SignupRequest(email, name, password);

        // Then
        assertThat(request).isNotNull();
        assertThat(request.email()).isEqualTo(email);
        assertThat(request.name()).isEqualTo(name);
        assertThat(request.password()).isEqualTo(password);
    }

    @Test
    @DisplayName("SignupRequest - equals()とhashCode()が正しく動作する")
    void signupRequest_EqualsAndHashCode() {
        // Given
        AuthDto.SignupRequest request1 = new AuthDto.SignupRequest("test@example.com", "Test User", "password123");
        AuthDto.SignupRequest request2 = new AuthDto.SignupRequest("test@example.com", "Test User", "password123");
        AuthDto.SignupRequest request3 = new AuthDto.SignupRequest("other@example.com", "Other User", "password456");

        // Then
        assertThat(request1).isEqualTo(request2);
        assertThat(request1).isNotEqualTo(request3);
        assertThat(request1.hashCode()).isEqualTo(request2.hashCode());
    }

    @Test
    @DisplayName("SignupRequest - toString()が動作する")
    void signupRequest_ToString() {
        // Given
        AuthDto.SignupRequest request = new AuthDto.SignupRequest("test@example.com", "Test User", "password123");

        // When
        String result = request.toString();

        // Then
        assertThat(result).contains("test@example.com");
        assertThat(result).contains("Test User");
    }

    @Test
    @DisplayName("LoginRequest - 正しくインスタンス化できる")
    void loginRequest_CanBeInstantiated() {
        // Given
        String email = "test@example.com";
        String password = "password123";

        // When
        AuthDto.LoginRequest request = new AuthDto.LoginRequest(email, password);

        // Then
        assertThat(request).isNotNull();
        assertThat(request.email()).isEqualTo(email);
        assertThat(request.password()).isEqualTo(password);
    }

    @Test
    @DisplayName("LoginRequest - equals()とhashCode()が正しく動作する")
    void loginRequest_EqualsAndHashCode() {
        // Given
        AuthDto.LoginRequest request1 = new AuthDto.LoginRequest("test@example.com", "password123");
        AuthDto.LoginRequest request2 = new AuthDto.LoginRequest("test@example.com", "password123");
        AuthDto.LoginRequest request3 = new AuthDto.LoginRequest("other@example.com", "password456");

        // Then
        assertThat(request1).isEqualTo(request2);
        assertThat(request1).isNotEqualTo(request3);
        assertThat(request1.hashCode()).isEqualTo(request2.hashCode());
    }

    @Test
    @DisplayName("LoginRequest - toString()が動作する")
    void loginRequest_ToString() {
        // Given
        AuthDto.LoginRequest request = new AuthDto.LoginRequest("test@example.com", "password123");

        // When
        String result = request.toString();

        // Then
        assertThat(result).contains("test@example.com");
    }

    @Test
    @DisplayName("LoginResponse - 正しくインスタンス化できる")
    void loginResponse_CanBeInstantiated() {
        // Given
        String token = "jwt-token-123";
        Long userId = 1L;
        String email = "test@example.com";
        String name = "Test User";

        // When
        AuthDto.LoginResponse response = new AuthDto.LoginResponse(token, userId, email, name);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.token()).isEqualTo(token);
        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.email()).isEqualTo(email);
        assertThat(response.name()).isEqualTo(name);
    }

    @Test
    @DisplayName("LoginResponse - equals()とhashCode()が正しく動作する")
    void loginResponse_EqualsAndHashCode() {
        // Given
        AuthDto.LoginResponse response1 = new AuthDto.LoginResponse("token123", 1L, "test@example.com", "Test User");
        AuthDto.LoginResponse response2 = new AuthDto.LoginResponse("token123", 1L, "test@example.com", "Test User");
        AuthDto.LoginResponse response3 = new AuthDto.LoginResponse("token456", 2L, "other@example.com", "Other User");

        // Then
        assertThat(response1).isEqualTo(response2);
        assertThat(response1).isNotEqualTo(response3);
        assertThat(response1.hashCode()).isEqualTo(response2.hashCode());
    }

    @Test
    @DisplayName("LoginResponse - toString()が動作する")
    void loginResponse_ToString() {
        // Given
        AuthDto.LoginResponse response = new AuthDto.LoginResponse("token123", 1L, "test@example.com", "Test User");

        // When
        String result = response.toString();

        // Then
        assertThat(result).contains("token123");
        assertThat(result).contains("test@example.com");
        assertThat(result).contains("Test User");
    }

    @Test
    @DisplayName("UserView - 正しくインスタンス化できる")
    void userView_CanBeInstantiated() {
        // Given
        Long id = 1L;
        String email = "test@example.com";
        String name = "Test User";

        // When
        AuthDto.UserView view = new AuthDto.UserView(id, email, name);

        // Then
        assertThat(view).isNotNull();
        assertThat(view.id()).isEqualTo(id);
        assertThat(view.email()).isEqualTo(email);
        assertThat(view.name()).isEqualTo(name);
    }

    @Test
    @DisplayName("UserView - equals()とhashCode()が正しく動作する")
    void userView_EqualsAndHashCode() {
        // Given
        AuthDto.UserView view1 = new AuthDto.UserView(1L, "test@example.com", "Test User");
        AuthDto.UserView view2 = new AuthDto.UserView(1L, "test@example.com", "Test User");
        AuthDto.UserView view3 = new AuthDto.UserView(2L, "other@example.com", "Other User");

        // Then
        assertThat(view1).isEqualTo(view2);
        assertThat(view1).isNotEqualTo(view3);
        assertThat(view1.hashCode()).isEqualTo(view2.hashCode());
    }

    @Test
    @DisplayName("UserView - toString()が動作する")
    void userView_ToString() {
        // Given
        AuthDto.UserView view = new AuthDto.UserView(1L, "test@example.com", "Test User");

        // When
        String result = view.toString();

        // Then
        assertThat(result).contains("test@example.com");
        assertThat(result).contains("Test User");
    }
}
