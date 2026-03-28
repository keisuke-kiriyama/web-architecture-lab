/**
 * BFF（Backend for Frontend）としての Route Handler
 *
 * 【学習ポイント】
 * Next.js の Route Handler はサーバーで実行される。
 * ブラウザからのリクエストを受け、Spring Boot API に転送する。
 *
 * 【BFF のメリット】
 * 1. CORS 回避: ブラウザ → Next.js は同一オリジン
 * 2. トークン保護: JWT をサーバー側に閉じ込められる
 * 3. API 集約: 複数 API をまとめて1回で返せる
 *
 * 【構成】
 * ブラウザ (localhost:3000)
 *     ↓ fetch('/api/posts')  ← 同一オリジン、CORS なし
 * Next.js Route Handler (このファイル)
 *     ↓ fetch('http://api:8080/api/posts')  ← サーバー間通信
 * Spring Boot API
 */

import { NextResponse } from "next/server";

// サーバー間通信用の API URL
// Docker 内では 'api' というサービス名で通信
const API_URL = process.env.SSR_API_URL || "http://api:8080";

/**
 * GET /api/posts
 * 投稿一覧を取得
 */
export async function GET() {
  try {
    // サーバー間通信（CORS は発生しない）
    const response = await fetch(`${API_URL}/api/posts`, {
      // キャッシュを無効化（常に最新を取得）
      cache: "no-store",
    });

    if (!response.ok) {
      return NextResponse.json(
        { error: "API request failed" },
        { status: response.status }
      );
    }

    const data = await response.json();
    return NextResponse.json(data);
  } catch (error) {
    console.error("BFF Error:", error);
    return NextResponse.json(
      { error: "Internal server error" },
      { status: 500 }
    );
  }
}

/**
 * POST /api/posts
 * 新規投稿を作成
 *
 * 【学習ポイント】
 * 認証が必要なリクエストの場合、BFF がトークンを付与する。
 * 現状は簡易実装として、クライアントから受け取ったヘッダーをそのまま転送。
 */
export async function POST(request: Request) {
  try {
    const body = await request.json();

    // クライアントから受け取った認証ヘッダーを転送
    // 本番では BFF 側でセッションから JWT を取得する
    const authHeader = request.headers.get("Authorization");

    const response = await fetch(`${API_URL}/api/posts`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        ...(authHeader ? { Authorization: authHeader } : {}),
      },
      body: JSON.stringify(body),
    });

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}));
      return NextResponse.json(
        { error: "API request failed", ...errorData },
        { status: response.status }
      );
    }

    const data = await response.json();
    return NextResponse.json(data, { status: 201 });
  } catch (error) {
    console.error("BFF Error:", error);
    return NextResponse.json(
      { error: "Internal server error" },
      { status: 500 }
    );
  }
}
