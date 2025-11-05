package com.example.kitchenapi.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UserEntity 単体テスト")
class UserEntityTest {

    @Test
    @DisplayName("デフォルトコンストラクタでインスタンス化できる")
    void defaultConstructor() {
        // When
        UserEntity user = new UserEntity();

        // Then
        assertThat(user).isNotNull();
        assertThat(user.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("パラメータ付きコンストラクタで正しくフィールドが設定される")
    void parameterizedConstructor() {
        // Given
        String email = "test@example.com";
        String name = "Test User";
        String passwordHash = "hashedPassword";

        // When
        UserEntity user = new UserEntity(email, name, passwordHash);

        // Then
        assertThat(user.getEmail()).isEqualTo(email);
        assertThat(user.getName()).isEqualTo(name);
        assertThat(user.getPasswordHash()).isEqualTo(passwordHash);
        assertThat(user.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("セッターで値を設定できる")
    void setters() {
        // Given
        UserEntity user = new UserEntity();
        Instant now = Instant.now();

        // When
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setName("Test User");
        user.setPasswordHash("hashedPassword");
        user.setCreatedAt(now);

        // Then
        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getEmail()).isEqualTo("test@example.com");
        assertThat(user.getName()).isEqualTo("Test User");
        assertThat(user.getPasswordHash()).isEqualTo("hashedPassword");
        assertThat(user.getCreatedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("equals()とhashCode()が正しく動作する")
    void equalsAndHashCode() {
        // Given
        UserEntity user1 = new UserEntity("test@example.com", "Test", "hash");
        user1.setId(1L);

        UserEntity user2 = new UserEntity("other@example.com", "Other", "hash");
        user2.setId(1L);

        UserEntity user3 = new UserEntity("test@example.com", "Test", "hash");
        user3.setId(2L);

        // Then
        assertThat(user1).isEqualTo(user2); // 同じID
        assertThat(user1).isNotEqualTo(user3); // 異なるID
        assertThat(user1.hashCode()).isEqualTo(user2.hashCode());
    }

    @Test
    @DisplayName("equals() - 同じインスタンス")
    void equals_SameInstance() {
        // Given
        UserEntity user = new UserEntity();
        user.setId(1L);

        // Then
        assertThat(user).isEqualTo(user);
    }

    @Test
    @DisplayName("equals() - nullと比較")
    void equals_Null() {
        // Given
        UserEntity user = new UserEntity();
        user.setId(1L);

        // Then
        assertThat(user).isNotEqualTo(null);
    }

    @Test
    @DisplayName("equals() - 異なるクラスと比較")
    void equals_DifferentClass() {
        // Given
        UserEntity user = new UserEntity();
        user.setId(1L);

        // Then
        assertThat(user).isNotEqualTo("string");
    }
}
