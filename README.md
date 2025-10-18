# キッチン管理API

Spring Boot製のキッチン管理Webアプリケーション - レシピと在庫を一元管理

<br>

## 概要

このアプリケーションは、家庭のキッチンにおけるレシピと在庫を効率的に管理するためのWebアプリケーションです。

ユーザーは以下のことができます:
1. アカウントを作成してログイン
2. レシピの登録・閲覧・削除
3. 材料と調理手順の詳細管理
4. 在庫アイテムの登録・閲覧・削除
5. 賞味期限の追跡と期限切れ間近アイテムの確認
6. ダッシュボードで統計情報を一目で確認

<br>

## 使用技術

### バックエンド
- **Java 17/25** - プログラミング言語
- **Spring Boot 3.5.6** - アプリケーションフレームワーク
- **Spring Security + JWT** - 認証・認可 (HS256アルゴリズム)
- **Spring Data JPA** - データアクセス層
- **Hibernate** - ORM
- **PostgreSQL 16** - データベース
- **Maven** - ビルドツール
- **Docker Compose** - コンテナ管理

### フロントエンド
- **HTML5/JavaScript (ES5)** - UI実装
- **TailwindCSS (CDN)** - CSSフレームワーク
- **Axios** - HTTPクライアント

<br>

## 機能詳細

### 認証機能
- ユーザー登録 (メールアドレス、パスワード、名前)
- ログイン/ログアウト
- JWTトークンによるステートレス認証
- トークン自動リフレッシュとエラーハンドリング

### ダッシュボード
- レシピ総数の表示
- 在庫アイテム数の表示
- 期限切れ間近アイテム数のハイライト表示 (7日以内)

### レシピ管理
- レシピの新規作成 (タイトル、説明、作り方、材料)
- 複数材料の登録 (材料名と分量)
- 調理時間とタグの設定
- レシピ一覧のカード表示
- レシピの削除機能
- ページネーション対応

### 在庫管理
- 在庫アイテムの登録 (材料名、数量、賞味期限)
- 在庫一覧の表示
- 賞味期限の自動判定
  - 期限切れ: 赤色ハイライト
  - 期限切れ間近 (7日以内): 黄色ハイライト
- 在庫アイテムの削除
- ページネーション対応

### データ管理
- スケジュールジョブによる期限切れアイテムの自動削除 (毎日午前3時)
- PostgreSQLによる永続化
- JPA/Hibernateによる自動スキーマ生成

<br>

## セットアップ

### 前提条件
- Java 17以上
- Docker & Docker Compose
- Maven 3.9以上

### インストール手順

1. リポジトリをクローン
```bash
git clone https://github.com/hip750/kitchenapi.git
cd kitchenapi
```

2. PostgreSQLデータベースを起動
```bash
docker compose up -d
```

3. アプリケーションをビルド
```bash
./mvnw clean package -DskipTests
```

4. アプリケーションを起動
```bash
./mvnw spring-boot:run
```

5. ブラウザでアクセス
```
http://localhost:8080
```

<br>

## 環境変数

開発環境では以下のデフォルト設定が使用されます (`application-dev.yml`):

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/kitchendb
    username: kitchen_user
    password: kitchen_password

security:
  jwt:
    secret-key: your-256-bit-secret-key-here
    expiration-hours: 2
```

本番環境では環境変数で設定を上書きしてください:
- `DB_URL` - データベースURL
- `DB_USERNAME` - データベースユーザー名
- `DB_PASSWORD` - データベースパスワード
- `JWT_SECRET_KEY` - JWT署名用シークレットキー (256ビット以上推奨)
- `JWT_EXPIRATION_HOURS` - トークン有効期限 (時間単位)

<br>

## API仕様

### 認証エンドポイント
- `POST /api/auth/signup` - ユーザー登録
- `POST /api/auth/login` - ログイン

### レシピエンドポイント
- `GET /api/recipes` - レシピ一覧取得 (ページネーション対応)
- `GET /api/recipes/{id}` - レシピ詳細取得
- `POST /api/recipes` - レシピ作成
- `PUT /api/recipes/{id}` - レシピ更新
- `DELETE /api/recipes/{id}` - レシピ削除

### 在庫エンドポイント
- `GET /api/pantry` - 在庫一覧取得 (ページネーション対応)
- `GET /api/pantry/{id}` - 在庫アイテム詳細取得
- `POST /api/pantry` - 在庫アイテム作成
- `PUT /api/pantry/{id}` - 在庫アイテム更新
- `DELETE /api/pantry/{id}` - 在庫アイテム削除

すべてのエンドポイント (認証を除く) はJWT Bearer認証が必要です。

<br>

## テスト

統合テストの実行:
```bash
./mvnw test
```

テストカバレッジ:
- 認証機能の統合テスト
- レシピCRUD操作の統合テスト
- 在庫管理機能の統合テスト
- JWT認証フローのテスト

<br>

## プロジェクト構成

```
kitchenapi/
├── src/
│   ├── main/
│   │   ├── java/com/example/kitchenapi/
│   │   │   ├── config/          # セキュリティ・OpenAPI設定
│   │   │   ├── controller/      # RESTコントローラー
│   │   │   ├── dto/             # データ転送オブジェクト
│   │   │   ├── entity/          # JPAエンティティ
│   │   │   ├── exception/       # 例外ハンドラー
│   │   │   ├── repository/      # Spring Data リポジトリ
│   │   │   ├── scheduler/       # スケジュールジョブ
│   │   │   └── service/         # ビジネスロジック
│   │   └── resources/
│   │       ├── static/          # フロントエンド (HTML/JS)
│   │       └── application*.yml # 設定ファイル
│   └── test/                    # 統合テスト
├── docker-compose.yml           # PostgreSQL設定
└── pom.xml                      # Maven設定
```

<br>

## 今後の機能拡張予定

- [ ] レシピ検索機能 (材料名、タグ、調理時間)
- [ ] レシピと在庫の連携 (必要材料の自動確認)
- [ ] レシピ評価・お気に入り機能
- [ ] 買い物リスト自動生成
- [ ] 画像アップロード機能
- [ ] ユーザープロフィール管理
- [ ] モバイルアプリ対応

<br>

## ライセンス

このプロジェクトはMITライセンスの下で公開されています。
