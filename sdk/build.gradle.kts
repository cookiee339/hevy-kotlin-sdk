plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.dokka)
    alias(libs.plugins.detekt)
    alias(libs.plugins.ktlint)
    `maven-publish`
}

kotlin {
    jvm {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
        }
    }

    js(IR) {
        browser()
        nodejs()
    }

    iosArm64()
    iosSimulatorArm64()
    iosX64()
    macosArm64()
    macosX64()

    sourceSets {
        commonMain.dependencies {
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.coroutines.core)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.ktor.client.mock)
            implementation(libs.kotlinx.coroutines.test)
        }

        jvmMain.dependencies {
            implementation(libs.ktor.client.okhttp)
        }

        jsMain.dependencies {
            implementation(libs.ktor.client.js)
        }

        appleMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
    }
}

detekt {
    buildUponDefaultConfig = true
    allRules = false
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/cookiee339/hevy-kotlin-sdk")
            credentials {
                username = System.getenv("GITHUB_ACTOR") ?: ""
                password = System.getenv("GITHUB_TOKEN") ?: ""
            }
        }
    }
    publications.withType<MavenPublication> {
        pom {
            name.set("Hevy Kotlin SDK")
            description.set("Kotlin Multiplatform SDK for the Hevy fitness app API")
            url.set("https://github.com/cookiee339/hevy-kotlin-sdk")
            licenses {
                license {
                    name.set("MIT License")
                    url.set("https://opensource.org/licenses/MIT")
                }
            }
            scm {
                connection.set("scm:git:git://github.com/cookiee339/hevy-kotlin-sdk.git")
                developerConnection.set("scm:git:ssh://github.com/cookiee339/hevy-kotlin-sdk.git")
                url.set("https://github.com/cookiee339/hevy-kotlin-sdk")
            }
            developers {
                developer {
                    id.set("hevy-sdk")
                    name.set("Hevy SDK Team")
                }
            }
        }
    }
}
