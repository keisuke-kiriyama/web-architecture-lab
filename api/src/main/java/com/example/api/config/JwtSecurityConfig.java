package com.example.api.config;

import com.example.api.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * JWT 認証用の SecurityFilterChain 設定
 *
 * 【学習ポイント】
 * @Profile("jwt") により、SPRING_PROFILES_ACTIVE=jwt の時のみ有効。
 *
 * JWT 認証の特徴：
 * - ステートレス：サーバーがセッション情報を保持しない
 * - CSRF 保護は不要：トークンは自動送信されないため
 * - 独自のログインエンドポイントで JWT を発行
 *
 * 【Cookie セッションとの比較】
 * | 項目 | Cookie セッション | JWT |
 * |------|-------------------|-----|
 * | CSRF | 有効（必須） | 無効（不要） |
 * | セッション | ステートフル | ステートレス |
 * | 認証フィルター | Spring 提供 | 自作 |
 * | ログイン | formLogin() | 独自エンドポイント |
 * | スケーラビリティ | Redis等で共有必要 | 共有不要 |
 *
 * 【起動方法】
 * docker-compose.yml で以下を設定：
 * environment:
 *   - SPRING_PROFILES_ACTIVE=jwt
 *
 * 【Reviewer観点】
 * - JWT 使用時に CSRF を無効化する理由を説明できるか
 * - ステートレスセッションの設定は適切か
 * - JWT フィルターの追加位置は正しいか
 */
@Configuration
@Profile("jwt")
public class JwtSecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public JwtSecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    /**
     * JWT 用のセキュリティフィルターチェーン
     *
     * 【学習ポイント】
     * Cookie セッション版との主な違い：
     * 1. CSRF 無効化
     * 2. セッションをステートレスに
     * 3. JWT 認証フィルターを追加
     * 4. formLogin() を使わない
     */
    @Bean
    public SecurityFilterChain jwtSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            // CORS 設定（Phase 1 で設定済みの CorsConfig と連携）
            .cors(cors -> {})

            // CSRF 無効化
            // 【学習ポイント】
            // JWT 認証では CSRF 保護を無効化することが多い。
            //
            // 【なぜ JWT では CSRF 対策が不要か】
            // CSRF 攻撃の仕組み：
            // 1. ブラウザが Cookie を「自動送信」する
            // 2. 攻撃者のサイトから被害者のブラウザ経由でリクエスト
            // 3. Cookie が自動で付与され、認証が通ってしまう
            //
            // JWT の場合：
            // - トークンは Authorization ヘッダーで送信
            // - ブラウザは「自動送信」しない
            // - JavaScript で明示的にヘッダーに設定する必要がある
            // - 攻撃者のサイトから被害者のトークンを読み取れない（同一オリジンポリシー）
            //
            // 【注意】
            // JWT を Cookie に保存する場合は CSRF 対策が必要。
            // localStorage に保存する場合は XSS に注意が必要。
            .csrf(csrf -> csrf.disable())

            // URL ごとのアクセス制御
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                .anyRequest().authenticated()
            )

            // セッション管理をステートレスに
            // 【学習ポイント】
            // STATELESS: サーバー側でセッションを作成しない。
            // リクエストごとに JWT で認証状態を確認する。
            //
            // 【Cookie セッションとの違い】
            // - Cookie セッション: サーバーがセッション ID → ユーザー情報のマップを保持
            // - JWT: サーバーは何も保持しない（トークン自体に情報が含まれる）
            //
            // 【メリット】
            // - サーバー間でセッション共有が不要
            // - スケールアウトしやすい
            //
            // 【デメリット】
            // - トークンを即座に無効化できない
            // - トークンサイズが大きくなりがち
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // 未認証時のハンドラー
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(401);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"Authentication required\"}");
                })
            )

            // JWT 認証フィルターを追加
            // 【学習ポイント】
            // addFilterBefore: 指定したフィルターの「前」に追加。
            // UsernamePasswordAuthenticationFilter の前に JWT 検証を行う。
            //
            // フィルターチェーンの順序：
            // ... → JwtAuthenticationFilter → UsernamePasswordAuthenticationFilter → ...
            //
            // JWT フィルターで認証が成功すれば、
            // UsernamePasswordAuthenticationFilter はスキップされる。
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
