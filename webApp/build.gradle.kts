import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    alias(libs.plugins.spotless)
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser {
            val projectDirPath = project.projectDir.path
            commonWebpackConfig {
                outputFileName = "composeApp.js"
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                    static(projectDirPath)
                }
                sourceMaps = false
            }
        }
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.compose.components.resources)
            implementation(libs.kotlinx.collections.immutable)
        }
        wasmJsMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.components.resources)
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor)
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.material.icons.extended)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime.compose)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.js)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.datetime)
        }
    }
}

compose.resources {
    packageOfResClass = "com.dirzaaulia.footballclips.monitor.resources"
    generateResClass = always
}

spotless {
    kotlin {
        target("src/wasmJsMain/kotlin/com/dirzaaulia/footballclips/monitor/**/*.kt",
            "src/commonMain/kotlin/com/dirzaaulia/footballclips/monitor/**/*.kt")
        ktlint("1.8.0").editorConfigOverride(mapOf(
            "max_line_length" to "120",
            "ktlint_function_naming_ignore_when_annotated_with" to "Composable",
            "ktlint_standard_trailing-comma-on-call-site" to "enabled",
            "ktlint_standard_trailing-comma-on-declaration-site" to "enabled",
        ))
        trimTrailingWhitespace()
        endWithNewline()
    }
}

val monitorDetekt = configurations.create("monitorDetekt")

dependencies { monitorDetekt(libs.detekt.cli) }

tasks.register<JavaExec>("detekt") {
    group = "verification"
    classpath = monitorDetekt
    mainClass.set("io.gitlab.arturbosch.detekt.cli.Main")
    args("--input", "src/wasmJsMain/kotlin/com/dirzaaulia/footballclips/monitor,src/commonMain/kotlin/com/dirzaaulia/footballclips/monitor",
        "--config", "config/monitor-detekt.yml", "--build-upon-default-config")
}
