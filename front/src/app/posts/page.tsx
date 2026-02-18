/**
 * 投稿一覧ページ（静的バージョン）
 *
 * 【学習ポイント】
 * まずは API 呼び出しなしで View だけを作成する。
 * これにより、View の問題と API の問題を切り分けやすくなる。
 *
 * 次のステップで fetch を追加して API から取得する。
 */

// ダミーデータ（後で API から取得するように変更）
const dummyPosts = [
  { id: 1, title: "最初の投稿", content: "これはダミーデータです。" },
  { id: 2, title: "2番目の投稿", content: "まだ API には接続していません。" },
];

export default function PostsPage() {
  return (
    <main className="min-h-screen p-8">
      <h1 className="text-2xl font-bold mb-6">投稿一覧</h1>

      {/* 投稿リスト */}
      <ul className="space-y-4">
        {dummyPosts.map((post) => (
          <li key={post.id} className="p-4 border rounded-lg">
            <h2 className="text-xl font-semibold">{post.title}</h2>
            <p className="text-gray-600 mt-2">{post.content}</p>
          </li>
        ))}
      </ul>

      {/* ステータス表示 */}
      <p className="mt-8 text-sm text-gray-500">
        ※ 現在はダミーデータを表示しています（API 未接続）
      </p>
    </main>
  );
}
