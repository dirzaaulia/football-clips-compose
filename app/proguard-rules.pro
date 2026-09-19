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
-keep class android.webkit.** { *; }
-keep class * extends android.webkit.WebViewClient { *; }
-keep class * extends android.webkit.WebChromeClient { *; }
-keepclassmembers class * extends android.webkit.WebViewClient {
    public <methods>;
}
-keepclassmembers class * extends android.webkit.WebChromeClient {
    public <methods>;
}
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
-keep class com.dirzaaulia.footballclips.ui.player.** { *; }
-keepclassmembers class com.dirzaaulia.footballclips.ui.player.** { *; }

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
