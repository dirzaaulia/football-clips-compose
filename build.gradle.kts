import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.jetbrainsCompose) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinMultiplatformLibrary) apply false
    alias(libs.plugins.kotlinSerialization) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.firebase.crashlytics) apply false
    alias(libs.plugins.triplet.play) apply false
}

subprojects {
    tasks.withType<KotlinCompilationTask<*>>().configureEach {
        compilerOptions.freeCompilerArgs.add("-Xexpect-actual-classes")
    }
}

abstract class CheckLineBudgetTask : DefaultTask() {
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val kotlinFiles: ConfigurableFileCollection

    @TaskAction
    fun check() {
        val violations = mutableListOf<String>()
        kotlinFiles.files.forEach { file ->
            val lineCount = file.readLines().size
            if (lineCount > 250) {
                violations.add("${file.name}: $lineCount lines (> 250)")
            }
        }
        if (violations.isNotEmpty()) {
            throw GradleException("Line budget violations (> 250 lines):\n" + violations.joinToString("\n"))
        } else {
            println("All Kotlin files respect the 250-line budget!")
        }
    }
}

tasks.register<CheckLineBudgetTask>("checkLineBudget") {
    group = "verification"
    description = "Checks that all Kotlin source files stay within the 250-line budget."
    kotlinFiles.from(
        fileTree(rootDir) {
            include("**/*.kt")
            exclude("**/build/**", "**/.gradle/**")
        },
    )
}


