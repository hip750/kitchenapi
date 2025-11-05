package com.example.kitchenapi.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtAuthFilter 単体テスト")
class JwtAuthFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private JwtAuthFilter jwtAuthFilter;

    @BeforeEach
    void setUp() {
        jwtAuthFilter = new JwtAuthFilter(jwtService);
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("doFilterInternal - 有効なトークンで認証情報を設定する")
    void doFilterInternal_WithValidToken_SetsAuthentication() throws ServletException, IOException {
        // Given
        String token = "valid-token";
        String email = "test@example.com";
        Long userId = 1L;

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.validateToken(token)).thenReturn(true);
        when(jwtService.getEmailFromToken(token)).thenReturn(email);
        when(jwtService.getUserIdFromToken(token)).thenReturn(userId);

        // When
        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        // Then
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(authentication.getPrincipal()).isInstanceOf(AuthUser.class);

        AuthUser authUser = (AuthUser) authentication.getPrincipal();
        assertThat(authUser.getUserId()).isEqualTo(userId);
        assertThat(authUser.getEmail()).isEqualTo(email);

        verify(filterChain).doFilter(request, response);
        verify(jwtService).validateToken(token);
        verify(jwtService).getEmailFromToken(token);
        verify(jwtService).getUserIdFromToken(token);
    }

    @Test
    @DisplayName("doFilterInternal - Authorizationヘッダーがない場合、認証情報を設定しない")
    void doFilterInternal_WithoutAuthorizationHeader_DoesNotSetAuthentication() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn(null);

        // When
        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        // Then
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNull();

        verify(filterChain).doFilter(request, response);
        verify(jwtService, never()).validateToken(anyString());
    }

    @Test
    @DisplayName("doFilterInternal - Bearerプレフィックスがない場合、認証情報を設定しない")
    void doFilterInternal_WithoutBearerPrefix_DoesNotSetAuthentication() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn("InvalidPrefix token");

        // When
        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        // Then
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNull();

        verify(filterChain).doFilter(request, response);
        verify(jwtService, never()).validateToken(anyString());
    }

    @Test
    @DisplayName("doFilterInternal - 無効なトークンの場合、認証情報を設定しない")
    void doFilterInternal_WithInvalidToken_DoesNotSetAuthentication() throws ServletException, IOException {
        // Given
        String token = "invalid-token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.validateToken(token)).thenReturn(false);

        // When
        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        // Then
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNull();

        verify(filterChain).doFilter(request, response);
        verify(jwtService).validateToken(token);
        verify(jwtService, never()).getEmailFromToken(anyString());
        verify(jwtService, never()).getUserIdFromToken(anyString());
    }

    @Test
    @DisplayName("doFilterInternal - トークン解析で例外が発生した場合、認証情報を設定せずに処理を継続する")
    void doFilterInternal_WhenTokenParsingThrowsException_DoesNotSetAuthentication() throws ServletException, IOException {
        // Given
        String token = "valid-token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.validateToken(token)).thenReturn(true);
        when(jwtService.getEmailFromToken(token)).thenThrow(new RuntimeException("Token parsing error"));

        // When
        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        // Then
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNull();

        verify(filterChain).doFilter(request, response);
        verify(jwtService).validateToken(token);
        verify(jwtService).getEmailFromToken(token);
    }

    @Test
    @DisplayName("doFilterInternal - getUserIdFromTokenで例外が発生した場合、認証情報を設定しない")
    void doFilterInternal_WhenGetUserIdThrowsException_DoesNotSetAuthentication() throws ServletException, IOException {
        // Given
        String token = "valid-token";
        String email = "test@example.com";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.validateToken(token)).thenReturn(true);
        when(jwtService.getEmailFromToken(token)).thenReturn(email);
        when(jwtService.getUserIdFromToken(token)).thenThrow(new RuntimeException("UserId extraction error"));

        // When
        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        // Then
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNull();

        verify(filterChain).doFilter(request, response);
        verify(jwtService).validateToken(token);
        verify(jwtService).getEmailFromToken(token);
        verify(jwtService).getUserIdFromToken(token);
    }

    @Test
    @DisplayName("doFilterInternal - 空のBearerトークンの場合、認証情報を設定しない")
    void doFilterInternal_WithEmptyBearerToken_DoesNotSetAuthentication() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn("Bearer ");

        // When
        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        // Then
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNull();

        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("doFilterInternal - フィルターチェーンは必ず呼ばれる")
    void doFilterInternal_AlwaysCallsFilterChain() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn(null);

        // When
        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("extractToken - 正しいフォーマットのトークンを抽出できる")
    void extractToken_WithValidFormat_ExtractsToken() throws ServletException, IOException {
        // Given
        String token = "valid-token-12345";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtService.validateToken(token)).thenReturn(true);
        when(jwtService.getEmailFromToken(token)).thenReturn("test@example.com");
        when(jwtService.getUserIdFromToken(token)).thenReturn(1L);

        // When
        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(jwtService).validateToken(token);
    }

    @Test
    @DisplayName("doFilterInternal - Bearerの後にスペースがない場合、トークンを抽出しない")
    void doFilterInternal_WithoutSpaceAfterBearer_DoesNotExtractToken() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn("Bearertoken");

        // When
        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        // Then
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNull();

        verify(filterChain).doFilter(request, response);
        verify(jwtService, never()).validateToken(anyString());
    }
}
