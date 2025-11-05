package com.example.kitchenapi.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("JwtService 単体テスト")
class JwtServiceTest {

    private static final String SECRET_KEY = "my-test-secret-key-with-at-least-32-characters-for-hs256";
    private static final int JWT_EXP_MINUTES = 120;

    private JwtService createJwtService() {
        AppSecurityProps props = new AppSecurityProps();
        props.setJwtSecret(SECRET_KEY);
        props.setJwtExpMinutes(JWT_EXP_MINUTES);
        return new JwtService(props);
    }

    @Test
    @DisplayName("generateToken - 正常系: トークンを生成できる")
    void generateToken_Success() {
        // Given
        JwtService jwtService = createJwtService();
        String email = "test@example.com";
        Long userId = 1L;

        // When
        String token = jwtService.generateToken(email, userId);

        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3); // Header.Payload.Signature
    }

    @Test
    @DisplayName("validateToken - 正常系: 有効なトークンを検証できる")
    void validateToken_ValidToken() {
        // Given
        JwtService jwtService = createJwtService();
        String email = "test@example.com";
        Long userId = 1L;
        String token = jwtService.generateToken(email, userId);

        // When
        boolean isValid = jwtService.validateToken(token);

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("validateToken - 異常系: 不正な形式のトークン")
    void validateToken_InvalidFormat() {
        // Given
        JwtService jwtService = createJwtService();
        String invalidToken = "invalid.token";

        // When
        boolean isValid = jwtService.validateToken(invalidToken);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("validateToken - 異常系: 期限切れのトークン")
    void validateToken_ExpiredToken() {
        // Given
        JwtService jwtService = createJwtService();
        Key key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
        Date now = new Date();
        Date expiration = new Date(now.getTime() - 1000); // 1秒前に期限切れ

        String expiredToken = Jwts.builder()
                .setSubject("test@example.com")
                .claim("uid", 1L)
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(key)
                .compact();

        // When
        boolean isValid = jwtService.validateToken(expiredToken);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("validateToken - 異常系: 署名が不正なトークン")
    void validateToken_InvalidSignature() {
        // Given
        JwtService jwtService = createJwtService();
        String wrongSecret = "wrong-secret-key-with-at-least-32-characters-for-hs256-abc";
        Key wrongKey = Keys.hmacShaKeyFor(wrongSecret.getBytes(StandardCharsets.UTF_8));

        String tokenWithWrongSignature = Jwts.builder()
                .setSubject("test@example.com")
                .claim("uid", 1L)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 60000))
                .signWith(wrongKey)
                .compact();

        // When
        boolean isValid = jwtService.validateToken(tokenWithWrongSignature);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("getEmailFromToken - 正常系: トークンからメールアドレスを取得できる")
    void getEmailFromToken_Success() {
        // Given
        JwtService jwtService = createJwtService();
        String email = "test@example.com";
        Long userId = 1L;
        String token = jwtService.generateToken(email, userId);

        // When
        String extractedEmail = jwtService.getEmailFromToken(token);

        // Then
        assertThat(extractedEmail).isEqualTo(email);
    }

    @Test
    @DisplayName("getUserIdFromToken - 正常系: トークンからユーザーIDを取得できる")
    void getUserIdFromToken_Success() {
        // Given
        JwtService jwtService = createJwtService();
        String email = "test@example.com";
        Long userId = 123L;
        String token = jwtService.generateToken(email, userId);

        // When
        Long extractedUserId = jwtService.getUserIdFromToken(token);

        // Then
        assertThat(extractedUserId).isEqualTo(userId);
    }

    @Test
    @DisplayName("getUserIdFromToken - 異常系: uidクレームがNumber型でない")
    void getUserIdFromToken_InvalidUidType() {
        // Given
        JwtService jwtService = createJwtService();
        Key key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
        String tokenWithInvalidUid = Jwts.builder()
                .setSubject("test@example.com")
                .claim("uid", "not-a-number") // 文字列
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 60000))
                .signWith(key)
                .compact();

        // When & Then
        assertThatThrownBy(() -> jwtService.getUserIdFromToken(tokenWithInvalidUid))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid uid claim");
    }

    @Test
    @DisplayName("トークンの有効期限が正しく設定される")
    void tokenExpiration_IsCorrect() throws InterruptedException {
        // Given
        AppSecurityProps props = new AppSecurityProps();
        props.setJwtSecret(SECRET_KEY);
        props.setJwtExpMinutes(0); // 即座に期限切れ
        JwtService shortLivedJwtService = new JwtService(props);

        String email = "test@example.com";
        Long userId = 1L;

        // When
        String shortLivedToken = shortLivedJwtService.generateToken(email, userId);
        Thread.sleep(100); // 100ms待機

        // Then
        assertThat(shortLivedJwtService.validateToken(shortLivedToken)).isFalse();
    }

    @Test
    @DisplayName("同じユーザーでも発行時刻が異なれば異なるトークンが生成される")
    void generateToken_DifferentTokensForSameUser() throws InterruptedException {
        // Given
        JwtService jwtService = createJwtService();
        String email = "test@example.com";
        Long userId = 1L;

        // When
        String token1 = jwtService.generateToken(email, userId);
        Thread.sleep(1100); // トークン生成の時刻を変えるため1秒以上待つ（iatは秒単位）
        String token2 = jwtService.generateToken(email, userId);

        // Then
        assertThat(token1).isNotEqualTo(token2); // 発行時刻が異なるため
        assertThat(jwtService.getEmailFromToken(token1)).isEqualTo(email);
        assertThat(jwtService.getEmailFromToken(token2)).isEqualTo(email);
        assertThat(jwtService.getUserIdFromToken(token1)).isEqualTo(userId);
        assertThat(jwtService.getUserIdFromToken(token2)).isEqualTo(userId);
    }
}
