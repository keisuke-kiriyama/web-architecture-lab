package com.example.api.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

/**
 * posts テーブルに対応するエンティティ
 *
 * 【学習ポイント】
 * JPA のエンティティ = DB テーブルと Java オブジェクトのマッピング定義。
 * アノテーションで「どのテーブル」「どのカラム」に対応するか指定する。
 *
 * 【Reviewer観点】
 * - エンティティをそのまま API レスポンスに使うのは避けるべき（DTO を使う）
 * - 今回は学習用のためシンプルにエンティティを直接返す
 * - 本番では循環参照、不要なフィールド露出のリスクがある
 */
@Entity
@Table(name = "posts")
public class Post {

    /**
     * 主キー
     * GenerationType.IDENTITY = DB の自動採番（SERIAL）を使用
     *
     * 【学習ポイント】
     * PostgreSQL の SERIAL は INTEGER（32ビット）。
     * Java の Long は BIGINT（64ビット）に対応するため、型を合わせる必要がある。
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * タイトル（必須）
     */
    @Column(nullable = false)
    private String title;

    /**
     * 本文
     */
    private String content;

    /**
     * 作成日時
     * insertable=false: INSERT 時にこのフィールドを含めない（DB の DEFAULT を使う）
     * updatable=false: UPDATE 時にこのフィールドを変更しない
     */
    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    /**
     * 更新日時
     */
    @Column(name = "updated_at", insertable = false, updatable = false)
    private OffsetDateTime updatedAt;

    // -------------------------------------------------------------------------
    // Getter / Setter
    // -------------------------------------------------------------------------
    // 【学習ポイント】
    // JPA はリフレクションでフィールドにアクセスするため、
    // Getter/Setter がなくても動作するが、慣例として用意する。

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
