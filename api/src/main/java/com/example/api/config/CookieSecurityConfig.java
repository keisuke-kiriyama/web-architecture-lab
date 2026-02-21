package com.example.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

/**
 * Cookie セッション認証用の SecurityFilterChain 設定
 *
 * 【学習ポイント】
 * @Profile("cookie") により、SPRING_PROFILES_ACTIVE=cookie の時のみ有効。
 *
 * Cookie セッション認証の特徴：
 * - ステートフル：サーバーがセッション情報を保持
 * - CSRF 保護が必要：Cookie は自動送信されるため
 * - セッション固定攻撃対策が必要
 *
 * 【起動方法】
 * docker-compose.yml で以下を設定：
 * environment:
 *   - SPRING_PROFILES_ACTIVE=cookie
 *
 * 【Reviewer観点】
 * - CSRF 保護が有効か
 * - セッション固定攻撃対策があるか
 * - Cookie の属性（HttpOnly, Secure, SameSite）は適切か
 */
@Configuration
@Profile("cookie")
public class CookieSecurityConfig {

    /**
     * Cookie セッション用のセキュリティフィルターチェーン
     *
     * 【学習ポイント】
     * Phase 2 で実装した Cookie セッション認証の設定。
     * JWT 版と比較するために残している。
     */
    @Bean
    public SecurityFilterChain cookieSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            // CORS 設定（Phase 1 で設定済みの CorsConfig と連携）
            .cors(cors -> {})

            // CSRF 保護
            // 【学習ポイント】
            // Cookie セッションでは CSRF 保護が必須。
            // 理由：Cookie は「そのドメインへのリクエスト」に自動付与されるため、
            // 攻撃者のサイトからリクエストを送ると Cookie が付いてしまう。
            //
            // 対策：CSRF トークンをヘッダーに設定させる。
            // 攻撃者は Cookie を「読めない」のでヘッダーに設定できない。
            .csrf(csrf -> {
                CsrfTokenRequestAttributeHandler handler = new CsrfTokenRequestAttributeHandler();
                handler.setCsrfRequestAttributeName(null);  // 遅延読み込みを無効化
                csrf
                    .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                    .csrfTokenRequestHandler(handler)
                    .ignoringRequestMatchers("/api/auth/login", "/api/auth/register");
            })

            // URL ごとのアクセス制御
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                .anyRequest().authenticated()
            )

            // フォームログインの設定
            // 【学習ポイント】
            // Spring Security がログイン処理を自動で行う。
            // ログイン成功時にセッションを作成し、JSESSIONID Cookie を発行。
            .formLogin(form -> form
                .loginProcessingUrl("/api/auth/login")
                .successHandler((request, response, authentication) -> {
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"message\":\"Login successful\",\"username\":\""
                        + authentication.getName() + "\"}");
                })
                .failureHandler((request, response, exception) -> {
                    response.setStatus(401);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"error\":\"Login failed\",\"message\":\""
                        + exception.getMessage() + "\"}");
                })
                .permitAll()
            )

            // ログアウトの設定
            // 【学習ポイント】
            // ログアウト時は「サーバー側のセッション削除」と
            // 「クライアント側の Cookie 削除」の両方が必要。
            .logout(logout -> logout
                .logoutUrl("/api/auth/logout")
                .logoutSuccessHandler((request, response, authentication) -> {
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"message\":\"Logout successful\"}");
                })
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
            )

            // 未認証時のハンドラー
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(401);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"Authentication required\"}");
                })
            )

            // セッション管理
            // 【学習ポイント】
            // migrateSession: ログイン時に新しいセッション ID を発行。
            // セッション固定攻撃対策。
            .sessionManagement(session -> session
                .sessionFixation().migrateSession()
            );

        return http.build();
    }
}
