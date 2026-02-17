package com.example.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot アプリケーションのエントリポイント
 *
 * 【学習ポイント】
 * @SpringBootApplication は以下の3つを組み合わせたアノテーション:
 * - @Configuration: このクラスが設定クラスであることを示す
 * - @EnableAutoConfiguration: Spring Boot の自動設定を有効化
 * - @ComponentScan: このパッケージ以下のコンポーネントを自動検出
 *
 * これだけで Web サーバー（Tomcat）が起動し、REST API を受け付ける状態になる。
 */
@SpringBootApplication
public class ApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiApplication.class, args);
    }
}
