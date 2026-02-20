-- =============================================================================
-- 初期テーブル作成
-- =============================================================================
--
-- 【学習ポイント】
-- このファイルは PostgreSQL コンテナ起動時に自動実行される。
-- /docker-entrypoint-initdb.d/ にマウントされたスクリプトは
-- アルファベット順に実行される（01_, 02_ のようにプレフィックスを付ける）。
--
-- 【自動挿入の仕組み】
-- - id: SERIAL = シーケンス（自動採番）。nextval() で連番生成。
-- - created_at/updated_at: DEFAULT = INSERT時に省略すると指定値が入る。
-- - 注意: updated_at は UPDATE 時には自動更新されない（トリガーが必要）。
--
-- 【Reviewer観点】
-- - 本番環境ではマイグレーションツール（Flyway, Liquibase）を使うべき
-- - 今回は学習用のため手動 SQL で十分
-- =============================================================================

-- posts テーブル
-- 認証なしの CRUD 対象として使用
CREATE TABLE IF NOT EXISTS posts (
    id SERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- サンプルデータ
INSERT INTO posts (title, content) VALUES
    ('最初の投稿', 'これはサンプルの投稿です。'),
    ('2番目の投稿', 'Docker Compose で環境構築しました。');

-- =============================================================================
-- users テーブル（Phase 2: 認証用）
-- =============================================================================
--
-- 【学習ポイント】
-- パスワードは平文保存NG。BCrypt でハッシュ化して保存する。
-- BCrypt のハッシュ値は約60文字 → VARCHAR(255) で余裕を持たせる。
--
-- 【Reviewer観点】
-- - パスワードカラムは十分な長さがあるか
-- - ユニーク制約が適切に設定されているか
CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- posts に user_id を追加（認可で使用）
-- 【学習ポイント】
-- 外部キー制約で参照整合性を保証する。
-- ON DELETE CASCADE: ユーザー削除時に投稿も削除
ALTER TABLE posts ADD COLUMN IF NOT EXISTS user_id INTEGER REFERENCES users(id) ON DELETE CASCADE
