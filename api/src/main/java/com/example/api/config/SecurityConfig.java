package com.example.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Spring Security の共通設定クラス
 *
 * 【学習ポイント】
 * 認証方式（Cookie セッション / JWT）に関わらず必要な共通設定。
 * 具体的な SecurityFilterChain はプロファイルごとに別クラスで定義。
 *
 * プロファイルによる切り替え：
 * - SPRING_PROFILES_ACTIVE=cookie → CookieSecurityConfig が有効
 * - SPRING_PROFILES_ACTIVE=jwt    → JwtSecurityConfig が有効
 *
 * 【Reviewer観点】
 * - 共通設定と認証方式固有の設定が適切に分離されているか
 * - プロファイル切り替えで動作が変わることが明確か
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
     * Cookie セッションでも JWT でも同じエンコーダーを使う。
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
     * AuthenticationManager の Bean 化
     *
     * 【学習ポイント】
     * AuthenticationManager は認証処理の中核。
     * ログインエンドポイントで username/password を検証するために使う。
     * Spring Security 6.x では明示的に Bean 化する必要がある。
     *
     * JWT 認証のログインエンドポイントで使用する。
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
