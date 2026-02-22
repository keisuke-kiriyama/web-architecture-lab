/**
 * 投稿詳細ページ（CSR 版・クエリパラメータ）
 *
 * 【値渡し方式: クエリパラメータ】
 * URL: /posts/detail?id=1
 *
 * 【学習ポイント】
 * - useSearchParams() でクエリパラメータを取得
 * - URL の ? 以降の部分を解析
 *
 * 【パスパラメータとの違い】
 * | 方式 | URL | 用途 |
 * |------|-----|------|
 * | パスパラメータ | /posts/1 | リソース識別（REST 的） |
 * | クエリパラメータ | /posts?id=1 | フィルタ、オプション |
 *
 * 【クエリパラメータの特徴】
 * - 複数パラメータを渡しやすい（?page=2&sort=desc）
 * - オプショナル（省略可能）
 * - フィルタリングやページネーションに適している
 *
 * 【Reviewer 観点】
 * - リソース識別にはパスパラメータを使うべき
 * - クエリパラメータは補助的な情報に使う
 */
"use client";

import { useEffect, useState, Suspense } from "react";
import { useSearchParams } from "next/navigation";
import Link from "next/link";

type Post = {
  id: number;
  title: string;
  content: string;
  createdAt: string;
  updatedAt: string;
};

const API_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";

/**
 * 詳細コンテンツコンポーネント
 *
 * 【学習ポイント】
 * useSearchParams() を使うコンポーネントは Suspense で囲む必要がある。
 * これは Next.js App Router の制約。
 * クエリパラメータはクライアント側でしか取得できないため、
 * サーバーサイドでは Suspense の fallback が表示される。
 */
function PostDetailContent() {
  // useSearchParams: クエリパラメータを取得
  // /posts/detail?id=1 → searchParams.get("id") = "1"
  const searchParams = useSearchParams();
  const id = searchParams.get("id");

  const [post, setPost] = useState<Post | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!id) {
      setError("id パラメータがありません");
      setLoading(false);
      return;
    }

    const fetchPost = async () => {
      try {
        console.log("投稿詳細を取得:", `${API_URL}/api/posts/${id}`);
        const response = await fetch(`${API_URL}/api/posts/${id}`);

        if (!response.ok) {
          if (response.status === 404) {
            throw new Error("投稿が見つかりません");
          }
          throw new Error(`HTTP error! status: ${response.status}`);
        }

        const data = await response.json();
        setPost(data);
      } catch (err) {
        console.error("API 呼び出しエラー:", err);
        setError(err instanceof Error ? err.message : "不明なエラー");
      } finally {
        setLoading(false);
      }
    };

    fetchPost();
  }, [id]);

  if (loading) {
    return <p>読み込み中...</p>;
  }

  if (error) {
    return (
      <>
        <div className="p-4 bg-red-100 border border-red-400 text-red-700 rounded">
          <p className="font-bold">エラーが発生しました</p>
          <p>{error}</p>
        </div>
        <div className="mt-4">
          <Link href="/posts" className="text-blue-600 hover:underline">
            ← 一覧に戻る
          </Link>
        </div>
      </>
    );
  }

  if (!post) {
    return null;
  }

  return (
    <>
      {/* 値渡し方式の説明 */}
      <div className="mb-4 p-3 bg-purple-100 border border-purple-400 text-purple-700 rounded text-sm">
        <p><strong>値渡し方式: クエリパラメータ</strong></p>
        <p>URL: /posts/detail?id={id}</p>
        <p>取得方法: useSearchParams().get(&quot;id&quot;)</p>
        <p>特徴: フィルタ向き、複数パラメータ可、オプショナル</p>
      </div>

      {/* 投稿内容 */}
      <article className="p-6 border rounded-lg">
        <h2 className="text-xl font-semibold mb-4">{post.title}</h2>
        <p className="text-gray-600 whitespace-pre-wrap">{post.content}</p>
        <div className="mt-4 text-sm text-gray-400">
          <p>作成日時: {new Date(post.createdAt).toLocaleString("ja-JP")}</p>
          <p>更新日時: {new Date(post.updatedAt).toLocaleString("ja-JP")}</p>
        </div>
      </article>

      {/* ナビゲーション */}
      <div className="mt-6">
        <Link
          href="/posts"
          className="px-4 py-2 bg-gray-500 text-white rounded hover:bg-gray-600"
        >
          ← 一覧に戻る
        </Link>
      </div>

      {/* 他の方式との比較リンク */}
      <div className="mt-8 pt-4 border-t">
        <p className="text-sm text-gray-500 mb-2">他の値渡し方式と比較:</p>
        <div className="flex gap-2">
          <Link
            href={`/posts/${id}`}
            className="text-blue-600 hover:underline text-sm"
          >
            パスパラメータ版（/posts/{id}）→
          </Link>
        </div>
      </div>
    </>
  );
}

export default function PostDetailQueryPage() {
  return (
    <main className="min-h-screen p-8">
      <h1 className="text-2xl font-bold mb-6">投稿詳細（CSR・クエリパラメータ）</h1>

      {/* Suspense で囲む必要がある */}
      <Suspense fallback={<p>読み込み中...</p>}>
        <PostDetailContent />
      </Suspense>
    </main>
  );
}
