# API リファレンス

本ライブラリの各クラス、関数、プロパティの詳細については、Dokka で生成された API リファレンスを参照してください。

- [API リファレンス (Dokka)](/openrouter-kotlin/api/)

## パッケージ構成

主なパッケージの構成は以下の通りです。

- **`dev.toliner.openrouter.client`**: API クライアント本体と設定。
- **`dev.toliner.openrouter.l1`**: OpenRouter API の JSON 構造をそのまま反映した低レイヤーのデータクラス。
- **`dev.toliner.openrouter.l2`**: 型安全なリクエスト構築のための DSL ビルダー。
- **`dev.toliner.openrouter.error`**: エラーハンドリングのための例外クラス。
- **`dev.toliner.openrouter.streaming`**: ストリーミング（SSE）処理に関連するユーティリティ。

!!! note
    本ライブラリは 3 レイヤーアーキテクチャを採用しており、`l1`（データモデル）、`l2`（DSL）、`client`（HTTP 実装）が明確に分離されています。通常の使用では `client` パッケージと `l2` パッケージを主に利用することになります。
