package com.example.api.repository;

import com.example.api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * users テーブルへのアクセスを担当するリポジトリ
 *
 * 【学習ポイント】
 * Spring Data JPA の「メソッド名からクエリを自動生成」機能を使う。
 * findByUsername(String username) と書くだけで、
 * SELECT * FROM users WHERE username = ? が自動生成される。
 *
 * JpaRepository を継承するだけで、実装クラスが自動生成される。
 * @Repository は省略可能だが、明示性のために付けている。
 *
 * 【Reviewer観点】
 * - ユーザー検索は認証で頻繁に使われるため、username にインデックスがあるか確認
 * - Optional を返すことで null チェック漏れを防ぐ
 */
@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    /**
     * ユーザー名でユーザーを検索
     * Spring Security の UserDetailsService で使用
     *
     * @param username ユーザー名
     * @return ユーザー（存在しない場合は empty）
     */
    Optional<User> findByUsername(String username);
}
