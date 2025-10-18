package com.example.kitchenapi.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * 認証関連のDTO
 * AuthControllerでリクエスト/レスポンスに使用される全てのレコードを定義しています
 */
public class AuthDto {

    /**
     * ユーザー登録のリクエストDTO
     * POST /api/auth/signup で使用されます
     */
    public record SignupRequest(
            @NotBlank(message = "Email is required")
            @Email(message = "Email must be valid")
            String email,

            @NotBlank(message = "Name is required")
            String name,

            @NotBlank(message = "Password is required")
            String password
    ) {}

    /**
     * ユーザーログインのリクエストDTO
     * POST /api/auth/login で使用されます
     */
    public record LoginRequest(
            @NotBlank(message = "Email is required")
            @Email(message = "Email must be valid")
            String email,

            @NotBlank(message = "Password is required")
            String password
    ) {}

    /**
     * ログイン成功時のレスポンスDTO
     * POST /api/auth/login のレスポンスで使用されます
     */
    public record LoginResponse(
            String token,
            Long userId,
            String email,
            String name
    ) {}

    /**
     * ユーザー情報表示用のレスポンスDTO
     * POST /api/auth/signup と GET /api/auth/me のレスポンスで使用されます
     */
    public record UserView(
            Long id,
            String email,
            String name
    ) {}
}
