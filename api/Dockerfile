# =============================================================================
# Spring Boot 開発用 Dockerfile
# =============================================================================
#
# 【学習ポイント】
# 開発環境ではソースコードをマウントし、変更を即座に反映させる。
# 本番環境では JAR をビルドしてコピーする（マルチステージビルド）。
#
# 【Reviewer観点】
# - 開発用と本番用で Dockerfile を分けるか、マルチステージにするか
# - 今回は開発用のみ（学習目的）
# =============================================================================

FROM eclipse-temurin:21-jdk

WORKDIR /app

# Gradle Wrapper をコピー
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# 依存関係をダウンロード（キャッシュ効率のため先に実行）
RUN ./gradlew dependencies --no-daemon || true

# ソースコードをコピー（開発時は volume でマウントされるため上書きされる）
COPY src src

# アプリケーション起動
# bootRun = Spring Boot の開発用起動コマンド（ホットリロード対応）
CMD ["./gradlew", "bootRun", "--no-daemon"]
