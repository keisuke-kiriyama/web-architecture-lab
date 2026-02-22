/**
 * 投稿一覧ページ（SSR 版）
 *
 * 【学習ポイント】
 * "use client" を書かない = Server Component（デフォルト）。
 * - サーバー（Node.js）で実行される
 * - fetch はサーバー側で実行される → CORS が発生しない
 * - 完成した HTML がブラウザに送られる
 *
 * 【CSR 版との違い】
 * | 項目 | CSR | SSR |
 * |------|-----|-----|
 * | fetch 実行場所 | ブラウザ | サーバー（Node.js） |
 * | 初回 HTML | 空（Loading...） | データ入り |
 * | CORS | 発生しうる | 発生しない |
 * | useState/useEffect | 使う | 使えない |
 * | SEO | 不利 | 有利 |
 *
 * 【Reviewer 観点】
 * - SSR では API の URL が「Docker 内ホスト名」になる
 * - ブラウザからは localhost:8080 だが、サーバーからは api:8080
 * - 環境変数で切り替える必要がある
 */

// 投稿の型定義
type Post = {
  id: number;
  title: string;
  content: string;
  createdAt: string;
  updatedAt: string;
};

/**
 * API から投稿一覧を取得
 *
 * 【学習ポイント】
 * Server Component では async/await を直接使える。
 * useEffect は不要（というか使えない）。
 *
 * 【重要】API URL の違い
 * - CSR: ブラウザから → localhost:8080（ホストマシン経由）
 * - SSR: サーバー（front コンテナ）から → api:8080（Docker 内通信）
 *
 * 【TypeScript の型について】
 * response.json() は Promise<any> を返す。
 * 戻り値型 Promise<Post[]> は「コンパイラへの宣言」であり、
 * 実行時には検証されない（Dart の fromJson とは異なる）。
 */
async function getPosts(): Promise<Post[]> {
  // サーバー側からの API 呼び出し
  // Docker 内通信では「サービス名」がホスト名になる
  const apiUrl = process.env.SSR_API_URL || "http://api:8080";

  console.log("[SSR] API を呼び出します:", `${apiUrl}/api/posts`);

  const response = await fetch(`${apiUrl}/api/posts`, {
    // キャッシュしない（毎回取得）
    // 【学習ポイント】
    // Next.js の fetch はデフォルトでキャッシュする（force-cache）。
    // 動的なデータは cache: 'no-store' で無効化。
    cache: "no-store",
  });

  if (!response.ok) {
    throw new Error(`API error: ${response.status}`);
  }

  return response.json();
}

/**
 * Server Component のページ
 *
 * 【学習ポイント】
 * - async function として定義できる（Client Component では不可）
 * - データ取得を await で待てる
 * - 取得完了後に HTML を生成してブラウザに送る
 */
export default async function PostsSsrPage() {
  // サーバー側でデータ取得（ブロッキング）
  // この処理が完了するまで HTML は生成されない
  let posts: Post[] = [];
  let error: string | null = null;

  try {
    posts = await getPosts();
    console.log("[SSR] API レスポンス:", posts.length, "件");
  } catch (err) {
    console.error("[SSR] API 呼び出しエラー:", err);
    error = err instanceof Error ? err.message : "不明なエラー";
  }

  // エラー時
  if (error) {
    return (
      <main className="min-h-screen p-8">
        <h1 className="text-2xl font-bold mb-6">投稿一覧（SSR）</h1>
        <div className="p-4 bg-red-100 border border-red-400 text-red-700 rounded">
          <p className="font-bold">エラーが発生しました</p>
          <p>{error}</p>
          <p className="mt-2 text-sm">
            ※ SSR では CORS エラーは発生しません（サーバー間通信のため）
          </p>
        </div>
      </main>
    );
  }

  // 正常時
  // 【学習ポイント】
  // この HTML はサーバーで生成される。
  // ブラウザで「View Source」すると、データが含まれた HTML が見える。
  return (
    <main className="min-h-screen p-8">
      <h1 className="text-2xl font-bold mb-6">投稿一覧（SSR）</h1>

      {/* SSR であることを明示 */}
      <div className="mb-4 p-3 bg-blue-100 border border-blue-400 text-blue-700 rounded text-sm">
        <p>
          <strong>SSR（Server-Side Rendering）</strong>
        </p>
        <p>このページはサーバーで HTML が生成されています。</p>
        <p>View Source で確認すると、データが含まれた HTML が見えます。</p>
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

      {/* CSR 版へのリンク */}
      <div className="mt-8 pt-4 border-t">
        <p className="text-sm text-gray-500">
          <a href="/posts" className="text-blue-600 hover:underline">
            CSR 版と比較する →
          </a>
        </p>
      </div>
    </main>
  );
}
