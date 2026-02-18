# 学習ログ（Learning Log）

つまずき、誤解、気づきを記録するドキュメントです。

---

## 記録方針

1. **つまずいたこと** - 何が起きたか
2. **原因** - なぜ起きたか
3. **解決方法** - どう解決したか
4. **学び** - 何を理解したか
5. **再発防止** - 次回気をつけること

---

## Phase 0: 事前整理

### CSR/SSR と SPA/MPA は別の軸

**疑問**

Phase 1 を始める前に「CSRのWebアプリを作る」という説明を受けたが、SPAとの関係が曖昧だった。「SPAだと必然的にCSRになる」という関係があるのか？

**誤解していたこと**

CSR/SSR と SPA/MPA を混同していた。または「SPA = CSR」と思っていた。

**正しい理解**

これらは **別の軸** の概念である。

| 軸 | 何を決めるか |
|---|---|
| SPA / MPA | ページ遷移の仕組み（リロードするか否か） |
| CSR / SSR | HTMLをどこで生成するか（ブラウザ or サーバー） |

組み合わせは4パターン可能：

|  | SPA | MPA |
|---|---|---|
| CSR | React SPA（典型） | 今回作るもの |
| SSR | Next.js（ハイブリッド） | 従来型PHP/Rails |

**追加の学び：Next.js の「SPA + SSR」の実態**

Next.js は「SSR」と呼ばれることが多いが、実際は：

- **初回アクセス**: SSR（サーバーでHTML生成）
- **ページ遷移後**: CSR（JSでコンポーネント差し替え + API fetch）

つまり **SSR と CSR のハイブリッド** である。「SPA + SSR」と呼ぶのは初回SSRを強調しているだけ。

**Reviewer観点での活用**

- 「SSRだからサーバー負荷が高い」→ 初回だけなら影響限定的
- 「CSRだからSEO不利」→ 初回SSRを入れれば解決可能
- 用語に惑わされず「どのタイミングで、どこでHTMLを生成しているか」を確認する

---

## Phase 1: Webの最小単位

_Phase 1 開始後に記録_

### 例: CORSエラーが発生した

**つまずいたこと**

フロントからAPIを呼び出したら、以下のエラーが発生した。

```
Access to fetch at 'http://localhost:8080/api/posts' from origin 'http://localhost:3000'
has been blocked by CORS policy: No 'Access-Control-Allow-Origin' header is present
on the requested resource.
```

**原因**

（ここに原因を記録）

**解決方法**

（ここに解決方法を記録）

**学び**

（ここに学びを記録）

**再発防止**

（ここに再発防止を記録）

---

## Phase 2: 認証導入（Cookieセッション）

_Phase 2 開始後に記録_

---

## Phase 3: 認証方式比較（JWT）

_Phase 3 開始後に記録_

---

## Phase 4: CSR vs SSR

_Phase 4 開始後に記録_

---

## Phase 5: BFFアーキテクチャ

_Phase 5 開始後に記録_

---

## Phase 6: パフォーマンスとキャッシュ

_Phase 6 開始後に記録_

---

## Phase 7: フロントの物理法則

_Phase 7 開始後に記録_

---

## 誤解していたこと

学習を通じて修正された誤解を記録する。

| 誤解 | 正しい理解 | Phase |
|------|------------|-------|
| SPA = CSR である | CSR/SSR と SPA/MPA は別の軸。組み合わせ可能 | 0 |
| Next.js は SSR | 初回SSR + 遷移後CSR のハイブリッド | 0 |
| _例: JWTは安全_ | 保存場所次第でXSSリスクがある | - |

---

## 用語・概念リスト

学習中に整理した用語と概念。

### レンダリング・アーキテクチャ

| 用語 | 説明 |
|------|------|
| CSR | Client-Side Rendering。ブラウザでJSを実行してHTMLを生成 |
| SSR | Server-Side Rendering。サーバーでHTMLを生成して返す |
| SPA | Single Page Application。単一HTMLで画面遷移をJS制御（リロードなし） |
| MPA | Multi-Page Application。ページ遷移のたびにサーバーからHTML取得 |

### DB関連

| 用語 | 説明 |
|------|------|
| SERIAL | PostgreSQLの自動採番。シーケンス（連番生成器）の省略記法 |
| DEFAULT | INSERT時にカラムを省略した場合に使われる値 |
| ORM | Object-Relational Mapping。オブジェクトとDBテーブルを自動マッピング |
| JPA | Java Persistence API。ORM の仕様（インターフェース） |
| Hibernate | JPA の実装。SQL生成、キャッシュ等を担当 |

### フレームワーク

| 用語 | 説明 |
|------|------|
| Spring | Javaアプリ開発のフレームワーク。DI、AOP等の基盤機能 |
| Spring Boot | Springを「すぐ使える」ようにしたもの。自動設定+組み込みサーバー |
| application.yml | Spring Bootの設定ファイル。自動設定をカスタマイズする |

### React / Next.js

| 用語 | 説明 |
|------|------|
| React | UI を構築するライブラリ。ルーティング機能は持たない |
| Next.js | React + ルーティング + SSR + その他便利機能 |
| 関数コンポーネント | UI を関数で表現する。戻り値（JSX）が画面に表示される |
| JSX | JavaScript 内で HTML 風の構文を書ける拡張 |
| `{}` | JSX 内で JavaScript 式を埋め込む。変数、計算、map 等が使える |
| App Router | Next.js のルーティング方式。`app/` ディレクトリ構造が URL パスになる |
| ファイルベースルーティング | ディレクトリ/ファイル構造が URL に対応する仕組み |
| `export default` | ファイルのメイン出力。App Router は `page.tsx` の `export default` 関数を実行する |
| Server Component | デフォルト。サーバーで実行され、完成した HTML を返す（SSR） |
| Client Component | `"use client"` を書く。ブラウザで実行される（CSR） |
| JSP | Java のテンプレートエンジン。SSR |

### React の状態管理

| 用語 | 説明 |
|------|------|
| `useState` | 状態を管理する Hook。値が変わると再レンダリングが発生する |
| `useEffect` | 副作用（API呼び出し等）を扱う Hook。マウント時や依存値変化時に実行 |
| Hook | `use` で始まる関数。関数コンポーネントに状態や副作用を追加する仕組み |
| 再レンダリング | 状態が変わると、コンポーネント関数が再実行され、UI が更新される |

### useState の動作イメージ

```tsx
const [posts, setPosts] = useState<Post[]>([]);
```

- `posts`: 現在の状態（値）
- `setPosts`: 状態を更新する関数
- `useState([])`: 初期値は空配列

`setPosts(newData)` を呼ぶと：
1. `posts` が `newData` に更新される
2. コンポーネントが再レンダリングされる
3. 新しい `posts` を使って画面が描画される

### useEffect の動作イメージ

```tsx
useEffect(() => {
  // この中のコードが実行される
  fetchPosts();
}, []);  // 依存配列
```

- 依存配列 `[]` = マウント時（初回表示時）のみ実行
- 依存配列 `[userId]` = userId が変わるたびに実行
- 依存配列なし = 毎回のレンダリングで実行（通常は避ける）

### Next.js の Server Component と Client Component

| | Server Component (SSR) | Client Component (CSR) |
|---|---|---|
| 宣言 | 何も書かない（デフォルト） | `"use client"` を先頭に書く |
| 実行場所 | サーバー | ブラウザ |
| 返されるもの | 完成した HTML | HTML + JavaScript（ブラウザで実行） |
| `useState` / `useEffect` | 使えない | 使える |
| fetch の実行場所 | サーバー（CORS なし） | ブラウザ（CORS 発生の可能性） |

### SSR / CSR の使い分け

| SSR が向いている | CSR が向いている |
|-----------------|-----------------|
| SEO が必要（LP、ブログ） | SEO 不要（管理画面） |
| 初回表示速度が重要 | インタラクティブな操作が多い |
| データ更新頻度が低い | リアルタイム更新が必要 |
| 認証不要 or シンプル | 認証後の画面 |

### App Router のルーティング例

| ファイルパス | URL |
|-------------|-----|
| `app/page.tsx` | `/` |
| `app/posts/page.tsx` | `/posts` |
| `app/posts/[id]/page.tsx` | `/posts/1`, `/posts/2` など（動的ルート） |
| `app/about/page.tsx` | `/about` |

### App Router の特殊ファイル

| ファイル名 | 役割 |
|-----------|------|
| `page.tsx` | そのルートで表示されるページ |
| `layout.tsx` | 共通レイアウト（ヘッダー等） |
| `loading.tsx` | ローディング中の表示 |
| `error.tsx` | エラー時の表示 |

### CORS

| 用語 | 説明 |
|------|------|
| CORS | 異なるオリジンへのブラウザからのリクエストを制限する仕組み |
| オリジン | スキーム + ホスト + ポート（1つでも違えば「異なるオリジン」） |

**なぜ必要か**: 悪意あるサイトから他サイトのAPIを勝手に呼び出す攻撃を防ぐため

**ポイント**:
- ブラウザの機能。サーバー間通信・curl には適用されない
- SSR では発生しない（サーバー間通信だから）
- CSR では発生しうる（ブラウザから直接APIを呼ぶから）

### React の再レンダリング範囲

- `useState` で状態更新すると、**その状態を持つコンポーネントだけ**再レンダリング
- ただし親の状態が変わると子も再レンダリングされる
- `useEffect` は依存配列で制御。`[]` ならマウント時のみで再レンダリングでは実行されない

### Spring Boot のアノテーション

| アノテーション | 説明 |
|--------------|------|
| `@Configuration` | 設定クラスであることを示す。Spring が起動時に読み込む |
| `@Bean` | メソッドの戻り値を Spring に管理させる（DI で使える） |

**コンポーネントスキャン**: `@SpringBootApplication` と同じパッケージ以下のクラスは自動検出される。`@Configuration` を書くだけで設定が適用される仕組み。

---

## 更新履歴

| 日付 | 内容 |
|------|------|
| 2025-02-15 | 初版作成（テンプレート） |
| 2025-02-15 | Phase 0: CSR/SSR と SPA/MPA の整理を追加 |
| 2025-02-16 | 用語・概念リストを追加 |
| 2025-02-18 | React / Next.js の基本概念を追加 |
| 2025-02-18 | Server Component / Client Component、SSR/CSR の使い分けを追加 |
| 2025-02-18 | React の状態管理（useState / useEffect）を追加 |
| 2025-02-18 | CORS の概要を追加 |
| 2025-02-19 | React 再レンダリング範囲、Spring Boot アノテーションを追加 |
