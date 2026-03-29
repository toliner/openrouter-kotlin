# Installation

openrouter-kotlin is published through [JitPack](https://jitpack.io/#toliner/openrouter-kotlin).

## Requirements

- **JDK 25+**
- **Kotlin 2.3.10+**

## Gradle (Kotlin DSL)

Add the JitPack repository to your `build.gradle.kts`:

```kotlin
repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}
```

Add the dependency:

```kotlin
dependencies {
    implementation("com.github.toliner:openrouter-kotlin:TAG")
}
```
Replace `TAG` with the latest version (e.g., `v1.0.0`).

## Gradle (Groovy DSL)

Add the JitPack repository to your `build.gradle`:

```groovy
repositories {
    mavenCentral()
    maven { url 'https://jitpack.io' }
}
```

Add the dependency:

```groovy
dependencies {
    implementation 'com.github.toliner:openrouter-kotlin:TAG'
}
```

## Maven

Add the JitPack repository:

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

Add the dependency:

```xml
<dependency>
    <groupId>com.github.toliner</groupId>
    <artifactId>openrouter-kotlin</artifactId>
    <version>TAG</version>
</dependency>
```

## Dependencies Note

openrouter-kotlin includes the **Ktor CIO engine** as a transitive dependency. You don't need to add another Ktor engine unless you specifically want to use a different one (like OKHttp or Apache).
