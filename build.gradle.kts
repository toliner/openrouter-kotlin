import org.jetbrains.dokka.gradle.engine.parameters.VisibilityModifier

plugins {
    kotlin("jvm") version "2.3.10"
    kotlin("plugin.serialization") version "2.3.10"
    `maven-publish`
    id("org.jetbrains.dokka") version "2.1.0"
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
    explicitApi()
}

java {
    withSourcesJar()
}

dokka {
    moduleName.set("openrouter-kotlin")
    moduleVersion.set(project.version.toString())

    dokkaPublications.html {
        outputDirectory.set(layout.buildDirectory.dir("dokka/html"))
        suppressObviousFunctions.set(true)
        suppressInheritedMembers.set(false)
        includes.from("MODULE.md")
    }

    dokkaSourceSets.main {
        documentedVisibilities(VisibilityModifier.Public)

        sourceLink {
            localDirectory.set(file("src/main/kotlin"))
            remoteUrl("https://github.com/toliner/openrouter-kotlin/tree/master/src/main/kotlin")
            remoteLineSuffix.set("#L")
        }

        externalDocumentationLinks.register("kotlinx-serialization") {
            url("https://kotlinlang.org/api/kotlinx.serialization/")
        }

        externalDocumentationLinks.register("ktor") {
            url("https://api.ktor.io/")
        }

        jdkVersion.set(17)
    }

    pluginsConfiguration.html {
        footerMessage.set("© openrouter-kotlin contributors. Apache 2.0 License.")
    }
}

val dokkaHtmlJar by tasks.registering(Jar::class) {
    description = "Dokka HTML documentation JAR"
    from(tasks.named("dokkaGeneratePublicationHtml").map { it.outputs })
    archiveClassifier.set("javadoc")
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
            artifact(dokkaHtmlJar)

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
