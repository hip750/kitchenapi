package com.example.kitchenapi.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

/**
 * JWT生成・検証を行うサービス
 */
@Service
public class JwtService {

    private final AppSecurityProps props;

    public JwtService(AppSecurityProps props) {
        this.props = props;
    }

    /**
     * JWTトークンを生成
     * @param email ユーザーのメールアドレス (sub claim)
     * @param userId ユーザーID (uid claim)
     * @return 生成されたJWTトークン
     */
    public String generateToken(String email, Long userId) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + props.getJwtExpMinutes() * 60 * 1000L);

        return Jwts.builder()
                .setSubject(email)
                .claim("uid", userId)
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * JWTトークンを検証
     * @param token 検証対象のトークン
     * @return 検証に成功した場合true
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * JWTトークンからメールアドレスを取得
     * @param token JWTトークン
     * @return メールアドレス (sub claim)
     */
    public String getEmailFromToken(String token) {
        Claims claims = getClaims(token);
        return claims.getSubject();
    }

    /**
     * JWTトークンからユーザーIDを取得
     * @param token JWTトークン
     * @return ユーザーID (uid claim)
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = getClaims(token);
        Object uid = claims.get("uid");
        if (uid instanceof Number) {
            return ((Number) uid).longValue();
        }
        throw new IllegalArgumentException("Invalid uid claim");
    }

    /**
     * トークンからClaimsを取得
     */
    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 署名用のKeyを取得
     */
    private Key getSigningKey() {
        byte[] keyBytes = props.getJwtSecret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
