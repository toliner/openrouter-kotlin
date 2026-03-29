# エラーハンドリングガイド

本ライブラリでは、API 通信時に発生したエラーを `OpenRouterException` という sealed クラスの階層で扱います。

## 例外の階層構造

すべての例外は `OpenRouterException` を継承しており、HTTP ステータスコードに応じて以下のサブクラスに分類されます。

- **`BadRequest` (400)**: リクエストが不正。
- **`Unauthorized` (401)**: API キーが無効。
- **`PaymentRequired` (402)**: クレジット不足。
- **`Forbidden` (403)**: アクセス拒否。
- **`RequestTimeout` (408)**: タイムアウト。
- **`TooManyRequests` (429)**: レートリミット到達。
- **`InternalServerError` (500)**: サーバー内部エラー。
- **`BadGateway` (502)**: プロバイダー側の一時的なエラー。
- **`ServiceUnavailable` (503)**: サービス利用不可。
- **`StreamError`**: ストリーミング中の通信エラー。
- **`InBandError`**: HTTP ステータスは成功（200）だが、レスポンス内容にエラーが含まれていた場合。

## リトライの判定

すべての `OpenRouterException` には、`isRetryable` という拡張プロパティが用意されています。これにより、一時的なエラー（502, 503, 408 など）かどうかを簡単に判断できます。

```kotlin
try {
    val response = client.chat { ... }
} catch (e: OpenRouterException) {
    if (e.isRetryable) {
        // 数秒待ってからリトライするロジック
    } else {
        // 恒久的なエラーとしてログを出す
    }
}
```

## レートリミット（429）への対応

`TooManyRequests` 例外には `retryAfter` プロパティが含まれる場合があります。これがある場合、指定された秒数待機してからリトライすることを推奨します。

```kotlin
try {
    client.chat { ... }
} catch (e: OpenRouterException.TooManyRequests) {
    val waitSeconds = e.retryAfter ?: 60L
    delay(waitSeconds * 1000L)
    // 再実行
}
```

## エラー詳細の取得

例外オブジェクトから、API が返したエラーメッセージやプロバイダー固有のエラー情報を取得できます。

```kotlin
catch (e: OpenRouterException) {
    println("Error code: ${e.error.code}")
    println("Error message: ${e.error.message}")
    e.error.metadata?.providerName?.let { println("Provider: $it") }
}
```
