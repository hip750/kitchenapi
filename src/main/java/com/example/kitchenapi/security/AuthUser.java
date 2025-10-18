package com.example.kitchenapi.security;

/**
 * 認証済みユーザー情報を保持するクラス
 * JWTから取得した情報を格納し、コントローラー等で利用可能
 */
public class AuthUser {

    private final Long userId;
    private final String email;
    private final String name;

    public AuthUser(Long userId, String email, String name) {
        this.userId = userId;
        this.email = email;
        this.name = name;
    }

    public Long getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "AuthUser{" +
                "userId=" + userId +
                ", email='" + email + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
