# Web Architecture Lab

Webアーキテクチャを「TechLead / Reviewer」の視点で理解するための学習リポジトリです。

---

## 目的

機能を完成させることではなく、**設計判断の根拠を説明できる状態**になることを目指します。

### ゴール

- ブラウザ ↔ フロント ↔ API ↔ DB の構造を説明できる
- HTTPの流れとヘッダの役割を説明できる
- Cookie / JWT の責務とリスクを語れる
- CORSがなぜ発生するかを構造的に説明できる
- CSRとSSRの設計判断を説明できる
- BFFの必要性と境界設計を語れる
- Webパフォーマンスのボトルネックを推測できる
- フロントエンド固有のセキュリティリスクを指摘できる

---

## 技術スタック

| 領域 | 技術 |
|------|------|
| Frontend | Next.js (App Router) + TypeScript |
| Backend | Spring Boot (Java) + REST API |
| Database | PostgreSQL |
| Infrastructure | Docker Compose |

---

## 現在の構成

```
web-architecture-lab/
├── CLAUDE.md              # AI向け運用指針
├── README.md              # このファイル
├── docs/
│   ├── implementation-plan.md   # フェーズ別実装計画
│   ├── architecture-notes.md    # 設計意思決定ログ
│   └── learning-log.md          # 学習ログ
├── front/                 # Next.js（未実装）
├── api/                   # Spring Boot（未実装）
└── docker-compose.yml     # ローカル環境（未実装）
```

---

## ブランチ構成と検証テーマ

| ブランチ | 検証テーマ | 状態 |
|----------|------------|------|
| `main` | 基本構成（CSR + Cookie認証） | 未実装 |
| `feature/jwt-auth` | JWT認証への切り替え比較 | 未実装 |
| `feature/ssr` | SSR実装との比較 | 未実装 |
| `feature/bff` | BFFパターンの導入 | 未実装 |

---

## ローカルセットアップ

### 前提条件

- Docker / Docker Compose
- Node.js 20+
- Java 21+

### 起動方法

```bash
# 全サービス起動
docker compose up -d

# フロントエンド: http://localhost:3000
# API: http://localhost:8080
# DB: localhost:5432
```

（※ 現在は未実装のため起動不可）

---

## 学習フェーズ

詳細は [docs/implementation-plan.md](docs/implementation-plan.md) を参照。

| Phase | テーマ | 状態 |
|-------|--------|------|
| 1 | Webの最小単位（認証なしCRUD） | 未着手 |
| 2 | 認証導入（Cookieセッション） | 未着手 |
| 3 | 認証方式比較（JWT） | 未着手 |
| 4 | CSR vs SSR | 未着手 |
| 5 | BFFアーキテクチャ | 未着手 |
| 6 | パフォーマンスとキャッシュ | 未着手 |
| 7 | フロントの物理法則 | 未着手 |

---

## 学びの要約

（各フェーズ完了後に更新）

### Phase 1

_未着手_

### Phase 2

_未着手_

---

## 関連ドキュメント

- [実装計画](docs/implementation-plan.md)
- [設計意思決定](docs/architecture-notes.md)
- [学習ログ](docs/learning-log.md)
