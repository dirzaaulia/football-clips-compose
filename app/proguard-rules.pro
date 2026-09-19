# R8 / ProGuard rules for Football Highlights & Clips

# --- Keep Annotations, Signatures & Reflection Attributes ---
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod,JavascriptInterface

# --- Kotlin Serialization ---
-keepclassmembers class * {
    @kotlinx.serialization.SerialName <fields>;
}
-keepnames class * {
    @kotlinx.serialization.Serializable *;
}
-keepclassmembers class * {
    @kotlinx.serialization.Serializable *;
}
-keepclassmembers class *$$serializer {
    *** INSTANCE;
}
-keepclassmembers class * {
    *** Companion;
}

# --- Supabase & Ktor & Kotlinx DateTime ---
-keep class io.github.jan.supabase.** { *; }
-keep class io.ktor.** { *; }
-keep class kotlinx.datetime.** { *; }

# --- Android WebView, YouTube Player & JavaScript Interop ---
# Keep all WebViewClient/WebChromeClient subclasses - including anonymous inner
# classes generated inside Compose 'factory' lambdas which R8 strips aggressively.
-keep class * extends android.webkit.WebViewClient { *; }
-keep class * extends android.webkit.WebChromeClient { *; }
-keepclassmembers class * extends android.webkit.WebViewClient {
    public *;
    protected *;
}
-keepclassmembers class * extends android.webkit.WebChromeClient {
    public *;
    protected *;
}

# Keep WebSettings property setters - R8 may inline/remove these in release
-keepclassmembers class android.webkit.WebSettings {
    public *;
}

# Keep JavascriptInterface-annotated methods
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# Keep the entire player package including generated Compose lambda classes
-keep class com.dirzaaulia.footballclips.ui.player.** { *; }
-keepclassmembers class com.dirzaaulia.footballclips.ui.player.** { *; }
# Keep any anonymous/synthetic classes generated inside the player package
-keep class com.dirzaaulia.footballclips.ui.player.**$* { *; }

# --- Suppress Warnings for Desktop/JVM Classes ---
-dontwarn java.lang.management.**
-dontwarn org.codehaus.mojo.animal_sniffer.**
-dontwarn okhttp3.internal.h2.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**

# --- Data Models & DTOs ---
-keep class com.dirzaaulia.footballclips.data.model.** { *; }
-keep class com.dirzaaulia.footballclips.domain.model.** { *; }

# --- RevenueCat Billing SDK ---
-keep class com.revenuecat.purchases.** { *; }

# --- Google Mobile Ads (AdMob) ---
-keep class com.google.android.gms.ads.** { *; }
-keep class com.google.ads.** { *; }

# --- Firebase & Analytics / Crashlytics ---
-keep class com.google.firebase.** { *; }
