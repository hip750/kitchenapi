package com.example.kitchenapi.controller;

import com.example.kitchenapi.dto.AuthDto;
import com.example.kitchenapi.entity.UserEntity;
import com.example.kitchenapi.security.AuthUser;
import com.example.kitchenapi.security.JwtService;
import com.example.kitchenapi.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * 認証関連のコントローラー
 * ユーザー登録、ログイン、認証済みユーザー情報の取得を処理します
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;

    public AuthController(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    /**
     * POST /api/auth/signup
     * 新規ユーザーを登録します
     *
     * @param req メールアドレス、名前、パスワードを含む登録リクエスト
     * @return 200 ユーザー情報を含むUserView
     */
    @PostMapping("/signup")
    public ResponseEntity<AuthDto.UserView> signup(@Valid @RequestBody AuthDto.SignupRequest req) {
        UserEntity user = userService.signup(req.email(), req.name(), req.password());

        // エンティティをDTOに変換
        AuthDto.UserView userView = new AuthDto.UserView(
                user.getId(),
                user.getEmail(),
                user.getName()
        );

        return ResponseEntity.ok(userView);
    }

    /**
     * POST /api/auth/login
     * ユーザーを認証してJWTトークンを生成します
     *
     * @param req メールアドレスとパスワードを含むログインリクエスト
     * @return 200 JWTトークンとユーザー情報を含むLoginResponse
     */
    @PostMapping("/login")
    public ResponseEntity<AuthDto.LoginResponse> login(@Valid @RequestBody AuthDto.LoginRequest req) {
        UserEntity user = userService.login(req.email(), req.password());

        // JWTトークンを生成
        String token = jwtService.generateToken(user.getEmail(), user.getId());

        // エンティティをDTOに変換
        AuthDto.LoginResponse loginResponse = new AuthDto.LoginResponse(
                token,
                user.getId(),
                user.getEmail(),
                user.getName()
        );

        return ResponseEntity.ok(loginResponse);
    }

    /**
     * GET /api/auth/me
     * JWTから認証済みユーザー情報を取得します
     *
     * @param authentication Spring SecurityのAuthenticationオブジェクト
     * @return 200 ユーザー情報を含むUserView
     */
    @GetMapping("/me")
    public ResponseEntity<AuthDto.UserView> me(Authentication authentication) {
        // SecurityContextからAuthUserを取得
        AuthUser authUser = (AuthUser) authentication.getPrincipal();

        // データベースからユーザー情報を取得
        UserEntity user = userService.findById(authUser.getUserId());

        // エンティティをDTOに変換
        AuthDto.UserView userView = new AuthDto.UserView(
                user.getId(),
                user.getEmail(),
                user.getName()
        );

        return ResponseEntity.ok(userView);
    }
}
