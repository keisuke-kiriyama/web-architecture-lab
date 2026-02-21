# 実装計画（Implementation Plan）

各フェーズで「何を実装し、何を学ぶか」を整理したドキュメントです。

---

## 全体方針

- 実装スピードより理解の質を優先
- 各フェーズで必ず「観察」と「比較」を行う
- 意図的に壊して挙動を確認する
- コードコメントに設計意図を残す

---

## Phase 1: Webの最小単位を作る（認証なし）

### 目的

Webアプリの全体構造を理解する。

### 実装内容

1. Docker Compose環境構築（front / api / db）
2. Spring Boot で REST API 作成（POST /posts, GET /posts）
3. PostgreSQL にテーブル作成（posts）
4. Next.js からAPIを呼び出す
5. CORSを意図的に発生させ、解決する

### 習得する概念

| 概念 | 説明 |
|------|------|
| HTTP Request/Response | メソッド、ヘッダ、ボディの役割 |
| CORS | Same-Origin Policy と Preflight Request |
| fetch API | ブラウザからのHTTP通信の実態 |
| REST | リソース指向設計の基本 |

### 検証観点

- [ ] ブラウザのNetwork タブでリクエスト/レスポンスを観察できるか
- [ ] CORSエラーが発生する条件を説明できるか
- [ ] Preflight（OPTIONS）リクエストの意味を理解しているか
- [ ] フロント → API → DB の流れを図示できるか

### 非機能観点

| 観点 | チェック項目 |
|------|-------------|
| セキュリティ | 認証なし状態のリスクを認識しているか |
| パフォーマンス | N+1クエリの可能性を意識しているか |
| 運用 | ログ出力は適切か |

---

## Phase 2: 認証導入（Cookieセッション）

### 目的

Webらしさの核心である「セッション」を理解する。

### 実装内容

1. Spring Security 導入
2. ログイン / ログアウト API
3. セッションIDをCookieで管理
4. 認可（自分のデータのみ操作可能）

### 習得する概念

| 概念 | 説明 |
|------|------|
| Cookie | HttpOnly, Secure, SameSite 属性 |
| Session | サーバー側状態管理 |
| 認証 vs 認可 | Authentication と Authorization の違い |
| CSRF | Cross-Site Request Forgery の構造 |

### 検証観点

- [ ] Set-Cookie ヘッダの内容を確認できるか
- [ ] HttpOnly の効果を DevTools で確認できるか
- [ ] SameSite の違いによる挙動差を説明できるか
- [ ] CSRF攻撃のシナリオを説明できるか

### 非機能観点

| 観点 | チェック項目 |
|------|-------------|
| セキュリティ | セッション固定攻撃への対策 |
| セキュリティ | CSRF トークンの実装 |
| 運用 | セッションタイムアウト設定 |

---

## Phase 3: 認証方式比較（JWT）

### 目的

設計レビューで必ず問われる「Cookie vs JWT」を理解する。

### 実装内容

1. JWT発行・検証の実装
2. トークン保存場所の比較（localStorage / Cookie）
3. リフレッシュトークンの検討
4. XSSリスクの整理

### 習得する概念

| 概念 | 説明 |
|------|------|
| JWT | ヘッダ・ペイロード・署名の構造 |
| Stateless認証 | サーバー側に状態を持たない設計 |
| トークン露出 | XSS時のリスク |
| リフレッシュ戦略 | アクセストークン短命化 |

### 検証観点

- [ ] JWT のペイロードをデコードして中身を確認できるか
- [ ] localStorage保存時のXSSリスクを説明できるか
- [ ] Cookie保存時との比較ができるか
- [ ] 設計判断の根拠を説明できるか

### 非機能観点

| 観点 | チェック項目 |
|------|-------------|
| セキュリティ | トークン漏洩時の影響範囲 |
| セキュリティ | 署名アルゴリズムの選択 |
| 運用 | トークン失効の仕組み |

---

## Phase 4: CSR vs SSR + 画面遷移 + Thymeleaf/JSP

### 目的

1. レンダリング戦略（CSR/SSR）の判断軸を持つ
2. Web における画面遷移と値渡しの方式を理解する
3. フロント/バック一体型（Thymeleaf/JSP）と分離型（Next.js）の違いを理解する

### 切り替え方式

**URL パス + プロファイルで切り替え**（同一プロジェクト内で比較可能）

| URL | 方式 | 切り替え方法 |
|-----|------|-------------|
| `localhost:3000/posts` | CSR (Next.js) | - |
| `localhost:3000/posts-ssr` | SSR (Next.js) | - |
| `localhost:8080/view/posts` | Thymeleaf | `SPRING_PROFILES_ACTIVE=jwt,thymeleaf` |
| `localhost:8080/view/posts` | JSP | `SPRING_PROFILES_ACTIVE=jwt,jsp` |

### 実装内容

**4-1: CSR/SSR 比較（Next.js）**:
1. CSR 版（既存 `/posts`）を維持
2. SSR 版（`/posts-ssr`）を新規作成
3. DevTools で初回 HTML、Network を比較
4. 認証（JWT）との組み合わせを検証

**4-2: 画面遷移**:
5. 動的ルート `/posts/[id]` で詳細ページ作成
6. `<Link>` による SPA 遷移の観察
7. `router.push()` によるプログラム的遷移
8. クエリパラメータ `?page=2` の扱い

**4-3: Thymeleaf（フロント/バック一体型）**:
9. Spring Boot に Thymeleaf を追加
10. `/view/posts` でテンプレートレンダリング
11. フォーム処理（PRG パターン）
12. Next.js SSR との比較

**4-4: JSP（レガシー比較）**:
13. JSP ビューリゾルバー設定
14. 同じ Controller で JSP/Thymeleaf 切り替え
15. JSP と Thymeleaf の構文比較

### 習得する概念

| 概念 | 説明 |
|------|------|
| CSR | Client-Side Rendering。ブラウザで HTML 生成 |
| SSR (Next.js) | Server Components でサーバー側 HTML 生成 |
| SSR (Thymeleaf) | Java テンプレートエンジンでサーバー側 HTML 生成 |
| SSR (JSP) | Java Server Pages でサーバー側 HTML 生成（レガシー） |
| Hydration | サーバー HTML にイベントを付与（Next.js のみ） |
| 動的ルート | `/posts/[id]` で URL パラメータ取得 |
| Link | SPA 遷移（リロードなし） |
| router.push | プログラム的な画面遷移 |
| useSearchParams | クエリパラメータの取得 |
| Model | Spring MVC でテンプレートにデータを渡す仕組み |
| PRG パターン | Post-Redirect-Get。フォーム二重送信防止 |

### アーキテクチャ比較

| 観点 | Next.js (CSR/SSR) | Thymeleaf/JSP |
|------|-------------------|---------------|
| 構成 | フロント/バック分離 | フロント/バック一体 |
| API | REST API 経由 | Controller → View 直接 |
| CORS | 発生しうる | 発生しない |
| 状態管理 | useState, Context | HttpSession, Model |
| フォーム | fetch + JSON | HTML form + PRG |
| SPA 遷移 | あり（リロードなし） | なし（毎回リロード） |

### Thymeleaf vs JSP

| 観点 | Thymeleaf | JSP |
|------|-----------|-----|
| 構文 | `th:text="${value}"` | `<%= value %>` or `${value}` (EL) |
| ファイル | `.html`（静的表示可） | `.jsp`（サーバー必須） |
| Spring 推奨 | ◎（公式推奨） | △（非推奨だが現役） |
| IDE プレビュー | 可能 | 不可 |
| XSS 対策 | デフォルトでエスケープ | `<c:out>` が必要 |

### 画面遷移の値渡し方式

| 方式 | 例 | 特徴 |
|------|-----|------|
| URL パラメータ | `/posts/123` | ブックマーク可、SEO 有利 |
| クエリパラメータ | `/posts?page=2` | フィルタ、ページネーション |
| localStorage | - | 永続化、XSS リスク |
| Context | - | コンポーネント間共有、リロードで消える |
| Cookie | - | サーバーでも読める |
| HttpSession | - | Thymeleaf/JSP でサーバー側セッション |
| Flash Attribute | - | リダイレクト後に1回だけ表示（PRG用） |

### 検証観点

**CSR/SSR (Next.js)**:
- [ ] View Source で初期 HTML の違いを確認できるか
- [ ] Network タブで読み込み順序を比較できるか
- [ ] SSR での API 呼び出し URL が Docker 内ホスト名になることを理解しているか
- [ ] 認証との組み合わせ課題を説明できるか
- [ ] SEO 観点での違いを説明できるか

**画面遷移**:
- [ ] Link と a タグの違い（リロード有無）を説明できるか
- [ ] 動的ルートでパラメータを取得できるか
- [ ] クエリパラメータの変更でページ遷移せずに状態更新できるか
- [ ] 値渡し方式の選択基準を説明できるか

**Thymeleaf/JSP**:
- [ ] Model にデータを詰めてテンプレートで表示できるか
- [ ] フォーム送信と PRG パターンを説明できるか
- [ ] XSS 対策（Thymeleaf: 自動、JSP: `<c:out>`）を理解しているか
- [ ] Next.js SSR との違いを説明できるか
- [ ] プロファイルで Thymeleaf/JSP を切り替えられるか

### 非機能観点

| 観点 | チェック項目 |
|------|-------------|
| パフォーマンス | 初回表示速度 |
| パフォーマンス | SPA 遷移 vs フルリロードの体感差 |
| セキュリティ | 認証情報の扱い（SSR で JWT をどう取得するか） |
| セキュリティ | XSS 対策の違い |
| UX | 戻るボタンの挙動、ブックマーク可能性 |

---

## Phase 5: BFFアーキテクチャ

### 目的

境界設計を語れるようにする。

### 実装内容

1. フロントからAPI直接アクセス構成
2. Next.js API Routes をBFFとして配置
3. CORS設定の違いを比較
4. トークン管理の違いを比較

### 習得する概念

| 概念 | 説明 |
|------|------|
| BFF | Backend For Frontend |
| セキュリティ境界 | どこで認証・認可を行うか |
| API Gateway | 集約・認証の責務 |

### 検証観点

- [ ] BFF有無でCORS設定がどう変わるか
- [ ] トークンがブラウザに露出するかどうか
- [ ] フロントとAPIの責務分離を説明できるか

### 非機能観点

| 観点 | チェック項目 |
|------|-------------|
| セキュリティ | 攻撃面の違い |
| パフォーマンス | ホップ数増加の影響 |
| 運用 | 障害切り分けの複雑さ |

---

## Phase 6: パフォーマンスとキャッシュ

### 目的

ボトルネックを推測できるようにする。

### 実装内容

1. Cache-Control ヘッダ調整
2. ETag / Last-Modified 導入
3. 簡易負荷試験
4. SSR / CSR 性能比較

### 習得する概念

| 概念 | 説明 |
|------|------|
| HTTPキャッシュ | Cache-Control, ETag, 304 |
| CDN | エッジキャッシュの役割 |
| Core Web Vitals | LCP, FID, CLS |

### 検証観点

- [ ] 304 Not Modified を発生させられるか
- [ ] キャッシュヒット率を意識した設計ができるか
- [ ] ボトルネックの推測ができるか

### 非機能観点

| 観点 | チェック項目 |
|------|-------------|
| パフォーマンス | キャッシュ戦略の妥当性 |
| 運用 | キャッシュ破棄の仕組み |

---

## Phase 7: フロントの物理法則

### 目的

レンダリングや非同期挙動を説明できる状態へ。

### 実装内容

1. 再レンダリング検証（React DevTools）
2. イベントループ実験（setTimeout, Promise）
3. 不要描画の観察
4. Hydration Mismatch 発生

### 習得する概念

| 概念 | 説明 |
|------|------|
| Event Loop | Call Stack, Task Queue, Microtask |
| React再レンダリング | 何がトリガーになるか |
| Virtual DOM | 差分検出の仕組み |
| Hydration | サーバー/クライアント不整合 |

### 検証観点

- [ ] microtask / macrotask の実行順を説明できるか
- [ ] 不要な再レンダリングを特定できるか
- [ ] Hydration Mismatch の原因を説明できるか

### 非機能観点

| 観点 | チェック項目 |
|------|-------------|
| パフォーマンス | 再描画コストの最小化 |
| UX | 意図しない挙動の防止 |

---

## 更新履歴

| 日付 | 内容 |
|------|------|
| 2025-02-15 | 初版作成 |
| 2025-02-21 | Phase 4 を「CSR vs SSR + 画面遷移 + Thymeleaf/JSP」に拡張 |
