import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.triplet.play)
}

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use { stream ->
            load(stream)
        }
    }
}

fun getSecret(key: String, defaultValue: String = ""): String {
    return localProperties.getProperty(key)
        ?: providers.gradleProperty(key).orNull
        ?: providers.environmentVariable(key).orNull
        ?: defaultValue
}

val versionPropsFile = rootProject.file("version.properties")
val versionProps = Properties().apply {
    if (!versionPropsFile.exists()) {
        versionPropsFile.writeText("VERSION_MAJOR=3\nVERSION_MINOR=0\nVERSION_PATCH=0\nVERSION_BUILD=1\n")
    }
    versionPropsFile.inputStream().use { load(it) }
}

val major = versionProps.getProperty("VERSION_MAJOR", "3").toInt()
val minor = versionProps.getProperty("VERSION_MINOR", "0").toInt()
val patch = versionProps.getProperty("VERSION_PATCH", "0").toInt()
val buildNumber = (System.getenv("GITHUB_RUN_NUMBER")?.toIntOrNull()
    ?: versionProps.getProperty("VERSION_BUILD", "1").toInt())

val targetTrack = providers.gradleProperty("track").getOrElse("internal")

play {
    val playKeyPath = localProperties.getProperty("PLAY_SERVICE_ACCOUNT")
        ?: System.getenv("PLAY_SERVICE_ACCOUNT")
        ?: "play-console-key.json"
    val playKeyFile = file(playKeyPath)
    if (playKeyFile.exists()) {
        serviceAccountCredentials.set(playKeyFile)
    }
    defaultToAppBundles.set(true)
    track.set(targetTrack)
}

tasks.register("incrementBuildNumber") {
    description = "Increments the build number in version.properties"
    group = "versioning"
    doLast {
        val current = versionProps.getProperty("VERSION_BUILD", "1").toInt()
        versionProps.setProperty("VERSION_BUILD", (current + 1).toString())
        versionPropsFile.outputStream().use { versionProps.store(it, "Auto-incremented by build runner") }
    }
}

tasks.matching { it.name.startsWith("publish") && it.name.contains("Bundle") }.configureEach {
    dependsOn("incrementBuildNumber")
}

android {
    namespace = "com.dirzaaulia.footballclips"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        applicationId = "com.dirzaaulia.footballclips"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()

        versionCode = buildNumber
        versionName = when (targetTrack.lowercase()) {
            "production", "prod" -> "$major.$minor.$patch"
            "beta" -> "$major.$minor.$patch-beta.$buildNumber"
            "alpha" -> "$major.$minor.$patch-alpha.$buildNumber"
            else -> "$major.$minor.$patch-internal.$buildNumber"
        }

        manifestPlaceholders["admobAppId"] = getSecret("ADMOB_APP_ID")

        buildConfigField("String", "REVENUECAT_API_KEY", "\"${getSecret("REVENUECAT_API_KEY")}\"")
        buildConfigField("String", "SUPABASE_URL", "\"${getSecret("SUPABASE_URL")}\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"${getSecret("SUPABASE_ANON_KEY")}\"")
        buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", "\"${getSecret("GOOGLE_WEB_CLIENT_ID")}\"")
        buildConfigField("String", "ADMOB_APP_ID", "\"${getSecret("ADMOB_APP_ID")}\"")
        buildConfigField("String", "ADMOB_BANNER_ID", "\"${getSecret("ADMOB_BANNER_ID")}\"")
        buildConfigField("String", "ADMOB_NATIVE_ID", "\"${getSecret("ADMOB_NATIVE_ID", "ca-app-pub-6717632447198427/5222531302")}\"")
        buildConfigField("String", "ADMOB_INTERSTITIAL_ID", "\"${getSecret("ADMOB_INTERSTITIAL_ID")}\"")
        androidResources {
            localeFilters += setOf("en", "id", "es", "pt")
        }
    }

    packaging {
        resources {
            excludes += setOf(
                "META-INF/{AL2.0,LGPL2.1}",
                "META-INF/DEPENDENCIES",
                "META-INF/LICENSE*",
                "META-INF/NOTICE*",
                "META-INF/*.kotlin_module",
                "META-INF/**/*.kotlin_module",
                "META-INF/*:*",
                "META-INF/**/*:*",
                "META-INF/io.github.jan-tennert.supabase:supabase-kt.kotlin_module",
                "**/attach_hotspot_windows.dll",
            )
        }
    }

    signingConfigs {
        create("release") {
            val keystorePath = localProperties.getProperty("KEYSTORE_FILE")
                ?: providers.gradleProperty("KEYSTORE_FILE").orNull 
                ?: System.getenv("KEYSTORE_FILE") 
                ?: ""
            val keystoreFile = file(keystorePath)
            val storePass = localProperties.getProperty("KEYSTORE_PASSWORD")
                ?: providers.gradleProperty("KEYSTORE_PASSWORD").orNull 
                ?: System.getenv("KEYSTORE_PASSWORD") 
                ?: ""
            val alias = localProperties.getProperty("KEY_ALIAS")
                ?: providers.gradleProperty("KEY_ALIAS").orNull 
                ?: System.getenv("KEY_ALIAS") 
                ?: ""
            val keyPass = localProperties.getProperty("KEY_PASSWORD")
                ?: providers.gradleProperty("KEY_PASSWORD").orNull 
                ?: System.getenv("KEY_PASSWORD") 
                ?: ""

            if (keystoreFile.exists() && storePass.isNotEmpty()) {
                storeFile = keystoreFile
                storePassword = storePass
                keyAlias = alias
                keyPassword = keyPass
            } else {
                initWith(getByName("debug"))
            }
        }
    }

    buildTypes {
        getByName("debug") {
            signingConfig = signingConfigs.getByName("release")
        }
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "src/main/keepRules/rules.keep",
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
    }
}

dependencies {
    implementation(project(":composeApp"))

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.admob.nextgen)
    implementation(libs.coil.core)
    implementation(libs.coil.svg)
    implementation(libs.koin.core)
    implementation(libs.koin.android)
    implementation(libs.revenuecat.sdk)
    implementation(libs.supabase.auth)
    implementation(project.dependencies.platform(libs.firebase.bom))
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.analytics)

    debugImplementation(libs.chucker.library)
    releaseImplementation(libs.chucker.library.no.op)
}
