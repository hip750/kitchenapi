package com.example.kitchenapi.common;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("GlobalExceptionHandler 単体テスト")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("handleMethodArgumentNotValid - バリデーションエラーを正しく処理する")
    void handleMethodArgumentNotValid_ReturnsProperProblemDetail() {
        // Given
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError error1 = new FieldError("object", "email", "Email is required");
        FieldError error2 = new FieldError("object", "name", "Name is required");
        when(bindingResult.getFieldErrors()).thenReturn(Arrays.asList(error1, error2));

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        // When
        ProblemDetail result = handler.handleMethodArgumentNotValid(ex);

        // Then
        assertThat(result.getStatus()).isEqualTo(400);
        assertThat(result.getTitle()).isEqualTo("Validation Failed");
        assertThat(result.getDetail()).contains("email");
        assertThat(result.getDetail()).contains("Email is required");
        assertThat(result.getDetail()).contains("name");
        assertThat(result.getDetail()).contains("Name is required");
    }

    @Test
    @DisplayName("handleMethodArgumentNotValid - 単一のバリデーションエラーを処理する")
    void handleMethodArgumentNotValid_SingleError() {
        // Given
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError error = new FieldError("object", "password", "Password is required");
        when(bindingResult.getFieldErrors()).thenReturn(Arrays.asList(error));

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        // When
        ProblemDetail result = handler.handleMethodArgumentNotValid(ex);

        // Then
        assertThat(result.getStatus()).isEqualTo(400);
        assertThat(result.getDetail()).isEqualTo("password: Password is required");
    }

    @Test
    @DisplayName("handleResponseStatusException - 404エラーを正しく処理する")
    void handleResponseStatusException_NotFound() {
        // Given
        ResponseStatusException ex = new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "User not found"
        );

        // When
        ProblemDetail result = handler.handleResponseStatusException(ex);

        // Then
        assertThat(result.getStatus()).isEqualTo(404);
        assertThat(result.getTitle()).contains("404");
        assertThat(result.getDetail()).isEqualTo("User not found");
    }

    @Test
    @DisplayName("handleResponseStatusException - 409 Conflictエラーを正しく処理する")
    void handleResponseStatusException_Conflict() {
        // Given
        ResponseStatusException ex = new ResponseStatusException(
                HttpStatus.CONFLICT,
                "User already exists"
        );

        // When
        ProblemDetail result = handler.handleResponseStatusException(ex);

        // Then
        assertThat(result.getStatus()).isEqualTo(409);
        assertThat(result.getTitle()).contains("409");
        assertThat(result.getDetail()).isEqualTo("User already exists");
    }

    @Test
    @DisplayName("handleResponseStatusException - 401 Unauthorizedエラーを正しく処理する")
    void handleResponseStatusException_Unauthorized() {
        // Given
        ResponseStatusException ex = new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Invalid credentials"
        );

        // When
        ProblemDetail result = handler.handleResponseStatusException(ex);

        // Then
        assertThat(result.getStatus()).isEqualTo(401);
        assertThat(result.getTitle()).contains("401");
        assertThat(result.getDetail()).isEqualTo("Invalid credentials");
    }

    @Test
    @DisplayName("handleIllegalArgumentException - 不正な引数エラーを正しく処理する")
    void handleIllegalArgumentException_ReturnsBadRequest() {
        // Given
        IllegalArgumentException ex = new IllegalArgumentException("Invalid input value");

        // When
        ProblemDetail result = handler.handleIllegalArgumentException(ex);

        // Then
        assertThat(result.getStatus()).isEqualTo(400);
        assertThat(result.getTitle()).isEqualTo("Bad Request");
        assertThat(result.getDetail()).isEqualTo("Invalid input value");
    }

    @Test
    @DisplayName("handleException - 一般的な例外を500エラーとして処理する")
    void handleException_ReturnsInternalServerError() {
        // Given
        Exception ex = new RuntimeException("Unexpected error occurred");

        // When
        ProblemDetail result = handler.handleException(ex);

        // Then
        assertThat(result.getStatus()).isEqualTo(500);
        assertThat(result.getTitle()).isEqualTo("Internal Server Error");
        assertThat(result.getDetail()).contains("An unexpected error occurred");
        assertThat(result.getDetail()).contains("Unexpected error occurred");
    }

    @Test
    @DisplayName("handleException - NullPointerExceptionを500エラーとして処理する")
    void handleException_NullPointerException() {
        // Given
        Exception ex = new NullPointerException("Null value encountered");

        // When
        ProblemDetail result = handler.handleException(ex);

        // Then
        assertThat(result.getStatus()).isEqualTo(500);
        assertThat(result.getTitle()).isEqualTo("Internal Server Error");
        assertThat(result.getDetail()).contains("Null value encountered");
    }
}
