# --- ステージ1: アプリケーションのビルド ---
# MavenとJava 21がインストールされたイメージをベースにする
FROM maven:3.9-eclipse-temurin-21 AS build

# 作業ディレクトリを作成
WORKDIR /app
# プロジェクトのファイルを全てコピー
COPY . .
# Mavenを使ってアプリケーションをビルド（テストはスキップ）
RUN mvn clean package -DskipTests

# --- ステージ2: 実行用の軽量イメージを作成 ---
# JRE（Java実行環境）のみの小さなイメージをベースにする
FROM eclipse-temurin:21-jre-jammy

# 作業ディレクトリを作成
WORKDIR /app
# ステージ1でビルドしたjarファイルだけをコピーしてくる
COPY --from=build /app/target/*.jar app.jar

# アプリケーションが使うポート8080を公開
EXPOSE 8080

# コンテナ起動時に実行するコマンド
ENTRYPOINT ["java", "-jar", "app.jar"]