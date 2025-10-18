package com.example.kitchenapi.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * JWT認証フィルター
 * リクエストヘッダーからJWTトークンを抽出し、検証後にSecurityContextに認証情報を設定
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;

    public JwtAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // Authorization ヘッダーからトークンを抽出
        String token = extractToken(request);

        // トークンが存在し、検証が成功した場合
        if (token != null && jwtService.validateToken(token)) {
            try {
                // トークンからユーザー情報を取得
                String email = jwtService.getEmailFromToken(token);
                Long userId = jwtService.getUserIdFromToken(token);

                // AuthUser オブジェクトを作成（nameは現時点でトークンに含まれていないためnullまたは空）
                AuthUser authUser = new AuthUser(userId, email, null);

                // Spring Security の Authentication オブジェクトを作成
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                authUser,
                                null,
                                Collections.emptyList()
                        );

                // SecurityContext に認証情報を設定
                SecurityContextHolder.getContext().setAuthentication(authentication);

            } catch (Exception e) {
                // トークン解析エラーの場合は認証情報を設定しない
                logger.warn("Failed to process JWT token", e);
            }
        }

        // 次のフィルターへ処理を委譲
        filterChain.doFilter(request, response);
    }

    /**
     * Authorization ヘッダーからBearerトークンを抽出
     */
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (bearerToken != null && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}
