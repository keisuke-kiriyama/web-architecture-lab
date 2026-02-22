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

### Cookie セッション認証の流れ

```
1. ログイン（POST /api/auth/login）
   ↓
2. サーバーがセッションを作成（メモリ上の Map に保存）
   ↓
3. Set-Cookie: JSESSIONID=xxx をレスポンス
   ↓
4. ブラウザが Cookie を保存
   ↓
5. 以降のリクエストで Cookie: JSESSIONID=xxx を自動送信
   ↓
6. サーバーがセッション ID でユーザーを特定
```

### Cookie とは

**ブラウザに保存され、リクエストのたびに自動送信されるデータ。**

| ヘッダー | 方向 | 意味 |
|---------|------|------|
| `Set-Cookie` | サーバー → ブラウザ | 「これを保存して」 |
| `Cookie` | ブラウザ → サーバー | 「保存してたやつ送るよ」 |

### セッションの保存場所

| 場所 | 特徴 |
|------|------|
| メモリ（デフォルト） | 高速。サーバー再起動で消える |
| Redis / DB | 永続化可能。複数サーバーで共有可能 |

### ログアウト時の処理

```java
.invalidateHttpSession(true)   // サーバー側：セッション削除
.deleteCookies("JSESSIONID")   // クライアント側：Cookie 削除指示
```

両方必要な理由：
- サーバー側だけ → Cookie が残り、無駄なリクエストが発生
- クライアント側だけ → サーバーにセッションが残りセキュリティリスク

### Spring Security が自動提供するエンドポイント

| エンドポイント | 処理 |
|---------------|------|
| `/api/auth/login` | Spring Security（設定で URL 指定） |
| `/api/auth/logout` | Spring Security（設定で URL 指定） |
| `/api/auth/register` | 自分で実装 |

### 認可（Authorization）

**認証と認可の違い**:
- 認証：「誰か」を確認する（ログイン）
- 認可：「何ができるか」を確認する（権限チェック）

**実装パターン**:
```java
// 所有者チェック
private boolean isOwner(Post post, Principal principal) {
    User currentUser = getCurrentUser(principal);
    return post.getUser().getId().equals(currentUser.getId());
}
```

**Reviewer 観点**:
- 認可チェック漏れがないか（編集・削除の両方で確認）
- ID で比較しているか（username より安全）
- 所有者がいない場合（null）の考慮があるか

**Principal について**:
- `java.security.Principal` は Java 標準インターフェース
- `getName()` しか持たない（汎用性のため）
- ID を直接取得するにはカスタム UserDetails が必要

### CSRF（Cross-Site Request Forgery）

**攻撃の仕組み**:
1. ユーザーが bank.com にログイン（Cookie 取得）
2. ユーザーが evil.com を訪問
3. evil.com に仕込まれた form が bank.com に POST
4. ブラウザが Cookie を自動送信 → 認証が通る
5. 勝手に送金などが実行される

**なぜ起きるか**: Cookie は「そのドメインへのリクエスト」に自動付与されるため。

**対策（CSRF トークン）**:
1. サーバーがランダムなトークンを Cookie で送信（XSRF-TOKEN）
2. フロントエンドが Cookie を読み取りヘッダーに設定（X-XSRF-TOKEN）
3. サーバーが Cookie とヘッダーを比較

**なぜ有効か**: 別オリジンは Cookie を「読めない」ため、ヘッダーに設定できない。

### CSRF と CORS の違い

| | CSRF | CORS |
|---|------|------|
| **防ぎたいこと** | 操作を実行させる | データを読み取る |
| **攻撃の例** | 勝手に送金 | 残高情報を盗む |
| **対策の主体** | サーバー（トークン検証） | ブラウザ（レスポンス遮断） |

**CORS で CSRF を防げない理由**:
- CORS はレスポンスの読み取りを制限するが、リクエスト送信は止められない
- 攻撃者はレスポンスを読めなくても、操作が実行されれば目的達成

---

## Phase 3: 認証方式比較（JWT）

### JWT とは

**JSON Web Token**: ユーザー情報を含む署名付きトークン。

```
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VybmFtZSI6InRlc3QiLCJleHAiOjE3MDg1MDAwMDB9.署名
└───────────────┬───────────────┘ └───────────────────┬────────────────────┘ └─┬─┘
              Header                              Payload                  Signature
```

| 部分 | 内容 | エンコード |
|------|------|-----------|
| Header | アルゴリズム情報（alg: HS256 等） | Base64（復号可能） |
| Payload | ユーザー情報、有効期限等 | Base64（復号可能） |
| Signature | Header + Payload を秘密鍵で署名 | 改ざん検知用 |

**重要**: Payload は暗号化ではなく Base64 エンコード。誰でも読める。機密情報は入れない。

### Cookie セッション vs JWT

| | Cookie セッション | JWT |
|---|---|---|
| **状態管理** | ステートフル（サーバーが保持） | ステートレス（クライアントが保持） |
| **認証の仕組み** | セッションIDでサーバー内を参照 | トークン自体に情報が含まれる |
| **スケーラビリティ** | サーバー間でセッション共有が必要 | 共有不要（署名検証のみ） |
| **即時無効化** | 可能（サーバーで削除） | 困難（有効期限まで有効） |

### 複数サーバーでのセッション共有

Cookie セッションで複数サーバー構成にする場合の選択肢:

| パターン | 仕組み | 採用率 |
|----------|--------|--------|
| **Redis** | セッションを共有ストレージに保存 | ◎ 最も一般的 |
| Sticky Session | 同じユーザーを同じサーバーに振り分け | △ 減少傾向 |
| DB | セッションを DB に保存 | ○ 追加インフラ不要 |

**JWT のメリット**: サーバーが状態を持たないので、共有インフラが不要。

### JWT で即時無効化できない理由

```
【Cookie セッション】
サーバー: セッション ID → ユーザー情報 のマップを保持
無効化:   マップから削除 → 即座に無効

【JWT】
サーバー: 何も保持しない（ステートレス）
検証:     署名が正しいか + 有効期限内か
無効化:   「このトークンを発行したか」を覚えていないので無効化できない
```

### ログアウトの実態

```
【通常のログアウト】
クライアントが JWT を削除 → ユーザーは認証できなくなる → OK

【問題になるケース】
1. ユーザーがログイン（JWT 取得）
2. 攻撃者が JWT を盗む（XSS 等）
3. ユーザーがログアウト（クライアントで JWT 削除）
4. 攻撃者は盗んだ JWT をまだ使える ← 問題
```

**結論**: 通常のログアウトは問題ないが、トークン漏洩時に即時無効化できない。

### JWT で即時無効化が必要な場合の対策

| 方法 | 仕組み | トレードオフ |
|------|--------|--------------|
| ブラックリスト | 無効化した JWT を Redis に記録 | ステートレスのメリットが薄れる |
| 短い有効期限 + リフレッシュトークン | アクセストークン 15分、リフレッシュ 7日 | 複雑になる |
| トークンバージョン管理 | ユーザーごとにバージョンを持つ | DB 参照が必要 |

### Cookie セッション vs JWT の使い分け

**Cookie セッション向き**:
- 単一サーバー / 小規模
- セッション即時無効化が必須（銀行・決済系）
- ブラウザのみがクライアント

**JWT 向き**:
- マイクロサービス / 複数サーバー（セッション共有インフラ不要）
- モバイルアプリも対象
- 外部サービス連携（OAuth）
- サーバーレス環境

### JWT は「新しい」のか？

| 時期 | 主流 |
|------|------|
| 2000年代〜 | Cookie セッション（PHP, Java EE, Rails） |
| 2010年代〜 | JWT 登場（OAuth 2.0, SPA の普及） |
| 現在 | 用途に応じて使い分け |

**「JWT だからモダン」「Cookie だから古い」ではなく、要件に合った選択が重要。**

最近は「JWT 万能論への反省」もあり、「Cookie セッション + Redis で十分では？」という回帰も見られる。

### Bearer トークンとは

HTTP リクエストで認証情報を送る方式の一つ。

```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**「Bearer」= 「持参人」**。このトークンを持っている人（Bearer）を認証するという意味。

**重要: Bearer = JWT ではない**

| トークン形式 | 例 |
|-------------|-----|
| JWT | `eyJhbGciOi...`（自己完結型、署名検証で認証） |
| Opaque Token | `abc123xyz`（意味のない文字列、サーバーで lookup） |
| API Key | `sk_live_xxx`（固定キー） |

Bearer はあくまで「Authorization ヘッダーの形式」であり、トークンの中身は何でもよい。

**Reviewer 観点**:
- 「Bearer トークン」と言われたら、JWT かどうかを確認する
- Opaque Token の場合はサーバー側でセッション管理が必要（ステートフル）

### 認証方式のレイヤー

認証方式は「同じ粒度」ではなく、複数のレイヤーで整理すると理解しやすい。

```
【レイヤー1: 認証の主体（誰が認証するか）】
├── 自サービスで認証（ID/Password を自分で管理）
│   ├── Basic 認証
│   ├── Form 認証
│   └── API Key
└── 外部サービスに認証を委任
    ├── OAuth 2.0（認可プロトコル、認証にも流用）
    └── OIDC（OAuth 2.0 + 認証レイヤー）

【レイヤー2: 認証状態の保持方法】
├── Cookie セッション（ステートフル）
└── JWT（ステートレス）
```

| 認証方式 | 説明 | ユースケース |
|---------|------|-------------|
| Basic 認証 | ID/PW を Base64 エンコードしてヘッダーで送信 | 簡易API、内部ツール |
| Form 認証 | HTMLフォームで ID/PW を POST | 一般的な Web サイト |
| OAuth 2.0 | 外部サービス（Google等）に認可を委任 | 「Google でログイン」 |
| OIDC | OAuth 2.0 に認証レイヤーを追加した標準規格 | エンタープライズ SSO |

**OAuth 2.0 と OIDC の違い**:
- OAuth 2.0: 「認可」の仕組み。本来は「Google Drive へのアクセス許可」のようなリソースアクセス用
- OIDC: OAuth 2.0 の上に「認証」を追加。ID Token でユーザー情報を取得

**よくある混同**:
- 「OAuth でログイン」→ 実際は OAuth 2.0 を認証に「流用」している
- 厳密にはログイン用途なら OIDC を使うべき

### Form 認証（フォーム認証）

**HTMLフォームで ID/Password を送信する認証方式。**

```html
<form action="/login" method="POST">
  <input type="text" name="username" />
  <input type="password" name="password" />
  <button type="submit">ログイン</button>
</form>
```

**特徴**:
- RFC などの正式な仕様はない（一般的な慣例）
- Basic 認証との違い: ブラウザの認証ダイアログではなく、自作のログインフォームを使う
- Spring Security の `formLogin()` はこの方式を自動設定する

**Basic 認証 vs Form 認証**:

| | Basic 認証 | Form 認証 |
|---|-----------|-----------|
| UI | ブラウザ標準ダイアログ | 自作HTMLフォーム |
| 送信方法 | Authorization ヘッダー | POST ボディ |
| カスタマイズ | 不可 | 可能（デザイン自由） |
| ログアウト | 困難（ブラウザがキャッシュ） | 容易（セッション破棄） |

**Reviewer 観点**:
- Basic 認証は HTTPS 必須（Base64 は暗号化ではない）
- Form 認証は CSRF 対策が必要

### HMAC-SHA256（署名アルゴリズム）

JWT の署名に使われるアルゴリズム。

```
HMAC = Hash-based Message Authentication Code
SHA256 = ハッシュアルゴリズム

署名 = HMAC-SHA256(Header + "." + Payload, 秘密鍵)
```

**なぜ署名が必要か**:
- Payload は Base64 で誰でも読める
- 署名がないと改ざんし放題（`"role": "user"` → `"role": "admin"`）
- 署名があれば、秘密鍵を持つサーバーだけが検証できる

### application.properties

Spring Boot の設定ファイル。

```properties
# 固定値
server.port=8080

# 環境変数から注入（本番運用）
jwt.secret=${JWT_SECRET}
```

**Git 管理**:
- `application.properties` 自体は Git 管理する
- 秘密情報は環境変数で注入（`.env` は `.gitignore`）

---

## Phase 4: CSR vs SSR + 画面遷移 + Thymeleaf/JSP

### Server Component と Client Component

Next.js App Router では、コンポーネントの「実行場所」が異なる。

```
【Server Component（デフォルト）】
実行場所: Node.js サーバー
↓
HTML を生成してブラウザに送る
↓
ブラウザは「完成した HTML」を受け取るだけ

【Client Component】
"use client" を宣言
↓
実行場所: ブラウザ（JavaScript エンジン）
↓
空の HTML + JavaScript がブラウザに送られる
↓
ブラウザが JavaScript を実行して DOM を構築
```

### なぜ Server Component で useState/useEffect が使えないか

| Hook | 目的 | 前提 |
|------|------|------|
| `useState` | ブラウザのメモリに状態を保持 | 継続的に動作 |
| `useEffect` | マウント後・状態変化後に実行 | 継続的に動作 |

**Server Component は「1回実行して HTML を生成して終わり」**。
ブラウザで継続的に動作しないので、これらの Hook は意味がない。

```tsx
// Client Component（ブラウザで実行）
"use client";
import { useState, useEffect } from "react";

export default function Page() {
  const [count, setCount] = useState(0);  // ブラウザのメモリに状態を保持

  useEffect(() => {
    document.title = `Count: ${count}`;  // ブラウザで実行
  }, [count]);

  return <button onClick={() => setCount(count + 1)}>Click</button>;
}
```

```tsx
// Server Component（サーバーで実行）
// "use client" なし

export default async function Page() {
  // サーバーで1回実行されて終わり
  const data = await fetch("http://api:8080/api/posts");

  // HTML を生成してブラウザに送る
  // その後、このコードは二度と実行されない
  return <div>{data}</div>;
}
```

### なぜ Server Component で async/await が使えるか

| コンポーネント | async 使用 | 理由 |
|---------------|-----------|------|
| Client Component | 不可 | React のレンダリングは同期的。Promise を返すと処理できない |
| Server Component | 可能 | サーバーは「データが揃ってから HTML を送る」ので待機できる |

**Client Component で非同期処理をするには useEffect を使う**:
```tsx
"use client";
useEffect(() => {
  // マウント後に非同期処理を実行
  fetch('/api/posts').then(res => res.json()).then(setData);
}, []);
```

### Server Component と Client Component の比較

| 項目 | Server Component | Client Component |
|------|-----------------|------------------|
| 宣言 | なし（デフォルト） | `"use client"` |
| 実行場所 | Node.js サーバー | ブラウザ |
| useState | 使えない | 使える |
| useEffect | 使えない | 使える |
| async/await | 使える | 使えない |
| onClick 等 | 使えない | 使える |
| 初回 HTML | データ入り | 空（Loading...） |
| SEO | 有利 | 不利 |
| 用途 | 初期データ取得、静的表示 | インタラクティブ UI |

### Next.js の fetch キャッシュ

Server Component での fetch は、Next.js が自動でキャッシュを管理する。

| オプション | 動作 | ユースケース |
|-----------|------|-------------|
| `cache: 'force-cache'` | キャッシュ（デフォルト） | 静的データ、マスタデータ |
| `cache: 'no-store'` | 毎回取得 | 動的データ、ユーザー固有データ |
| `next: { revalidate: 60 }` | 60秒ごとに再取得 | 更新頻度が低いデータ |

```tsx
// 毎回取得（動的）
const posts = await fetch('/api/posts', { cache: 'no-store' });

// 60秒ごとに再検証
const news = await fetch('/api/news', { next: { revalidate: 60 } });

// キャッシュを使用（静的）- デフォルト
const categories = await fetch('/api/categories');
```

**キャッシュの効果**:
```
【cache: 'no-store'】
ユーザーA → サーバー → API → DB → レスポンス
ユーザーB → サーバー → API → DB → レスポンス
（毎回 API 呼び出し）

【cache: 'force-cache'】
ユーザーA → サーバー → API → DB → レスポンス → キャッシュ保存
ユーザーB → サーバー → キャッシュから返す（API 呼び出しなし）
（初回のみ API 呼び出し）
```

**注意**: Client Component の fetch は Next.js キャッシュとは無関係（ブラウザの通常の fetch）。

### TypeScript の型システムと API レスポンス

```tsx
async function getPosts(): Promise<Post[]> {
  const response = await fetch(...);
  return response.json();  // 実行時の型: any
}
```

**`response.json()` は `Promise<any>` を返す。**
TypeScript は **コンパイル時の型チェックのみ** で、**実行時には型情報が消える**。

| 言語 | 型チェック | API レスポンスの扱い |
|------|-----------|---------------------|
| **Dart** | コンパイル時 + 実行時 | `fromJson` で明示的マッピング必要 |
| **TypeScript** | コンパイル時のみ | 不要（信じるだけ） |

```dart
// Dart: 明示的なマッピングが必要
final dynamic json = jsonDecode(response.body);
return (json as List).map((e) => Post.fromJson(e)).toList();
```

```tsx
// TypeScript: 戻り値型の宣言だけで OK（実行時検証なし）
async function getPosts(): Promise<Post[]> {
  return response.json();  // any を Post[] として「信じる」
}
```

**本番コードでは Zod 等で実行時バリデーションを入れることが推奨。**

### SSR での API URL の違い

| 方式 | fetch 実行場所 | API URL |
|------|---------------|---------|
| CSR | ブラウザ | `localhost:8080`（ホストマシン経由） |
| SSR | サーバー（front コンテナ） | `api:8080`（Docker 内通信） |

SSR では「サーバー間通信」になるため、Docker 内のサービス名を使う。
環境変数で切り替える設計が必要。

```tsx
// SSR での fetch
const apiUrl = process.env.SSR_API_URL || "http://api:8080";
const data = await fetch(`${apiUrl}/api/posts`);
```

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
| JWT は Cookie より安全 | 保存場所次第。localStorage は XSS に弱い | 3 |
| JWT ならログアウトできない | クライアントで削除すれば通常は OK。問題は「漏洩時に無効化できない」こと | 3 |
| JWT は新しいからモダン | 用途次第。Cookie セッション + Redis 回帰の動きもある | 3 |
| Bearer トークン = JWT | Bearer はヘッダー形式。中身は JWT でも Opaque Token でもよい | 3 |
| OAuth/OIDC と JWT は同じ粒度 | 認証の「主体」と「状態保持」は別レイヤー | 3 |
| Server Component で useState が使える | 使えない。サーバーで1回実行して終わりなので状態管理は不要 | 4 |
| Client Component で async/await が使える | 使えない。React のレンダリングは同期的。useEffect で代用 | 4 |

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
| ORM | Object-Relational Mapping。オブジェクトとDBテーブルを対応させる概念 |
| JPA | Java Persistence API。Java 用の ORM 標準仕様（インターフェース） |
| Hibernate | JPA の実装。アノテーションを読み取り、実際の DB 操作を行う |
| Spring Data JPA | JPA（Hibernate）をさらに使いやすくした Spring のライブラリ |

**ORM と JPA の関係**:
- ORM は概念（言語を問わない）
- JPA は Java 用の ORM 仕様
- Hibernate は JPA の実装

**JPA アノテーション**（`jakarta.persistence.*`）:

| アノテーション | 説明 |
|---------------|------|
| `@Entity` | このクラスは DB テーブルに対応する |
| `@Table(name = "xxx")` | 対応するテーブル名を指定 |
| `@Id` | 主キー |
| `@GeneratedValue` | 主キーの自動生成方法（IDENTITY = DB の自動採番） |
| `@Column` | カラムの詳細設定（nullable, unique, length など） |

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

### Spring Security

| 用語 | 説明 |
|------|------|
| Spring Security | 認証・認可フレームワーク。依存関係追加だけで全エンドポイントが保護される |
| 認証 (Authentication) | 「誰か」を確認する（ログイン） |
| 認可 (Authorization) | 「何ができるか」を確認する（権限チェック） |
| UserDetailsService | ユーザー名からユーザー情報を取得するインターフェース |
| UserDetails | 認証に必要な情報（username, password, 権限等）を持つオブジェクト |

**設計思想**: デフォルトで安全、必要に応じて緩める

- 依存関係を追加した瞬間、全 API が認証必須になる
- 明示的に「公開」と設定しない限り保護される
- 逆（デフォルト無効）だと設定漏れがセキュリティホールになる

**デフォルト動作**:
- ユーザー名: `user`（固定）
- パスワード: 起動時にログに出力される（毎回変わる）
- `docker compose logs api | grep "security password"` で確認可能

**UserDetailsService の役割**:
- Spring Security は「ユーザー情報がどこにあるか」を知らない
- 開発者が `loadUserByUsername(username)` を実装して `UserDetails` を返す
- Spring Security がパスワード比較・セッション作成を自動で行う

```
ログインリクエスト
    ↓
loadUserByUsername(username) ← 開発者が実装
    ↓
UserDetails を返す
    ↓
Spring Security がパスワード比較 → 認証成功/失敗
```

### パスワードの保存

**絶対に平文保存しない。** DB 漏洩時に全ユーザーのパスワードが露出する。

| 用語 | 説明 |
|------|------|
| ハッシュ化 | パスワードを元に戻せない形式に変換 |
| ソルト | ユーザーごとに異なるランダム文字列。ハッシュ化時に付加する |
| BCrypt | パスワードハッシュ専用アルゴリズム。Spring Security のデフォルト |

**ソルトの目的**: レインボーテーブル攻撃を防ぐ
- レインボーテーブル = 事前に計算した「パスワード → ハッシュ値」の対応表
- ソルトがあると、そのソルト専用の対応表を一から作る必要がある
- ソルトは秘密にする必要がない（ハッシュ値に含まれていてもOK）

**BCrypt の特徴**:
- ソルトがハッシュ値に埋め込まれる（別カラム不要）
- 意図的に遅い（辞書攻撃対策）
- コスト係数で計算回数を調整可能

```
$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZRGwW7MnXJpvjH.Y0.Zo6FLaYvFua
└─┬─┘└┬┘└──────────┬─────────┘└──────────────┬──────────────┘
  │   │            │                         │
  │   │            └── ソルト（22文字）       └── ハッシュ本体
  │   └── コスト係数（2^10回の計算）
  └── アルゴリズム識別子
```

**Reviewer 観点**:
- BCrypt または Argon2 を使っているか
- SHA-256 / MD5 は **絶対NG**（高速すぎて総当たりに弱い）
- 平文パスワードをログに出力していないか

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
| 2025-02-19 | Spring Security の基本を追加 |
| 2025-02-20 | パスワード保存（ハッシュ化・ソルト・BCrypt）を追加 |
| 2025-02-20 | JPA・ORM・Hibernate の関係、JPA アノテーションを追加 |
| 2025-02-21 | UserDetailsService の役割を追加 |
| 2025-02-21 | Cookie セッション認証の流れ、ログアウト処理を追加 |
| 2025-02-21 | 認可（Authorization）の実装パターンを追加 |
| 2025-02-21 | CSRF の仕組みと対策、CORS との違いを追加 |
| 2025-02-21 | Phase 3: JWT の概念、Cookie セッションとの比較を追加 |
| 2025-02-21 | Bearer トークン、認証方式のレイヤー、Form 認証を追加 |
| 2025-02-21 | Phase 4: Server/Client Component、Next.js fetch キャッシュを追加 |
