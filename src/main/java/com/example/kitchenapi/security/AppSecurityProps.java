package com.example.kitchenapi.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT認証に関する設定プロパティ
 * application.yml の app.security.* を読み込む
 */
@Component
@ConfigurationProperties(prefix = "app.security")
public class AppSecurityProps {

    /**
     * JWT署名用のシークレットキー
     * 本番環境では環境変数等から読み込むこと
     */
    private String jwtSecret;

    /**
     * JWT有効期限（分単位）
     */
    private int jwtExpMinutes;

    public String getJwtSecret() {
        return jwtSecret;
    }

    public void setJwtSecret(String jwtSecret) {
        this.jwtSecret = jwtSecret;
    }

    public int getJwtExpMinutes() {
        return jwtExpMinutes;
    }

    public void setJwtExpMinutes(int jwtExpMinutes) {
        this.jwtExpMinutes = jwtExpMinutes;
    }
}
