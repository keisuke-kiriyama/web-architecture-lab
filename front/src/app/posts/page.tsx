/**
 * 投稿一覧ページ（CSR + API 呼び出し版）
 *
 * 【学習ポイント】
 * "use client" を指定すると Client Component になる。
 * - Server Component（デフォルト）: サーバーで実行、完成 HTML を返す
 * - Client Component: ブラウザで実行、JS コードがブラウザに送られる
 *
 * CSR では useEffect + fetch で API を呼び出す。
 * fetch はブラウザで実行されるため、CORS が発生する。
 */
"use client";

import { useEffect, useState } from "react";

// 投稿の型定義
type Post = {
  id: number;
  title: string;
  content: string;
  createdAt: string;
  updatedAt: string;
};

// API の URL
// NEXT_PUBLIC_ プレフィックスでブラウザから参照可能
const API_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";

export default function PostsPage() {
  // useState: 状態管理。値が変わると再レンダリングされる
  const [posts, setPosts] = useState<Post[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // useEffect: コンポーネントのマウント時に実行
  // 第2引数 [] = 初回のみ実行
  useEffect(() => {
    const fetchPosts = async () => {
      try {
        console.log("API を呼び出します:", `${API_URL}/api/posts`);

        // fetch: ブラウザの HTTP クライアント
        // 【重要】ここで CORS エラーが発生するはず
        const response = await fetch(`${API_URL}/api/posts`);

        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`);
        }

        const data = await response.json();
        console.log("API レスポンス:", data);
        setPosts(data);
      } catch (err) {
        console.error("API 呼び出しエラー:", err);
        setError(err instanceof Error ? err.message : "不明なエラー");
      } finally {
        setLoading(false);
      }
    };

    fetchPosts();
  }, []);

  // ローディング中
  if (loading) {
    return (
      <main className="min-h-screen p-8">
        <h1 className="text-2xl font-bold mb-6">投稿一覧</h1>
        <p>読み込み中...</p>
      </main>
    );
  }

  // エラー時
  if (error) {
    return (
      <main className="min-h-screen p-8">
        <h1 className="text-2xl font-bold mb-6">投稿一覧</h1>
        <div className="p-4 bg-red-100 border border-red-400 text-red-700 rounded">
          <p className="font-bold">エラーが発生しました</p>
          <p>{error}</p>
          <p className="mt-2 text-sm">
            ※ CORS エラーの場合、DevTools の Console タブで詳細を確認してください
          </p>
        </div>
      </main>
    );
  }

  // 正常時
  return (
    <main className="min-h-screen p-8">
      <h1 className="text-2xl font-bold mb-6">投稿一覧（CSR）</h1>

      {/* CSR であることを明示 */}
      <div className="mb-4 p-3 bg-green-100 border border-green-400 text-green-700 rounded text-sm">
        <p>
          <strong>CSR（Client-Side Rendering）</strong>
        </p>
        <p>このページはブラウザで JavaScript が実行されてデータを取得しています。</p>
        <p>View Source で確認すると、データが含まれていない HTML が見えます。</p>
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

      {/* SSR 版へのリンク */}
      <div className="mt-8 pt-4 border-t">
        <p className="text-sm text-gray-500">
          <a href="/posts-ssr" className="text-blue-600 hover:underline">
            SSR 版と比較する →
          </a>
        </p>
      </div>
    </main>
  );
}
