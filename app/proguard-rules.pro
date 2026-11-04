# Proguard rules for CallScreener
-keep class com.callscreener.app.service.** { *; }
-keepclassmembers class * {
    @androidx.compose.runtime.Composable *;
}

