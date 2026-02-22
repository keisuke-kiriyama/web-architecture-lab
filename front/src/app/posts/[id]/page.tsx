/**
 * 投稿詳細ページ（CSR 版・パスパラメータ）
 *
 * 【値渡し方式: パスパラメータ】
 * URL: /posts/[id] → /posts/1, /posts/42 など
 *
 * 【学習ポイント】
 * - [id] フォルダ名が動的セグメントになる
 * - useParams() でパスパラメータを取得
 * - REST 的なリソース識別（URL がリソースを表す）
 *
 * 【特徴】
 * - ブックマーク可能
 * - SEO に有利（URL が意味を持つ）
 * - リソース識別に適している
 *
 * 【Reviewer 観点】
 * - パスパラメータ: リソース識別（/posts/1）
 * - クエリパラメータ: フィルタ・オプション（?page=2）
 */
"use client";

import { useEffect, useState } from "react";
import { useParams } from "next/navigation";
import Link from "next/link";

type Post = {
  id: number;
  title: string;
  content: string;
  createdAt: string;
  updatedAt: string;
};

const API_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";

export default function PostDetailPage() {
  // useParams: 動的ルートのパスパラメータを取得
  // /posts/[id] の [id] 部分が params.id に入る
  const params = useParams();
  const id = params.id as string;

  const [post, setPost] = useState<Post | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
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

    if (id) {
      fetchPost();
    }
  }, [id]);

  if (loading) {
    return (
      <main className="min-h-screen p-8">
        <h1 className="text-2xl font-bold mb-6">投稿詳細</h1>
        <p>読み込み中...</p>
      </main>
    );
  }

  if (error) {
    return (
      <main className="min-h-screen p-8">
        <h1 className="text-2xl font-bold mb-6">投稿詳細</h1>
        <div className="p-4 bg-red-100 border border-red-400 text-red-700 rounded">
          <p className="font-bold">エラーが発生しました</p>
          <p>{error}</p>
        </div>
        <div className="mt-4">
          <Link href="/posts" className="text-blue-600 hover:underline">
            ← 一覧に戻る
          </Link>
        </div>
      </main>
    );
  }

  if (!post) {
    return null;
  }

  return (
    <main className="min-h-screen p-8">
      <h1 className="text-2xl font-bold mb-6">投稿詳細（CSR）</h1>

      {/* 値渡し方式の説明 */}
      <div className="mb-4 p-3 bg-blue-100 border border-blue-400 text-blue-700 rounded text-sm">
        <p><strong>値渡し方式: パスパラメータ</strong></p>
        <p>URL: /posts/{id}</p>
        <p>取得方法: useParams().id</p>
        <p>特徴: ブックマーク可、SEO 有利、REST 的</p>
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
            href={`/posts/detail?id=${id}`}
            className="text-purple-600 hover:underline text-sm"
          >
            クエリパラメータ版（/posts/detail?id={id}）→
          </Link>
        </div>
      </div>
    </main>
  );
}
