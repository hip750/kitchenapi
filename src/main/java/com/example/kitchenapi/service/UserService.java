package com.example.kitchenapi.service;

import com.example.kitchenapi.entity.UserEntity;
import com.example.kitchenapi.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * ユーザー管理のサービス層
 * ユーザーの登録、ログイン、およびユーザー取得操作を処理します。
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * メールアドレス、名前、パスワードを使用して新しいユーザーを登録します。
     * パスワードはBCryptPasswordEncoderを使用してハッシュ化されます。
     *
     * @param email ユーザーのメールアドレス
     * @param name ユーザーの名前
     * @param password ユーザーの平文パスワード
     * @return 作成されたUserEntity
     * @throws ResponseStatusException メールアドレスが既に存在する場合は409
     */
    @Transactional
    public UserEntity signup(String email, String name, String password) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name is required");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }

        // メールアドレスの重複をチェック
        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }

        // パスワードをハッシュ化
        String passwordHash = passwordEncoder.encode(password);

        // ユーザーを作成して保存
        UserEntity user = new UserEntity(email, name, passwordHash);
        return userRepository.save(user);
    }

    /**
     * メールアドレスとパスワードでユーザーを認証します。
     *
     * @param email ユーザーのメールアドレス
     * @param password ユーザーの平文パスワード
     * @return 認証されたUserEntity
     * @throws ResponseStatusException 認証に失敗した場合は401
     */
    @Transactional(readOnly = true)
    public UserEntity login(String email, String password) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }

        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        // パスワードを検証
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        return user;
    }

    /**
     * IDでユーザーを検索します。
     *
     * @param userId ユーザーID
     * @return UserEntity
     * @throws ResponseStatusException ユーザーが見つからない場合は404
     */
    @Transactional(readOnly = true)
    public UserEntity findById(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID is required");
        }

        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }
}
