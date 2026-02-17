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

-- 【学習ポイント】
-- Phase 2 で認証を導入する際、以下のようなカラムを追加する：
-- - user_id: 投稿者（認可で使用）
-- 今は認証なしのため、誰でも全データにアクセス可能
