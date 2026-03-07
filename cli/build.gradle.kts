plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.graalvm.native)
    application
}

application {
    mainClass.set("com.hevy.cli.MainKt")
}

dependencies {
    implementation(project(":sdk"))
    implementation(libs.clikt)
    implementation(libs.ktor.client.cio)
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(kotlin("test"))
    testImplementation(libs.ktor.client.mock)
    testImplementation(libs.kotlinx.coroutines.test)
}

kotlin {
    jvmToolchain(21)
}

graalvmNative {
    binaries {
        named("main") {
            mainClass.set("com.hevy.cli.MainKt")
            imageName.set("hevy")
            buildArgs.addAll(
                "--no-fallback",
                "-H:+ReportExceptionStackTraces",
            )
        }
    }
    metadataRepository {
        enabled.set(true)
    }
}

tasks.test {
    useJUnitPlatform()
}
