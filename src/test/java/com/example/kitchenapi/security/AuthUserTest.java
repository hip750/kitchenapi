package com.example.kitchenapi.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AuthUser 単体テスト")
class AuthUserTest {

    @Test
    @DisplayName("コンストラクタで正しくフィールドが設定される")
    void constructor_SetsFieldsCorrectly() {
        // Given
        Long userId = 1L;
        String email = "test@example.com";
        String name = "Test User";

        // When
        AuthUser authUser = new AuthUser(userId, email, name);

        // Then
        assertThat(authUser.getUserId()).isEqualTo(userId);
        assertThat(authUser.getEmail()).isEqualTo(email);
        assertThat(authUser.getName()).isEqualTo(name);
    }

    @Test
    @DisplayName("toString()が正しく文字列を返す")
    void toString_ReturnsCorrectString() {
        // Given
        AuthUser authUser = new AuthUser(1L, "test@example.com", "Test User");

        // When
        String result = authUser.toString();

        // Then
        assertThat(result).contains("userId=1");
        assertThat(result).contains("email='test@example.com'");
        assertThat(result).contains("name='Test User'");
    }

    @Test
    @DisplayName("nameがnullでもインスタンス化できる")
    void constructor_AcceptsNullName() {
        // When
        AuthUser authUser = new AuthUser(1L, "test@example.com", null);

        // Then
        assertThat(authUser.getUserId()).isEqualTo(1L);
        assertThat(authUser.getEmail()).isEqualTo("test@example.com");
        assertThat(authUser.getName()).isNull();
    }
}
