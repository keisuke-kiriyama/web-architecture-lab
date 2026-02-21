package com.example.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security の設定クラス
 *
 * 【学習ポイント】
 * Spring Security 6.x では WebSecurityConfigurerAdapter は非推奨。
 * 代わりに SecurityFilterChain を Bean として定義する。
 *
 * フィルターチェーン: リクエストが Controller に到達する前に
 * 複数のフィルターを通過する。認証・認可・CSRF チェックなど。
 *
 * 【Reviewer観点】
 * - CSRF 保護が有効か（デフォルト有効）
 * - セッション固定攻撃対策があるか
 * - 認証が必要なエンドポイントが適切に設定されているか
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * パスワードエンコーダー
     *
     * 【学習ポイント】
     * BCryptPasswordEncoder は：
     * - ソルトを自動生成して埋め込む
     * - コストファクター（デフォルト10）で計算コストを調整
     * - 同じパスワードでも毎回異なるハッシュ値を生成
     *
     * 【Reviewer観点】
     * - BCrypt 以外（MD5, SHA-1）を使っていないか
     * - コストファクターが適切か（10〜12が推奨）
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * セキュリティフィルターチェーンの設定
     *
     * 【学習ポイント】
     * authorizeHttpRequests: URL パターンごとのアクセス制御
     * formLogin: フォームベース認証の設定
     * sessionManagement: セッション管理の設定
     *
     * 【Phase 2 の設定方針】
     * - /api/auth/** は認証不要（ログイン・登録用）
     * - /api/posts/** は認証必要
     * - CSRF は一旦無効化（後で有効化して挙動を観察）
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // CORS 設定（Phase 1 で設定済みの CorsConfig と連携）
            .cors(cors -> {})

            // CSRF 保護
            // 【学習ポイント】
            // REST API では CSRF トークンの扱いが複雑になるため、
            // 一旦無効化する。後で有効化して Cookie での CSRF トークン送信を学ぶ。
            // 【Reviewer観点】これは学習用。本番では有効化すべき。
            .csrf(csrf -> csrf.disable())

            // URL ごとのアクセス制御
            .authorizeHttpRequests(auth -> auth
                // 認証不要のエンドポイント
                .requestMatchers("/api/auth/**").permitAll()
                // ヘルスチェック用（Docker 等）
                .requestMatchers("/actuator/health").permitAll()
                // それ以外は認証必要
                .anyRequest().authenticated()
            )

            // フォームログインの設定
            // 【学習ポイント】
            // REST API では通常フォームログインは使わないが、
            // Cookie セッションの挙動を理解するために設定する。
            .formLogin(form -> form
                // ログイン処理のエンドポイント
                .loginProcessingUrl("/api/auth/login")
                // ログイン成功時のハンドラー（JSON レスポンスを返す）
                .successHandler((request, response, authentication) -> {
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"message\":\"Login successful\",\"username\":\""
                        + authentication.getName() + "\"}");
                })
                // ログイン失敗時のハンドラー
                .failureHandler((request, response, exception) -> {
                    response.setStatus(401);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"error\":\"Login failed\",\"message\":\""
                        + exception.getMessage() + "\"}");
                })
                // すべてのユーザーがログインページにアクセス可能
                .permitAll()
            )

            // ログアウトの設定
            .logout(logout -> logout
                .logoutUrl("/api/auth/logout")
                .logoutSuccessHandler((request, response, authentication) -> {
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"message\":\"Logout successful\"}");
                })
                // セッション無効化
                .invalidateHttpSession(true)
                // Cookie 削除
                .deleteCookies("JSESSIONID")
            )

            // 未認証時のハンドラー（401 を返す）
            // 【学習ポイント】
            // デフォルトではログインページにリダイレクトするが、
            // REST API では 401 ステータスを返すべき。
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(401);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"Authentication required\"}");
                })
            )

            // セッション管理
            // 【学習ポイント】
            // sessionFixation: セッション固定攻撃対策
            // migrateSession: ログイン時に新しいセッション ID を発行
            .sessionManagement(session -> session
                .sessionFixation().migrateSession()
            );

        return http.build();
    }
}
