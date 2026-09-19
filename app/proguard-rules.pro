# R8 / ProGuard rules for Football Highlights & Clips

# --- Keep Annotations, Signatures & Reflection Attributes ---
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod,JavascriptInterface

# --- AndroidX WorkManager & Room Database ---
-keep class * extends androidx.room.RoomDatabase { *; }
-keep class androidx.work.impl.** { *; }
-keepclassmembers class * extends androidx.room.RoomDatabase {
    public <init>();
}

# --- Android WebView, YouTube Player & JavaScript Interop ---
# Keep all WebViewClient/WebChromeClient subclasses and properties
-keep class android.webkit.** { *; }
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
-keepclassmembers class android.webkit.WebSettings {
    public *;
}
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# Keep the entire player package including generated Compose lambda classes
-keep class com.dirzaaulia.footballclips.ui.player.** { *; }
-keepclassmembers class com.dirzaaulia.footballclips.ui.player.** { *; }
-keep class com.dirzaaulia.footballclips.ui.player.**$* { *; }

# --- Data Models & DTOs ---
-keep class com.dirzaaulia.footballclips.data.model.** { *; }
-keep class com.dirzaaulia.footballclips.domain.model.** { *; }

# --- Suppress Warnings for Desktop/JVM Classes ---
-dontwarn java.lang.management.**
-dontwarn org.codehaus.mojo.animal_sniffer.**
-dontwarn okhttp3.internal.h2.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
