# Set Up CI/CD with GitHub Actions and JitPack for openrouter-kotlin

This ExecPlan is a living document. The sections `Progress`, `Surprises & Discoveries`, `Decision Log`, and `Outcomes & Retrospective` must be kept up to date as work proceeds.

This document must be maintained in accordance with `.agent/PLANS.md`.

## Purpose / Big Picture

After this change, every push and pull request to the `openrouter-kotlin` repository will automatically compile the project and run all tests, with results visible in the GitHub Actions UI. When a maintainer pushes a Git tag matching the pattern `v*.*.*`, a GitHub Release will be created automatically with generated release notes, and JitPack will be able to build and serve the library as a Maven artifact. Any Gradle user will then be able to depend on this library by adding `com.github.toliner:openrouter-kotlin:TAG` to their dependencies and pointing at the JitPack repository.

Today, none of this infrastructure exists. There is no `.github` directory, no CI workflow, no publishing configuration, and no `jitpack.yml`. The `build.gradle.kts` file contains only the application build logic with no `maven-publish` plugin. This plan creates the complete CI/CD pipeline from scratch.

## Progress

- [ ] Milestone 1: `build.gradle.kts` updated with `maven-publish` plugin, publishing configuration, and `GenerateModuleMetadata` disabled.
- [ ] Milestone 2: `jitpack.yml` created with JDK 25 SDKMAN configuration.
- [ ] Milestone 3: `.github/workflows/ci.yml` created and validated.
- [ ] Milestone 4: `.github/workflows/release.yml` created and validated.
- [ ] Milestone 5: End-to-end dry-run validation of the full pipeline.

## Surprises & Discoveries

(To be populated as work proceeds.)

## Decision Log

- Decision: Use `gradle/actions/setup-gradle` v5.0.2 instead of v6.0.1.
  Rationale: v6 introduced closed-source caching components governed by Gradle's Terms of Use rather than MIT. v5.0.2 remains fully MIT-licensed and is still maintained. For an open-source library with no special caching needs, v5 is the safer, more transparent choice.
  Date/Author: Plan authoring.

- Decision: Pin all GitHub Actions by full commit SHA rather than by floating tag.
  Rationale: SHA pinning prevents supply-chain attacks where a tag could be force-pushed to point at malicious code. The comment after each SHA records the human-readable version for auditability. This is an industry best practice used by projects like Node.js, TypeScript, and Home Assistant.
  Date/Author: Plan authoring.

- Decision: Disable Gradle Module Metadata generation via `tasks.withType<GenerateModuleMetadata> { enabled = false }`.
  Rationale: JitPack has a known bug (jitpack/jitpack.io#5349) where it regenerates `.module` files from the POM, silently dropping dependency `<exclusions>`. When Gradle consumers resolve dependencies, they prefer `.module` files over POM files, so the corrupted metadata takes priority and breaks transitive dependency exclusion. Disabling `.module` generation forces Gradle consumers to fall back to the POM, which JitPack handles correctly. This is the officially recommended workaround from the JitPack issue tracker.
  Date/Author: Plan authoring.

- Decision: Use `java { withSourcesJar(); withJavadocJar() }` instead of custom Dokka-based javadoc generation.
  Rationale: Adding Dokka would introduce a new plugin dependency and significant build complexity. The built-in `withJavadocJar()` generates a jar from the Java-style javadoc task, which for a Kotlin-only project will contain minimal content but satisfies the Maven Central and JitPack convention of shipping a javadoc artifact. This keeps the build simple and can be upgraded to Dokka in a future plan if richer documentation is desired.
  Date/Author: Plan authoring.

- Decision: Use JDK 25 via SDKMAN in `jitpack.yml` with `sdk install java 25-open` rather than relying on the `jdk:` field.
  Rationale: JitPack's `jdk:` field is unreliable for non-standard JDK versions — labels like `openjdk25` may resolve to dead URLs (issue #6479), and the `jdk:` field has a known bug (#7075) that overwrites the `$VERSION` environment variable with the Java version string. Using SDKMAN's `sdk install` and `sdk use` commands in `before_install` is the reliable pattern used by projects that need JDK 21+.
  Date/Author: Plan authoring.

- Decision: Use `25-open` as the SDKMAN identifier rather than `25-tem`.
  Rationale: The OpenJDK builds from jdk.java.net are available under the identifier `25-open` in SDKMAN and are guaranteed to exist for GA releases. Temurin (`25-tem`) is also available but is published by a third party (Adoptium). Either works; we choose `25-open` for simplicity and directness. The project's `build.gradle.kts` uses `jvmToolchain(25)` which is distribution-agnostic; the foojay toolchain resolver in `settings.gradle.kts` will handle auto-provisioning at build time regardless of which JDK is active.
  Date/Author: Plan authoring.

- Decision: CI workflow does not set `cache: gradle` on `actions/setup-java`.
  Rationale: `gradle/actions/setup-gradle` manages its own Gradle User Home cache. Using `actions/setup-java`'s `cache: gradle` option simultaneously would cause cache collisions and unpredictable behavior. The setup-gradle documentation explicitly warns against this.
  Date/Author: Plan authoring.

- Decision: The release workflow triggers JitPack's build by sending an HTTP request to the JitPack build log URL after creating the GitHub Release.
  Rationale: JitPack normally builds on first consumer request, which means the first user to depend on a new version experiences a long wait. Pre-triggering the build immediately after release ensures the artifact is warm and ready. The HTTP request is fire-and-forget (we allow failure) because JitPack may take time to detect the new tag.
  Date/Author: Plan authoring.

- Decision: Adopt an atomic commit strategy with one commit per milestone.
  Rationale: Each commit represents a self-contained, independently verifiable unit of work. If any step needs to be reverted, it can be done cleanly without entangling unrelated changes. The commit order follows the dependency chain: build configuration first (needed by everything), then JitPack configuration (depends on build config), then CI workflow (depends on nothing but benefits from build config being correct), then release workflow (depends on all prior work).
  Date/Author: Plan authoring.

## Outcomes & Retrospective

(To be populated upon completion.)

## Context and Orientation

The `openrouter-kotlin` project is a Kotlin JVM library that provides a client for the OpenRouter API. It lives at `github.com/toliner/openrouter-kotlin`. The project uses Gradle 9.2.0 with the Kotlin DSL for build configuration, Kotlin 2.3.10 as the language, and targets JVM 25 via the `jvmToolchain(25)` directive.

The relevant files are:

- `build.gradle.kts` — The main build script. Contains plugin declarations (`kotlin("jvm")` and `kotlin("plugin.serialization")`), dependency declarations (Ktor 3.4.1 for HTTP, Kotlinx Serialization 1.10.0 for JSON, Kotest 6.1.0 for testing), the JVM toolchain setting, and the JUnit Platform test configuration. This file will be modified to add the `maven-publish` plugin and publishing configuration.
- `settings.gradle.kts` — Declares the root project name (`openrouter-kotlin`) and the foojay toolchain resolver plugin (version 1.0.0), which allows Gradle to automatically download the correct JDK when the locally installed JDK does not match the toolchain version. This file will not be modified.
- `gradle.properties` — Contains a single property: `kotlin.code.style=official`. This file will not be modified.
- `gradle/wrapper/gradle-wrapper.properties` — Points at `gradle-9.2.0-bin.zip`. The Gradle wrapper scripts (`gradlew` and `gradlew.bat`) and the wrapper jar (`gradle/wrapper/gradle-wrapper.jar`) are committed to the repository and will be used by CI.
- `src/main/kotlin/dev/toliner/openrouter/` — Main source tree with 56 Kotlin files across packages `client`, `error`, `l1`, `l2`, `serialization`, and `streaming`.
- `src/test/kotlin/dev/toliner/openrouter/` — Test source tree with 38 Kotlin test files using Kotest's JUnit5 runner. Tests are executed via `./gradlew test`, which uses `useJUnitPlatform()`.
- `.github/` — Does not exist yet. Will be created.
- `jitpack.yml` — Does not exist yet. Will be created.

Key terms used in this plan:

- **GitHub Actions** — GitHub's built-in CI/CD service. Workflows are defined as YAML files in `.github/workflows/` and are triggered by repository events (push, pull request, tag creation, etc.).
- **JitPack** — A package repository service that builds Maven artifacts directly from GitHub repositories. When a user requests a dependency like `com.github.toliner:openrouter-kotlin:v1.0.0`, JitPack clones the repository at that tag, runs `./gradlew build publishToMavenLocal`, and serves the resulting artifacts.
- **`maven-publish` plugin** — A Gradle plugin that adds the ability to publish build artifacts to Maven repositories. It creates tasks like `publishToMavenLocal` which JitPack relies on.
- **Gradle Module Metadata** — A JSON file (`.module`) that Gradle generates alongside the traditional POM file. It carries richer dependency information than POM. Gradle consumers prefer `.module` over `.pom` when both are available.
- **SHA pinning** — Referencing a GitHub Action by its full 40-character commit hash instead of a mutable tag like `v4`. This prevents supply-chain attacks where an attacker could force-push a tag to point at malicious code.
- **SDKMAN** — A tool for managing multiple JDK installations. JitPack's build environment has SDKMAN pre-installed at `/home/jitpack/.sdkman/`. Commands like `sdk install java 25-open` download and install a specific JDK, and `sdk use java 25-open` activates it for the current shell session.
- **Foojay toolchain resolver** — The `org.gradle.toolchains.foojay-resolver-convention` plugin in `settings.gradle.kts`. When Gradle's `jvmToolchain(25)` requires JDK 25 but it is not locally available, this plugin automatically downloads it from the foojay (Friends of OpenJDK) discovery API.

## Plan of Work

The work is organized into five milestones that must be implemented in order. Each milestone produces a single atomic commit.

Milestone 1 modifies `build.gradle.kts` to add the `maven-publish` plugin, configure a Maven publication named `"maven"` that publishes the Java component with sources and javadoc JARs, includes POM metadata (project name, description, URL, license, developer, SCM), and disables Gradle Module Metadata generation to work around the JitPack bug. This is the foundation that all other milestones depend on.

Milestone 2 creates `jitpack.yml` at the repository root, which tells JitPack how to build this project. It uses SDKMAN to install and activate JDK 25, then runs `./gradlew build publishToMavenLocal`. This file is small but critical: without it, JitPack would attempt to build with JDK 8 and fail because the project requires JDK 25.

Milestone 3 creates `.github/workflows/ci.yml`, the continuous integration workflow. It triggers on every push to `main` and on every pull request. It checks out the code, sets up JDK 25 via `actions/setup-java` with the Temurin distribution, configures Gradle via `gradle/actions/setup-gradle` with branch-aware caching, runs `./gradlew build`, and uploads test reports as artifacts when tests fail. This workflow provides the safety net that catches build and test failures before they reach the main branch.

Milestone 4 creates `.github/workflows/release.yml`, the release workflow. It triggers when a Git tag matching `v*.*.*` is pushed. It checks out the code, sets up JDK 25, runs the full build, creates a GitHub Release with auto-generated release notes using `softprops/action-gh-release`, and fires an HTTP request to JitPack to pre-warm the artifact build.

Milestone 5 is a validation milestone with no code changes. It verifies the entire pipeline by running the build locally, inspecting the generated POM, and confirming that the CI workflow YAML is syntactically valid.

## Concrete Steps

### Milestone 1: Configure `maven-publish` in `build.gradle.kts`

Working directory: repository root (`/home/toliner/openrouter-kotlin`).

Replace the entire contents of `build.gradle.kts` with the following. The file is shown in full so there is no ambiguity about the final state:

```kotlin
plugins {
    kotlin("jvm") version "2.3.10"
    kotlin("plugin.serialization") version "2.3.10"
    `maven-publish`
}

group = "dev.toliner"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-client-core:3.4.1")
    implementation("io.ktor:ktor-client-cio:3.4.1")
    implementation("io.ktor:ktor-client-content-negotiation:3.4.1")
    implementation("io.ktor:ktor-serialization-kotlinx-json:3.4.1")
    implementation("io.ktor:ktor-sse:3.4.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.10.0")

    testImplementation("io.kotest:kotest-runner-junit5:6.1.0")
    testImplementation("io.kotest:kotest-assertions-core:6.1.0")
    testImplementation("io.kotest:kotest-property:6.1.0")
    testImplementation("io.ktor:ktor-client-mock:3.4.1")

    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(25)
}

java {
    withSourcesJar()
    withJavadocJar()
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<GenerateModuleMetadata> {
    enabled = false
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])

            pom {
                name.set("openrouter-kotlin")
                description.set("Kotlin client library for the OpenRouter API")
                url.set("https://github.com/toliner/openrouter-kotlin")

                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }

                developers {
                    developer {
                        id.set("toliner")
                        name.set("toliner")
                        url.set("https://github.com/toliner")
                    }
                }

                scm {
                    connection.set("scm:git:git://github.com/toliner/openrouter-kotlin.git")
                    developerConnection.set("scm:git:ssh://github.com/toliner/openrouter-kotlin.git")
                    url.set("https://github.com/toliner/openrouter-kotlin")
                }
            }
        }
    }
}
```

The changes from the original file are:

1. Added `maven-publish` to the `plugins` block (line 4).
2. Added the `java { withSourcesJar(); withJavadocJar() }` block after the `kotlin` block. `withSourcesJar()` creates a `-sources.jar` artifact containing the Kotlin source files. `withJavadocJar()` creates a `-javadoc.jar` artifact.
3. Added `tasks.withType<GenerateModuleMetadata> { enabled = false }` to disable Gradle Module Metadata generation, working around JitPack bug #5349.
4. Added the `publishing` block, which defines a publication named `"maven"` that publishes `components["java"]` (the main jar, sources jar, and javadoc jar). The `pom` block provides metadata that will appear in the generated POM file.

After making this change, verify it works by running:

```bash
./gradlew build publishToMavenLocal
```

Expected output (last few lines):

```
BUILD SUCCESSFUL in Xs
N actionable tasks: N executed
```

Then verify the published artifacts exist:

```bash
ls ~/.m2/repository/dev/toliner/openrouter-kotlin/1.0-SNAPSHOT/
```

Expected output should include files matching these patterns:

```
openrouter-kotlin-1.0-SNAPSHOT.jar
openrouter-kotlin-1.0-SNAPSHOT-sources.jar
openrouter-kotlin-1.0-SNAPSHOT-javadoc.jar
openrouter-kotlin-1.0-SNAPSHOT.pom
```

There must NOT be any `.module` file in this directory (confirming the `GenerateModuleMetadata` disable is working).

Verify the POM contains the metadata:

```bash
cat ~/.m2/repository/dev/toliner/openrouter-kotlin/1.0-SNAPSHOT/openrouter-kotlin-1.0-SNAPSHOT.pom
```

The POM should contain `<name>openrouter-kotlin</name>`, `<description>Kotlin client library for the OpenRouter API</description>`, `<url>https://github.com/toliner/openrouter-kotlin</url>`, an `<licenses>` block with Apache 2.0, a `<developers>` block with `toliner`, and an `<scm>` block with the repository URLs.

Commit message: `build: add maven-publish plugin with JitPack-compatible publishing configuration`

### Milestone 2: Create `jitpack.yml`

Create the file `jitpack.yml` at the repository root with the following content:

```yaml
jdk:
  - openjdk21
before_install:
  - sdk install java 25-open
  - sdk use java 25-open
install:
  - ./gradlew build publishToMavenLocal
```

Explanation of each line:

- `jdk: - openjdk21` — Sets a base JDK for the JitPack environment. This label is a fallback; the actual JDK we want is installed via SDKMAN below. We specify `openjdk21` rather than a higher version because JitPack's label-based JDK resolution is unreliable for newer versions.
- `before_install:` — Commands that run before the build. We use SDKMAN (pre-installed in JitPack's environment) to install JDK 25 from the OpenJDK project (`25-open`) and then activate it (`sdk use`). After these commands, `java -version` will report JDK 25.
- `install:` — The build command. JitPack's default is `./gradlew build publishToMavenLocal`, but specifying it explicitly makes the build process self-documenting and ensures it will not change if JitPack changes its defaults.

There is no local test for this file because JitPack's build environment is remote. Validation will happen when the first tag is pushed and JitPack attempts a build. However, we can verify the YAML is syntactically valid:

```bash
python3 -c "import yaml; yaml.safe_load(open('jitpack.yml'))" && echo "Valid YAML"
```

If `python3` with `pyyaml` is not available, any YAML linter will do; alternatively, visually inspect that the indentation uses exactly 2 spaces and the structure is a mapping with three keys.

Commit message: `ci: add jitpack.yml with JDK 25 via SDKMAN`

### Milestone 3: Create `.github/workflows/ci.yml`

Create the directory `.github/workflows/` and the file `.github/workflows/ci.yml` with the following content:

```yaml
name: CI

on:
  push:
    branches: [ main ]
  pull_request:

permissions:
  contents: read

jobs:
  build:
    name: Build & Test
    runs-on: ubuntu-latest

    steps:
      - name: Checkout
        uses: actions/checkout@de0fac2e4500dabe0009e67214ff5f5447ce83dd # v6.0.2

      - name: Set up JDK 25
        uses: actions/setup-java@be666c2fcd27ec809703dec50e508c2fdc7f6654 # v5.2.0
        with:
          distribution: temurin
          java-version: '25'

      - name: Set up Gradle
        uses: gradle/actions/setup-gradle@0723195856401067f7a2779048b490ace7a47d7c # v5.0.2
        with:
          cache-read-only: ${{ github.ref != 'refs/heads/main' }}

      - name: Build and test
        run: ./gradlew build

      - name: Upload test reports
        if: always()
        uses: actions/upload-artifact@ea165f8d65b6e75b540449e92b4886f43607fa02 # v4.6.2
        with:
          name: test-reports
          path: build/reports/tests/
          retention-days: 14
```

Explanation of each section:

The `on` block defines two triggers. `push: branches: [ main ]` runs the workflow on every push to the `main` branch. `pull_request:` (with no further filters) runs on every pull request targeting any branch. Together, these ensure that every code change is tested before and after merge.

The `permissions` block restricts the `GITHUB_TOKEN` to read-only access to repository contents. This is a security best practice: CI jobs only need to read code, not write to the repository.

The `steps` sequence:

1. **Checkout** clones the repository using `actions/checkout`. The SHA pin `de0fac2e4500dabe0009e67214ff5f5447ce83dd` corresponds to `v6.0.2`.

2. **Set up JDK 25** installs Eclipse Temurin JDK 25 using `actions/setup-java`. The SHA pin `be666c2fcd27ec809703dec50e508c2fdc7f6654` corresponds to `v5.2.0`. The `distribution: temurin` selects the Eclipse Temurin build, which is the community-standard OpenJDK distribution maintained by the Adoptium project. Note: we do NOT set `cache: gradle` here because `setup-gradle` handles caching (using both simultaneously causes conflicts).

3. **Set up Gradle** configures Gradle caching and wrapper validation using `gradle/actions/setup-gradle`. The `cache-read-only` expression evaluates to `true` for all branches except `main`. This means only `main` branch builds write to the cache, and pull request builds read from it. This prevents cache pollution from feature branches while still benefiting from cached dependencies.

4. **Build and test** runs `./gradlew build`, which compiles the Kotlin source, runs all tests via `useJUnitPlatform()` (which discovers and executes Kotest specs), and packages the jar. If any test fails, this step fails and the workflow reports a failure.

5. **Upload test reports** runs `if: always()`, meaning it executes even if the build step failed. It uploads the HTML test reports from `build/reports/tests/` as a downloadable artifact in the GitHub Actions UI, retained for 14 days. This allows developers to inspect detailed test results without needing to scroll through log output.

To validate the YAML locally before committing:

```bash
python3 -c "import yaml; yaml.safe_load(open('.github/workflows/ci.yml'))" && echo "Valid YAML"
```

Or use `actionlint` if available:

```bash
actionlint .github/workflows/ci.yml
```

Commit message: `ci: add GitHub Actions CI workflow for build and test`

### Milestone 4: Create `.github/workflows/release.yml`

Create the file `.github/workflows/release.yml` with the following content:

```yaml
name: Release

on:
  push:
    tags:
      - "v*.*.*"

permissions:
  contents: write

jobs:
  release:
    name: Release
    runs-on: ubuntu-latest

    steps:
      - name: Checkout
        uses: actions/checkout@de0fac2e4500dabe0009e67214ff5f5447ce83dd # v6.0.2

      - name: Set up JDK 25
        uses: actions/setup-java@be666c2fcd27ec809703dec50e508c2fdc7f6654 # v5.2.0
        with:
          distribution: temurin
          java-version: '25'

      - name: Set up Gradle
        uses: gradle/actions/setup-gradle@0723195856401067f7a2779048b490ace7a47d7c # v5.0.2

      - name: Build
        run: ./gradlew build

      - name: Create GitHub Release
        uses: softprops/action-gh-release@153bb8e04406b158c6c84fc1615b65b24149a1fe # v2.6.1
        with:
          generate_release_notes: true
          files: |
            build/libs/*.jar

      - name: Trigger JitPack build
        continue-on-error: true
        run: |
          echo "Triggering JitPack build for ${{ github.ref_name }}..."
          curl -sL "https://jitpack.io/com/github/toliner/openrouter-kotlin/${{ github.ref_name }}/build.log" > /dev/null || true
          echo "JitPack build triggered (may take several minutes to complete)."
```

Explanation of each section:

The `on: push: tags: - "v*.*.*"` trigger matches tags like `v1.0.0`, `v2.1.3`, etc. The workflow does NOT trigger on branch pushes or pull requests. A release is initiated by pushing a tag: `git tag v1.0.0 && git push origin v1.0.0`.

The `permissions: contents: write` is required because the `softprops/action-gh-release` action needs to create a Release object in the repository, which is a write operation on repository contents.

The steps:

1. **Checkout**, **Set up JDK 25**, **Set up Gradle** are identical to the CI workflow except that `cache-read-only` is not set on `setup-gradle`. Release builds are infrequent and run only on tags, so cache behavior is not critical. The default behavior (read and write cache) is fine.

2. **Build** runs the full build including tests. We do not skip tests on release builds because releasing a broken artifact is worse than a slightly slower release pipeline.

3. **Create GitHub Release** uses `softprops/action-gh-release` to create a GitHub Release from the tag. `generate_release_notes: true` tells GitHub to auto-generate release notes from merged pull requests and commit messages since the previous tag. The `files` pattern uploads all JAR files from `build/libs/` as release assets, which typically includes the main jar, sources jar, and javadoc jar.

4. **Trigger JitPack build** sends an HTTP GET request to JitPack's build log URL for this version. When JitPack receives this request and the version has not been built yet, it queues a build. The `continue-on-error: true` ensures the workflow succeeds even if JitPack is temporarily unavailable. The `|| true` at the end of the `curl` command is a belt-and-suspenders safeguard.

Commit message: `ci: add GitHub Actions release workflow with JitPack trigger`

### Milestone 5: End-to-End Validation

This milestone has no code changes. It verifies the complete pipeline.

**Step 1: Run the full build locally.**

```bash
./gradlew clean build publishToMavenLocal
```

Expected: `BUILD SUCCESSFUL`. All tests pass. Artifacts appear in `~/.m2/repository/dev/toliner/openrouter-kotlin/1.0-SNAPSHOT/`.

**Step 2: Verify no `.module` file is generated.**

```bash
find ~/.m2/repository/dev/toliner/openrouter-kotlin/1.0-SNAPSHOT/ -name "*.module" | wc -l
```

Expected output: `0`

**Step 3: Verify POM content.**

```bash
grep -c "<name>openrouter-kotlin</name>" ~/.m2/repository/dev/toliner/openrouter-kotlin/1.0-SNAPSHOT/*.pom
```

Expected output: `1` (the POM contains the project name).

**Step 4: Verify the three expected JARs exist.**

```bash
ls ~/.m2/repository/dev/toliner/openrouter-kotlin/1.0-SNAPSHOT/*.jar
```

Expected: Three jars — the main jar, the `-sources.jar`, and the `-javadoc.jar`.

**Step 5: Verify workflow YAML files are syntactically valid.**

```bash
python3 -c "
import yaml, sys
for f in ['.github/workflows/ci.yml', '.github/workflows/release.yml', 'jitpack.yml']:
    try:
        yaml.safe_load(open(f))
        print(f'{f}: OK')
    except Exception as e:
        print(f'{f}: FAIL - {e}')
        sys.exit(1)
"
```

Expected output:

```
.github/workflows/ci.yml: OK
.github/workflows/release.yml: OK
jitpack.yml: OK
```

**Step 6: Verify the Gradle wrapper is committed and executable.**

```bash
test -f gradle/wrapper/gradle-wrapper.jar && echo "Wrapper jar exists"
test -x gradlew && echo "gradlew is executable"
```

Expected: Both lines print their messages.

No commit is made for this milestone — it is purely validation.

## Validation and Acceptance

The acceptance criteria for this plan are:

1. Running `./gradlew build` in the repository root completes successfully with all tests passing. The test suite uses Kotest's JUnit5 runner, so the output will show test results in the standard JUnit format.

2. Running `./gradlew publishToMavenLocal` produces artifacts in `~/.m2/repository/dev/toliner/openrouter-kotlin/1.0-SNAPSHOT/` including the main jar, a `-sources.jar`, a `-javadoc.jar`, and a `.pom` file. No `.module` file is present.

3. The generated POM file at `~/.m2/repository/dev/toliner/openrouter-kotlin/1.0-SNAPSHOT/openrouter-kotlin-1.0-SNAPSHOT.pom` contains `<name>`, `<description>`, `<url>`, `<licenses>`, `<developers>`, and `<scm>` elements with the values specified in the `publishing` block.

4. The file `.github/workflows/ci.yml` exists and is valid YAML. When pushed to GitHub, it will trigger on pushes to `main` and on pull requests. (This can be verified after the first push by checking the Actions tab.)

5. The file `.github/workflows/release.yml` exists and is valid YAML. When a tag matching `v*.*.*` is pushed, it will trigger a release build. (This can be verified by pushing a tag like `v0.0.1-test` to a test branch.)

6. The file `jitpack.yml` exists at the repository root and is valid YAML.

7. After pushing a tag (e.g., `v1.0.0`) and waiting for the release workflow to complete, the GitHub Releases page shows a release with auto-generated notes and attached JAR files.

8. After the tag is pushed, visiting `https://jitpack.io/#toliner/openrouter-kotlin` shows the tag with a build status (green = success, in progress, or queued).

Criteria 1-6 can be verified locally before pushing. Criteria 7-8 require pushing to GitHub and can only be verified in the live environment.

## Idempotence and Recovery

All steps in this plan are idempotent. Running `./gradlew build publishToMavenLocal` multiple times overwrites the same SNAPSHOT artifacts in the local Maven repository without side effects. Creating the workflow YAML files is a file-write operation that produces the same result each time.

If Milestone 1 is applied but a subsequent milestone fails, the repository remains in a valid state: the build still works, tests still pass, and the new `maven-publish` configuration does not affect existing functionality. The `maven-publish` plugin adds new tasks (`publish*`) but does not modify existing ones (`build`, `test`, etc.).

If the CI workflow fails on its first run (e.g., due to JDK availability issues), it can be re-run from the GitHub Actions UI without any repository changes. The workflow is stateless — it does not modify the repository.

If the release workflow fails partway through (e.g., the build succeeds but GitHub Release creation fails), it can be re-run. The `softprops/action-gh-release` action is idempotent: if a release for the same tag already exists, it updates it rather than creating a duplicate.

If the JitPack trigger step fails (which is expected to happen occasionally due to network issues or JitPack downtime), it does not affect the release. JitPack will build the artifact on first consumer request regardless.

To fully reset and retry from scratch: delete any created files (`.github/`, `jitpack.yml`), restore `build.gradle.kts` from Git (`git checkout build.gradle.kts`), and start from Milestone 1.

## Artifacts and Notes

### Commit Strategy

The implementation produces exactly four commits, one per code-changing milestone:

```
build: add maven-publish plugin with JitPack-compatible publishing configuration
ci: add jitpack.yml with JDK 25 via SDKMAN
ci: add GitHub Actions CI workflow for build and test
ci: add GitHub Actions release workflow with JitPack trigger
```

Each commit should pass `./gradlew build` independently. The first commit adds publishing infrastructure. The second adds JitPack configuration. The third and fourth add CI/CD workflows.

### SHA Pin Reference Table

All GitHub Actions are pinned to full commit SHAs for supply-chain security. The table below maps each SHA to the human-readable version for future auditing and updates:

| Action | Version | SHA |
|--------|---------|-----|
| `actions/checkout` | v6.0.2 | `de0fac2e4500dabe0009e67214ff5f5447ce83dd` |
| `actions/setup-java` | v5.2.0 | `be666c2fcd27ec809703dec50e508c2fdc7f6654` |
| `gradle/actions/setup-gradle` | v5.0.2 | `0723195856401067f7a2779048b490ace7a47d7c` |
| `softprops/action-gh-release` | v2.6.1 | `153bb8e04406b158c6c84fc1615b65b24149a1fe` |
| `actions/upload-artifact` | v4.6.2 | `ea165f8d65b6e75b540449e92b4886f43607fa02` |

To update an action: check the action's releases page on GitHub, find the new tag's commit SHA, and replace the SHA in the workflow file. Always add a comment with the version tag after the SHA.

### How Consumers Will Use This Library

After a tag like `v1.0.0` is pushed and JitPack builds it, Gradle users can depend on this library by adding the following to their build files:

In `settings.gradle.kts` or `build.gradle.kts` (repositories):

```kotlin
repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}
```

In `build.gradle.kts` (dependencies):

```kotlin
dependencies {
    implementation("com.github.toliner:openrouter-kotlin:v1.0.0")
}
```

The group ID used by JitPack is always `com.github.{username}`, the artifact ID is the repository name, and the version is the Git tag.

### Release Process for Maintainers

To create a release:

1. Update the `version` in `build.gradle.kts` from `"1.0-SNAPSHOT"` to `"1.0.0"` (or the desired version).
2. Commit the version change: `git commit -am "release: v1.0.0"`
3. Create and push a tag: `git tag v1.0.0 && git push origin main v1.0.0`
4. The release workflow will automatically create a GitHub Release with generated notes and attached JARs, then trigger JitPack to build the artifact.
5. Monitor the build at `https://jitpack.io/#toliner/openrouter-kotlin/v1.0.0`.

## Interfaces and Dependencies

This plan does not introduce new Kotlin interfaces or change existing API surfaces. It only adds build and CI infrastructure.

The external dependencies introduced are:

- **`maven-publish` plugin** — A built-in Gradle plugin (no version to specify; it ships with Gradle itself). It adds `publishToMavenLocal` and `publish` tasks to the project.
- **GitHub Actions** — Five actions are used, all pinned by SHA as documented in the Artifacts section above. No secrets need to be configured; all actions use the default `GITHUB_TOKEN` provided by GitHub Actions.
- **JitPack** — No configuration is needed on the JitPack side. JitPack will discover the repository automatically when a consumer requests it or when the release workflow hits the build URL. The `jitpack.yml` file provides build instructions.
- **SDKMAN in JitPack** — Pre-installed in JitPack's environment at `/home/jitpack/.sdkman/`. The `sdk` command is available in `before_install` without any setup.

The key Gradle task graph changes after Milestone 1:

- `./gradlew build` — Unchanged behavior. Compiles, tests, and packages.
- `./gradlew publishToMavenLocal` — NEW. Publishes the main jar, sources jar, javadoc jar, and POM to `~/.m2/repository/dev/toliner/openrouter-kotlin/{version}/`.
- `./gradlew publishMavenPublicationToMavenLocal` — NEW (auto-generated by `maven-publish`). Same as above but explicit about which publication.
- `./gradlew generatePomFileForMavenPublication` — NEW (auto-generated). Generates only the POM file without publishing.
- The `GenerateModuleMetadata` task exists but is disabled (it will report as `SKIPPED` in Gradle output).
