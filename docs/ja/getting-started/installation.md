# インストール

`openrouter-kotlin` は JitPack を通じて提供されています。

## Gradle (Kotlin DSL)

`build.gradle.kts` にリポジトリと依存関係を追加します。

```kotlin
repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    implementation("com.github.toliner:openrouter-kotlin:TAG")
}
```

!!! note
    `TAG` の部分は、[最新のリリース](https://github.com/toliner/openrouter-kotlin/releases)に合わせて置き換えてください（例: `v0.1.0`）。

## Gradle (Groovy)

`build.gradle` に以下を追加します。

```groovy
repositories {
    mavenCentral()
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.toliner:openrouter-kotlin:TAG'
}
```

## Maven

`pom.xml` にリポジトリと依存関係を追加します。

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.github.toliner</groupId>
        <artifactId>openrouter-kotlin</artifactId>
        <version>TAG</version>
    </dependency>
</dependencies>
```

## 必要要件

- **JDK**: 25 以上
- **Kotlin**: 2.3.10 以上

## 推奨エンジン

本ライブラリは内部で Ktor を使用しています。デフォルトの構成では `ktor-client-cio` が含まれており、特に設定することなく非同期通信が可能です。
