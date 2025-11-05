package com.example.kitchenapi.service;

import com.example.kitchenapi.entity.UserEntity;
import com.example.kitchenapi.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService 単体テスト")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private UserEntity testUser;

    @BeforeEach
    void setUp() {
        testUser = new UserEntity("test@example.com", "Test User", "hashedPassword");
        testUser.setId(1L);
    }

    @Test
    @DisplayName("signup - 正常系: 新規ユーザーを登録できる")
    void signup_Success() {
        // Given
        String email = "test@example.com";
        String name = "Test User";
        String password = "password123";
        String hashedPassword = "hashedPassword";

        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(passwordEncoder.encode(password)).thenReturn(hashedPassword);
        when(userRepository.save(any(UserEntity.class))).thenReturn(testUser);

        // When
        UserEntity result = userService.signup(email, name, password);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo(email);
        assertThat(result.getName()).isEqualTo(name);
        verify(userRepository).existsByEmail(email);
        verify(passwordEncoder).encode(password);
        verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("signup - 異常系: メールアドレスがnull")
    void signup_NullEmail() {
        // When & Then
        assertThatThrownBy(() -> userService.signup(null, "Test User", "password123"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email is required");
    }

    @Test
    @DisplayName("signup - 異常系: メールアドレスが空白")
    void signup_BlankEmail() {
        // When & Then
        assertThatThrownBy(() -> userService.signup("   ", "Test User", "password123"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email is required");
    }

    @Test
    @DisplayName("signup - 異常系: 名前がnull")
    void signup_NullName() {
        // When & Then
        assertThatThrownBy(() -> userService.signup("test@example.com", null, "password123"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Name is required");
    }

    @Test
    @DisplayName("signup - 異常系: 名前が空白")
    void signup_BlankName() {
        // When & Then
        assertThatThrownBy(() -> userService.signup("test@example.com", "   ", "password123"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Name is required");
    }

    @Test
    @DisplayName("signup - 異常系: パスワードがnull")
    void signup_NullPassword() {
        // When & Then
        assertThatThrownBy(() -> userService.signup("test@example.com", "Test User", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Password is required");
    }

    @Test
    @DisplayName("signup - 異常系: パスワードが空白")
    void signup_BlankPassword() {
        // When & Then
        assertThatThrownBy(() -> userService.signup("test@example.com", "Test User", "   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Password is required");
    }

    @Test
    @DisplayName("signup - 異常系: メールアドレスが既に存在する")
    void signup_EmailAlreadyExists() {
        // Given
        String email = "test@example.com";
        when(userRepository.existsByEmail(email)).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> userService.signup(email, "Test User", "password123"))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException rse = (ResponseStatusException) ex;
                    assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
                    assertThat(rse.getReason()).isEqualTo("Email already exists");
                });

        verify(userRepository).existsByEmail(email);
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("login - 正常系: 正しい認証情報でログインできる")
    void login_Success() {
        // Given
        String email = "test@example.com";
        String password = "password123";

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(password, testUser.getPasswordHash())).thenReturn(true);

        // When
        UserEntity result = userService.login(email, password);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo(email);
        verify(userRepository).findByEmail(email);
        verify(passwordEncoder).matches(password, testUser.getPasswordHash());
    }

    @Test
    @DisplayName("login - 異常系: メールアドレスがnull")
    void login_NullEmail() {
        // When & Then
        assertThatThrownBy(() -> userService.login(null, "password123"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email is required");
    }

    @Test
    @DisplayName("login - 異常系: メールアドレスが空白")
    void login_BlankEmail() {
        // When & Then
        assertThatThrownBy(() -> userService.login("   ", "password123"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Email is required");
    }

    @Test
    @DisplayName("login - 異常系: パスワードがnull")
    void login_NullPassword() {
        // When & Then
        assertThatThrownBy(() -> userService.login("test@example.com", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Password is required");
    }

    @Test
    @DisplayName("login - 異常系: パスワードが空白")
    void login_BlankPassword() {
        // When & Then
        assertThatThrownBy(() -> userService.login("test@example.com", "   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Password is required");
    }

    @Test
    @DisplayName("login - 異常系: ユーザーが存在しない")
    void login_UserNotFound() {
        // Given
        String email = "nonexistent@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.login(email, "password123"))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException rse = (ResponseStatusException) ex;
                    assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
                    assertThat(rse.getReason()).isEqualTo("Invalid credentials");
                });

        verify(userRepository).findByEmail(email);
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("login - 異常系: パスワードが一致しない")
    void login_InvalidPassword() {
        // Given
        String email = "test@example.com";
        String password = "wrongPassword";

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(password, testUser.getPasswordHash())).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> userService.login(email, password))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException rse = (ResponseStatusException) ex;
                    assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
                    assertThat(rse.getReason()).isEqualTo("Invalid credentials");
                });

        verify(userRepository).findByEmail(email);
        verify(passwordEncoder).matches(password, testUser.getPasswordHash());
    }

    @Test
    @DisplayName("findById - 正常系: IDでユーザーを取得できる")
    void findById_Success() {
        // Given
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        // When
        UserEntity result = userService.findById(userId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(userId);
        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("findById - 異常系: ユーザーIDがnull")
    void findById_NullUserId() {
        // When & Then
        assertThatThrownBy(() -> userService.findById(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User ID is required");
    }

    @Test
    @DisplayName("findById - 異常系: ユーザーが存在しない")
    void findById_UserNotFound() {
        // Given
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.findById(userId))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException rse = (ResponseStatusException) ex;
                    assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
                    assertThat(rse.getReason()).isEqualTo("User not found");
                });

        verify(userRepository).findById(userId);
    }
}
