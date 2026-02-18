package com.example.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS 設定
 *
 * 【学習ポイント】
 * CORS はブラウザのセキュリティ機能。
 * サーバー側で「どのオリジンからのリクエストを許可するか」を設定する。
 *
 * 【Reviewer観点】
 * - allowedOrigins("*") は本番で使わない（セキュリティリスク）
 * - 本番では環境変数で許可オリジンを指定する
 * - 認証が必要な場合は allowCredentials(true) が必要
 */
@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        // 許可するオリジン（開発環境用）
                        .allowedOrigins("http://localhost:3000")
                        // 許可する HTTP メソッド
                        .allowedMethods("GET", "POST", "PUT", "DELETE")
                        // 許可するヘッダー
                        .allowedHeaders("*");
            }
        };
    }
}
