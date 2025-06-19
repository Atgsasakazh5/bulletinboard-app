# 掲示板アプリケーション APIサーバー

## 概要

Spring Bootを用いて開発した、基本的なCRUD機能とJWT認証機能を備えた掲示板アプリケーションのバックエンドAPIです。

## 作成目的

JavaおよびSpring Bootを用いたWebアプリケーション開発スキル、特にデータベース操作、API設計、セキュリティ、テストに関する実践的な知識を習得し、就職活動用のポートフォリオとして提示するために作成しました。

## 主な機能一覧

- **投稿機能 (Posts)**
  - 全件取得 (GET /api/posts)
  - 1件取得 (GET /api/posts/{id})
  - 新規作成 (POST /api/posts)
  - 更新 (PUT /api/posts/{id})
  - 削除 (DELETE /api/posts/{id})
- **認証機能 (Authentication)**
  - ユーザー登録 (POST /api/auth/signup)
  - ログイン・JWT発行 (POST /api/auth/login)

## 使用技術（技術スタック）

- **バックエンド:**
  - Java 21
  - Spring Boot 3.5
  - Spring Web
  - Spring Data JPA / Hibernate
  - Spring Security (JWT認証)
- **データベース:**
  - H2 (ローカル開発用)
  - PostgreSQL (本番環境用)
- **テスト:**
  - JUnit 5
  - Mockito
  - MockMvc
- **インフラ・デプロイ:**
  - Docker
  - Render
- **その他ツール:**
  - Git / GitHub
  - Maven

## APIエンドポイント一覧

| Method | URL | 説明 | 認証 |<br><br>
| GET    | `/api/posts` | 全ての投稿を取得 | 不要 |<br>
| GET    | `/api/posts/{id}` | 指定したIDの投稿を1件取得 | 不要 |<br>
| POST   | `/api/posts` | 新しい投稿を作成 | 必要 (JWT) |<br>
| PUT    | `/api/posts/{id}` | 指定したIDの投稿を更新 | 必要 (JWT) |<br>
| DELETE | `/api/posts/{id}` | 指定したIDの投稿を削除 | 必要 (JWT) |<br>
| POST   | `/api/auth/signup` | 新規ユーザー登録 | 不要 |<br>
| POST   | `/api/auth/login` | ログインしてJWTを発行 | 不要 |<br>

## セットアップ・起動方法

1.  **リポジトリをクローン:**
    ```bash
    git clone https://github.com/あなたのユーザー名/リポジトリ名.git
    ```
2.  **ローカル設定:**
    `src/main/resources/` に `application.properties.example` があります。これをコピーして `application.properties` を作成し、必要に応じて設定してください。（ローカルではH2データベースを使用するため、特別な設定は不要です）
3.  **アプリケーションの実行:**
    IDE（Eclipseなど）から`BulletinboardAppApplication.java`を実行します。

## 今後の課題・改善点

-  一覧取得APIへのページネーション機能の実装
-  画像アップロード機能の追加
-  UI/UXの改善