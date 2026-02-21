package com.example.api.controller;

import com.example.api.entity.User;
import com.example.api.repository.UserRepository;
import com.example.api.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
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
 * 【認証方式による違い】
 * - Cookie セッション: Spring Security の formLogin() が自動でログイン処理
 * - JWT: このコントローラーで独自にログイン処理を実装
 *
 * 【Reviewer観点】
 * - パスワードを平文で保存していないか
 * - 入力値のバリデーションがあるか
 * - ユーザー名の重複チェックがあるか
 * - JWT のレスポンスに機密情報が含まれていないか
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    // JwtUtil は JWT プロファイル時のみ存在するため、オプショナルに注入
    // 【学習ポイント】
    // @Autowired(required = false) で、Bean が存在しない場合は null になる。
    // Cookie セッション時は JwtUtil が不要なので、この方式を使う。
    @Autowired(required = false)
    private JwtUtil jwtUtil;

    public AuthController(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
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
     * JWT 用ログインエンドポイント
     *
     * 【学習ポイント】
     * JWT 認証では、Spring Security の formLogin() を使わず、
     * 独自にログインエンドポイントを実装する。
     *
     * 処理の流れ：
     * 1. AuthenticationManager で username/password を検証
     * 2. 認証成功したら JwtUtil で JWT を生成
     * 3. JWT をレスポンスで返す
     *
     * 【Cookie セッションとの違い】
     * - Cookie セッション: Set-Cookie ヘッダーでセッション ID を返す
     * - JWT: レスポンスボディで JWT を返す
     *
     * 【Reviewer観点】
     * - 認証失敗時に詳細なエラーメッセージを返していないか
     *   （「ユーザーが存在しない」と「パスワードが違う」を区別しない）
     * - JWT に機密情報が含まれていないか
     */
    @PostMapping("/jwt/login")
    public ResponseEntity<?> jwtLogin(@RequestBody LoginRequest request) {
        // JWT プロファイルでない場合はエラー
        if (jwtUtil == null) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "JWT authentication is not enabled",
                "message", "Set SPRING_PROFILES_ACTIVE=jwt to enable JWT authentication"
            ));
        }

        try {
            // AuthenticationManager で認証
            // 【学習ポイント】
            // UsernamePasswordAuthenticationToken に username/password を渡す。
            // AuthenticationManager が UserDetailsService を使って検証。
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    request.username(),
                    request.password()
                )
            );

            // 認証成功 → JWT を生成
            String token = jwtUtil.generateToken(authentication.getName());

            // JWT をレスポンスで返す
            // 【学習ポイント】
            // Cookie セッションでは Set-Cookie ヘッダーで返すが、
            // JWT ではレスポンスボディで返す。
            // クライアントは localStorage や Cookie に保存する。
            return ResponseEntity.ok(Map.of(
                "message", "Login successful",
                "token", token,
                "username", authentication.getName()
            ));

        } catch (BadCredentialsException e) {
            // 認証失敗
            // 【学習ポイント】
            // 「ユーザーが存在しない」と「パスワードが違う」を区別しない。
            // 区別すると攻撃者にヒントを与えてしまう。
            return ResponseEntity.status(401).body(Map.of(
                "error", "Authentication failed",
                "message", "Invalid username or password"
            ));
        }
    }

    /**
     * リクエストボディ用の record
     *
     * 【学習ポイント】
     * Java 14+ の record を使用。イミュータブルな DTO として最適。
     * getter、equals、hashCode、toString が自動生成される。
     */
    public record RegisterRequest(String username, String password) {}

    /**
     * ログイン用のリクエストボディ
     */
    public record LoginRequest(String username, String password) {}
}
