/**
 * 投稿一覧ページ（BFF 経由版）
 *
 * 【学習ポイント: BFF（Backend for Frontend）】
 * このページは Next.js の Route Handler（/api/posts）を経由して
 * Spring Boot API からデータを取得する。
 *
 * 【構成の比較】
 * 直接アクセス（/posts）:
 *   ブラウザ → Spring Boot API (localhost:8080)
 *   → HTML取得元とfetch先のオリジンが違う → CORS 設定が必要
 *
 * BFF 経由（このページ）:
 *   ブラウザ → Next.js BFF (localhost:3000/api/posts) → Spring Boot API
 *   → HTML取得元とfetch先が同一オリジン → CORS 不要
 *
 * 【BFF のメリット】
 * 1. CORS 回避: ブラウザと BFF は同一オリジン
 * 2. トークン保護: JWT を BFF サーバー側に閉じ込め、ブラウザには HttpOnly Cookie だけ渡す
 * 3. API 集約: 複数 API をまとめて1回で返せる
 */
"use client";

import { useEffect, useState } from "react";
import Link from "next/link";

type Post = {
  id: number;
  title: string;
  content: string;
  createdAt: string;
  updatedAt: string;
};

export default function PostsBffPage() {
  const [posts, setPosts] = useState<Post[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchPosts = async () => {
      try {
        // 【重要】BFF 経由でアクセス
        // /api/posts は Next.js の Route Handler（同一オリジン）
        // → CORS は発生しない
        console.log("BFF を呼び出します: /api/posts");

        const response = await fetch("/api/posts");

        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`);
        }

        const data = await response.json();
        console.log("BFF レスポンス:", data);
        setPosts(data);
      } catch (err) {
        console.error("BFF 呼び出しエラー:", err);
        setError(err instanceof Error ? err.message : "不明なエラー");
      } finally {
        setLoading(false);
      }
    };

    fetchPosts();
  }, []);

  if (loading) {
    return (
      <main className="min-h-screen p-8">
        <h1 className="text-2xl font-bold mb-6">投稿一覧（BFF 経由）</h1>
        <p>読み込み中...</p>
      </main>
    );
  }

  if (error) {
    return (
      <main className="min-h-screen p-8">
        <h1 className="text-2xl font-bold mb-6">投稿一覧（BFF 経由）</h1>
        <div className="p-4 bg-red-100 border border-red-400 text-red-700 rounded">
          <p className="font-bold">エラーが発生しました</p>
          <p>{error}</p>
        </div>
      </main>
    );
  }

  return (
    <main className="min-h-screen p-8">
      <h1 className="text-2xl font-bold mb-6">投稿一覧（BFF 経由）</h1>

      {/* BFF 経由であることを明示 */}
      <div className="mb-4 p-3 bg-blue-100 border border-blue-400 text-blue-700 rounded text-sm">
        <p>
          <strong>BFF（Backend for Frontend）経由</strong>
        </p>
        <p>このページは <code>/api/posts</code>（Next.js Route Handler）を経由しています。</p>
        <p>ブラウザと BFF は同一オリジン（localhost:3000）なので CORS は発生しません。</p>
        <p className="mt-2">
          <strong>リクエストの流れ:</strong><br />
          ブラウザ → Next.js BFF (/api/posts) → Spring Boot API (api:8080)
        </p>
      </div>

      {/* Network タブでの確認方法 */}
      <div className="mb-4 p-3 bg-yellow-100 border border-yellow-400 text-yellow-700 rounded text-sm">
        <p><strong>確認方法（DevTools の Network タブ）:</strong></p>
        <ul className="list-disc ml-5 mt-1">
          <li>リクエスト先が <code>localhost:3000/api/posts</code> になっている</li>
          <li>CORS 関連のエラーが出ていない</li>
          <li>Preflight（OPTIONS）リクエストがない</li>
        </ul>
      </div>

      {posts.length === 0 ? (
        <p>投稿がありません</p>
      ) : (
        <ul className="space-y-4">
          {posts.map((post) => (
            <li key={post.id} className="p-4 border rounded-lg">
              <h2 className="text-xl font-semibold">{post.title}</h2>
              <p className="text-gray-600 mt-2">{post.content}</p>
            </li>
          ))}
        </ul>
      )}

      {/* 比較リンク */}
      <div className="mt-8 pt-4 border-t space-y-2">
        <p className="text-sm text-gray-500">
          <Link href="/posts" className="text-blue-600 hover:underline">
            ← 直接 API 版（CORS あり）と比較する
          </Link>
        </p>
        <p className="text-sm text-gray-500">
          <Link href="/posts-ssr" className="text-blue-600 hover:underline">
            → SSR 版と比較する
          </Link>
        </p>
      </div>
    </main>
  );
}
