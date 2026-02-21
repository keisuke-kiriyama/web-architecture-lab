package com.example.api.controller;

import com.example.api.entity.User;
import com.example.api.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 認証関連の API エンドポイント
 *
 * 【学習ポイント】
 * /api/auth/** は SecurityConfig で permitAll() に設定済み。
 * 認証なしでアクセス可能。
 *
 * ログイン処理自体は Spring Security が行う（/api/auth/login）。
 * このコントローラーではユーザー登録のみ実装。
 *
 * 【Reviewer観点】
 * - パスワードを平文で保存していないか
 * - 入力値のバリデーションがあるか
 * - ユーザー名の重複チェックがあるか
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * ユーザー登録
     *
     * 【学習ポイント】
     * 1. 平文パスワードを受け取る
     * 2. BCrypt でハッシュ化
     * 3. ハッシュ化されたパスワードを DB に保存
     *
     * @param request username と password を含むリクエスト
     * @return 登録成功メッセージ
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        // 入力チェック
        if (request.username() == null || request.username().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username is required"));
        }
        if (request.password() == null || request.password().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Password is required"));
        }

        // ユーザー名の重複チェック
        if (userRepository.findByUsername(request.username()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username already exists"));
        }

        // ユーザー作成
        User user = new User();
        user.setUsername(request.username());
        // 【重要】パスワードは必ずハッシュ化して保存
        user.setPassword(passwordEncoder.encode(request.password()));

        userRepository.save(user);

        return ResponseEntity.ok(Map.of(
            "message", "User registered successfully",
            "username", user.getUsername()
        ));
    }

    /**
     * 現在ログイン中のユーザー情報を取得
     *
     * 【学習ポイント】
     * このエンドポイントは認証必須（/api/auth/** は permitAll だが、
     * /api/auth/me は例外的に認証を要求する設計もある）。
     * 今回は permitAll の範囲内なので、Principal が null の場合を考慮。
     */
    @GetMapping("/me")
    public ResponseEntity<?> me(java.security.Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }
        return ResponseEntity.ok(Map.of("username", principal.getName()));
    }

    /**
     * リクエストボディ用の record
     *
     * 【学習ポイント】
     * Java 14+ の record を使用。イミュータブルな DTO として最適。
     * getter、equals、hashCode、toString が自動生成される。
     */
    public record RegisterRequest(String username, String password) {}
}
