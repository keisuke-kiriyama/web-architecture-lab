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

---

## 更新履歴

| 日付 | 内容 |
|------|------|
| 2025-02-15 | 初版作成（テンプレート） |
| 2025-02-15 | Phase 0: CSR/SSR と SPA/MPA の整理を追加 |
| 2025-02-16 | 用語・概念リストを追加 |
