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
            val maxAllowed = if (file.name.endsWith("ViewModel.kt")) 150 else 250
            if (lineCount > maxAllowed) {
                violations.add("${file.name}: $lineCount lines (> $maxAllowed)")
            }
        }
        if (violations.isNotEmpty()) {
            throw GradleException("Line budget violations:\n" + violations.joinToString("\n"))
        } else {
            println("All Kotlin files and ViewModels respect line budgets!")
        }
    }
}

tasks.register<CheckLineBudgetTask>("checkLineBudget") {
    group = "verification"
    description = "Checks that all Kotlin source files stay within 250 lines (and ViewModels within 150 lines)."
    kotlinFiles.from(
        fileTree(rootDir) {
            include("**/*.kt")
            exclude("**/build/**", "**/.gradle/**")
        },
    )
}


