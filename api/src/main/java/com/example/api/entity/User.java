package com.example.api.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

/**
 * users テーブルに対応するエンティティ
 *
 * 【学習ポイント】
 * Spring Security と連携するため、UserDetails インターフェースを
 * 実装することが多いが、今回はシンプルにエンティティのみ定義。
 * 別途 UserDetailsService で変換する（責務の分離）。
 *
 * 【Reviewer観点】
 * - password フィールドを API レスポンスに含めないこと（DTO で除外）
 * - toString() でパスワードを出力しないこと
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * ユーザー名（ログインID）
     * ユニーク制約あり
     */
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    /**
     * パスワード（BCrypt ハッシュ）
     * 【重要】平文を保存してはいけない
     */
    @Column(nullable = false)
    private String password;

    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    // -------------------------------------------------------------------------
    // Getter / Setter
    // -------------------------------------------------------------------------

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
