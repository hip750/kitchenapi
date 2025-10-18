Kitchen API 仕様書（spec.md）

Version: 1.0
Framework: Spring Boot 3.5.6
Java: 17
DB: PostgreSQL 16（開発）
Test: Testcontainers（PostgreSQL）
Auth: JWT（HS256）
Docs: springdoc-openapi（Swagger UI）

0. 目的

この仕様書は、AI コーディングアシスタント（例：Claude Code）が本リポジトリの構成・要件・API 契約・セキュリティ・テスト方針を正しく理解し、安全にコード生成／変更提案できるようにするためのものです。
以降のセクションで 必須遵守事項・ディレクトリ規約・API 契約・テスト要件・拡張バックログ を定義します。

1. プロジェクト概要

名称: kitchenapi
目的: ユーザーごとの「レシピ」と「パントリー在庫」を管理する REST API を提供します。
主機能:

ユーザー登録／ログイン（JWT 認証）

レシピ CRUD・検索（材料・調理時間・キーワード、ページング対応）

パントリー在庫 CRUD・検索（材料名および賞味期限レンジ、ページング対応）

賞味期限が近い在庫のリマインド・ジョブ（@Scheduled）

Swagger UI による API ドキュメント

1.1 技術スタック

Spring Boot 3.5.6（Web / Data JPA / Security / Validation / Actuator）

Java 17+

DB: PostgreSQL 16（開発時は Docker）

Test: Testcontainers（PostgreSQL コンテナ）

認証: JWT（jjwt 0.11.5, HS256）

API ドキュメンテーション: springdoc-openapi（Swagger UI）

ビルド: Maven（Wrapper 同梱）

プロファイル: dev（開発）, test（統合テスト）

2. ディレクトリ構成（MVC 準拠）
com.example.kitchenapi
├─ KitchenApiApplication.java
├─ common/
│  └─ GlobalExceptionHandler.java
├─ config/
│  └─ OpenApiConfig.java
├─ security/
│  ├─ AppSecurityProps.java
│  ├─ JwtService.java
│  ├─ AuthUser.java
│  ├─ JwtAuthFilter.java
│  └─ SecurityConfig.java
├─ entity/
│  ├─ UserEntity.java
│  ├─ IngredientEntity.java
│  ├─ RecipeEntity.java
│  ├─ RecipeIngredientKey.java
│  ├─ RecipeIngredientEntity.java
│  └─ PantryItemEntity.java
├─ repository/
│  ├─ UserRepository.java
│  ├─ IngredientRepository.java
│  ├─ RecipeRepository.java
│  ├─ RecipeIngredientRepository.java
│  └─ PantryRepository.java
├─ service/
│  ├─ UserService.java
│  ├─ IngredientService.java
│  ├─ RecipeService.java
│  └─ PantryService.java
├─ dto/
│  ├─ AuthDto.java
│  ├─ RecipeDto.java
│  └─ PantryDto.java
├─ controller/
│  ├─ AuthController.java
│  ├─ RecipeController.java
│  └─ PantryController.java
└─ job/
   └─ PantryExpiryJob.java

2.1 不変条件（アシスタント向け指示）

package は必ず com.example.kitchenapi プレフィックスとすること。

Controller は DTO を介して入出力し、Entity を外部に直接返さない。

例外は GlobalExceptionHandler で ProblemDetail を返す流儀に従う。

認証は JWT（Authorization: Bearer <token>）。匿名許可は /auth/** と Swagger/Health のみ。

DB は PostgreSQL。統合テストは Testcontainers の Postgres を使用。

層は Controller → Service → Repository → Entity の一方向依存を維持すること。

3. セットアップ
3.1 pom.xml（抜粋）

Spring Boot 3.5.6 親

主要依存:

spring-boot-starter-web, data-jpa, security, validation, actuator

org.postgresql:postgresql（version 指定なし：Spring Boot に委譲）

org.springdoc:springdoc-openapi-starter-webmvc-ui:2.5.0

io.jsonwebtoken:jjwt-api/impl/jackson:0.11.5

Testcontainers BOM: 1.20.2（junit-jupiter, postgresql）

3.2 設定ファイル

src/main/resources/application.yml

spring:
  profiles:
    active: dev
management:
  endpoints:
    web.exposure.include: health,info
logging:
  level:
    org.hibernate.SQL: warn
    org.hibernate.type.descriptor.sql: warn
app:
  security:
    jwt-secret: "change-this-secret-to-32bytes-minimum-123456"
    jwt-exp-minutes: 120


src/main/resources/application-dev.yml

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/kitchen
    username: kitchen
    password: secret
    hikari:
      maximum-pool-size: 10
  jpa:
    hibernate:
      ddl-auto: update   # 学習用。実務は Flyway で管理
    properties:
      hibernate:
        format_sql: true
        jdbc.time_zone: UTC


src/test/resources/application-test.yml

spring:
  jpa:
    properties:
      hibernate.jdbc.time_zone: UTC

3.3 Docker（開発 DB）

docker-compose.yml

services:
  postgres:
    image: postgres:16
    container_name: kitchen-postgres
    environment:
      POSTGRES_USER: kitchen
      POSTGRES_PASSWORD: secret
      POSTGRES_DB: kitchen
      TZ: UTC
    ports:
      - "5432:5432"
    volumes:
      - pgdata:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL","pg_isready -U kitchen -d kitchen"]
      interval: 5s
      timeout: 3s
      retries: 10
volumes:
  pgdata:


起動

docker compose up -d
./mvnw spring-boot:run


Swagger UI: http://localhost:8080/swagger-ui/index.html

Health: /actuator/health

4. ドメインモデル

UserEntity (1) — RecipeEntity (N)（owner_id）

RecipeEntity (N) — IngredientEntity (N) を RecipeIngredientEntity で中間結合（複合キー）

UserEntity (1) — PantryItemEntity (N)（user_id）

PantryItemEntity — IngredientEntity (1)（ingredient_id）

監査用に createdAt: Instant、賞味期限用に expiresOn: LocalDate を使用

5. セキュリティ（JWT）
5.1 認可

PermitAll: /auth/**, /swagger-ui/**, /v3/api-docs/**, /actuator/health

Authenticated: 上記以外のすべて

5.2 トークン仕様

Header: Authorization: Bearer <JWT>

Claims:

sub = email

uid = userId（Number）

署名: HS256（app.security.jwt-secret は 32 bytes 以上推奨）

6. API 契約
6.1 認証
POST /auth/signup

Request

{ "email": "alice@example.com", "name": "Alice", "password": "pass1234" }


Response 200

{ "id": 1, "email": "alice@example.com", "name": "Alice" }

POST /auth/login

Request

{ "email": "alice@example.com", "password": "pass1234" }


Response 200

{ "token": "<JWT>", "userId": 1, "email": "alice@example.com", "name": "Alice" }

GET /auth/me（要 JWT）

Response 200

{ "id": 1, "email": "alice@example.com", "name": "Alice" }

6.2 レシピ
POST /recipes（要 JWT）

Request

{
  "title": "オニオンスープ",
  "steps": "皮をむく→炒める→煮る",
  "cookTimeMin": 30,
  "tags": "soup,vegetable",
  "ingredients": [
    { "name": "onion", "quantity": "2個" },
    { "name": "butter", "quantity": "20g" }
  ]
}


Response 201（RecipeView）

{
  "id": 10,
  "title": "オニオンスープ",
  "steps": "皮をむく→炒める→煮る",
  "cookTimeMin": 30,
  "tags": "soup,vegetable",
  "ingredients": [
    { "name": "onion", "quantity": "2個" },
    { "name": "butter", "quantity": "20g" }
  ]
}

GET /recipes/{id}（要 JWT）

Response 200: 上記と同様の RecipeView

GET /recipes（要 JWT）

Query（例）: page=0&size=20&sort=createdAt,desc&q=onion&maxTime=45&ingredient=butter
Response 200: Page<RecipeView>

PATCH /recipes/{id}（要 JWT）

Request（例）

{ "cookTimeMin": 25, "tags": "soup,quick" }


Response 200: 更新後の RecipeView

DELETE /recipes/{id}（要 JWT）

Response: 204 No Content

6.3 パントリー
POST /pantry（要 JWT）

Request

{ "ingredientName": "onion", "amount": "1個", "expiresOn": "2025-10-25" }


Response 201

{ "id": 21, "ingredientName": "onion", "amount": "1個", "expiresOn": "2025-10-25" }

GET /pantry（要 JWT）

Query（例）: page=0&size=20&sort=id,desc&ingredient=oni&expFrom=2025-10-01&expTo=2025-10-31
Response 200: Page<PantryView>

PATCH /pantry/{id}（要 JWT）

Request（例）

{ "amount": "2個" }


Response 200: 更新後の PantryView

DELETE /pantry/{id}（要 JWT）

Response: 204 No Content

6.4 バリデーション & エラー

リクエスト DTO は jakarta.validation による検証を実施

エラーは ProblemDetail で返却（400/401/403/404/409 等）

例（400 Body バリデーション）:

{
  "type": "about:blank",
  "title": "Validation failed",
  "status": 400,
  "detail": "[FieldError ...]"
}

7. スケジュール処理

PantryExpiryJob が 毎朝 9:00 に実行（Cron: 0 0 9 * * *）

本日〜3日後に賞味期限の在庫をユーザーごとにログ出力

拡張案: メール／Slack 通知、しきい値日数の外部設定化

8. テスト（統合：Testcontainers）

クラス例: KitchenApiIntegrationIT

@Testcontainers + @Container PostgreSQLContainer<>("postgres:16-alpine")

@DynamicPropertySource で Spring DataSource に JDBC URL 等を注入

スモーク: /auth/signup → /auth/login（JWT 取得）→ /pantry 追加が 201 で返る

実行

./mvnw -q test

9. コーディング規約（生成時の指示）

層の一方向依存（Controller→Service→Repository→Entity）を厳守。逆依存禁止。

DTO 変換は Controller で完結（Service は Entity を返却）。

Null/空文字は検索条件で null 正規化して JPQL に渡す。

Security: 新規エンドポイントは原則 認証必須。匿名許可が必要な場合のみ SecurityConfig を更新。

日時: DB/JPA は UTC。Instant（時刻）と LocalDate（日付）を使い分ける。

例外: IllegalArgumentException / ResponseStatusException を基本とし、GlobalExceptionHandler に集約。

package と import をすべて記載（com.example.kitchenapi）。

API 契約の破壊的変更は不可。必要なら v2 提案＋移行パスを示すこと。

10. 変更時チェックリスト

 package は com.example.kitchenapi か

 DTO の Validation 注釈（@NotBlank, @Email 等）が適切か

 Controller は DTO↔Entity 変換のみ／ビジネスロジックは Service へ寄せたか

 認証要件に応じて SecurityConfig の許可リストを更新したか

 JPA リレーション・外部キー整合は取れているか

 ページング／ソート（Sort.by("field,dir")）の既定に沿っているか

 統合テストを追加し、既存テストはすべてグリーンか

 Swagger UI で新旧レスポンスが契約どおりか

11. 拡張バックログ（受入基準つき）
11.1 Flyway 導入

Done: ddl-auto:update → validate へ変更、db/migration に初期スキーマを追加、起動時に自動適用される。

11.2 期限通知（メール/Slack）

Done: PantryExpiryJob から通知サービスへ依存を分離。3日以内の在庫がある場合に通知が送られ、送信ログが残る。

11.3 画像アップロード（レシピ写真）

Done: 署名付き URL（S3）で PUT/GET。/recipes/{id}/image で署名 URL を返し、CORS 設定・メタデータ保存を実装。

11.4 高度検索

Done: tags の AND/OR、cookTime 範囲、材料名の前方一致、ページングで正しい総件数とページ数が返る。

12. エンドツーエンド例（cURL）
# サインアップ
curl -s -X POST http://localhost:8080/auth/signup \
  -H "Content-Type: application/json" \
  -d '{"email":"alice@example.com","name":"Alice","password":"pass1234"}'

# ログイン → JWT
TOKEN=$(curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"alice@example.com","password":"pass1234"}' | jq -r .token)

# 在庫追加
curl -s -X POST http://localhost:8080/pantry \
  -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" \
  -d '{"ingredientName":"onion","amount":"1個","expiresOn":"2025-10-25"}' | jq

13. 非機能要件

性能: 典型 CRUD＋検索。既定ページサイズは 20。必要に応じて Index（例：ingredient.name）を追加。

セキュリティ: 本番は HTTPS 前提。JWT シークレットは 32 bytes 以上を環境変数で設定。

運用: Actuator Health 監視。スケジュールはタイムゾーン差異に留意（UTC/ローカル）。

14. 起動チェックフロー

docker compose up -d（PostgreSQL 起動）

./mvnw spring-boot:run（アプリ起動）

http://localhost:8080/swagger-ui/index.html
 が表示される

/auth/signup → /auth/login → JWT で /pantry 追加が 201

./mvnw -q test（Testcontainers により Postgres が自動起動し、統合テストがグリーン）

15. 付記（Claude Code への重要指示）

API 契約を破壊しないで変更提案を行うこと。破壊的変更が必要な場合は 移行パス（v1→v2） を提示すること。

新規クラスを追加する際は package・import を完全記載し、テストコードを同時に提案すること。

例外設計・バリデーション・セキュリティの 3 点は本仕様に準拠すること。

大規模変更前には 影響範囲（クラス・エンドポイント・DB） を明示すること。