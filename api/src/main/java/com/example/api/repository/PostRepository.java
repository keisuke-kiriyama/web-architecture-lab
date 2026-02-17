package com.example.api.repository;

import com.example.api.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * posts テーブルへのアクセスを担当するリポジトリ
 *
 * 【学習ポイント】
 * JpaRepository を継承するだけで、基本的な CRUD メソッドが自動生成される:
 * - findAll(): 全件取得
 * - findById(id): ID で1件取得
 * - save(entity): 保存（INSERT または UPDATE）
 * - deleteById(id): ID で削除
 *
 * SQL を書かずに DB 操作ができる。
 * ただし、複雑なクエリは @Query アノテーションで JPQL/SQL を書く必要がある。
 *
 * 【Reviewer観点】
 * - findAll() は全件取得なのでデータ量が多いとパフォーマンス問題
 * - 本番ではページネーション（Pageable）を使うべき
 */
@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    // 基本的な CRUD は JpaRepository が提供するため、何も書かなくて良い
    // カスタムクエリが必要な場合はここにメソッドを追加する
}
