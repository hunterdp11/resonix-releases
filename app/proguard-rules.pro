# ====================================================================
# Resonix Proguard & R8 Optimization Rules
# Ensures maximum size reduction, dead code stripping, and rock-solid stability
# ====================================================================

# 1. Keep all Android Manifest entry points
-keep public class com.callscreener.app.CallScreenerApp { *; }
-keep public class com.callscreener.app.ui.MainActivity { *; }
-keep public class com.callscreener.app.ui.ScreeningCallActivity { *; }
-keep public class * extends android.app.Service { *; }
-keep public class * extends android.content.BroadcastReceiver { *; }
-keep public class * extends android.content.ContentProvider { *; }

# 2. Telecom & CallScreening Framework Services
-keep class com.callscreener.app.service.** { *; }

# 3. Data Models (JSON cache, SharedPreferences persistence)
-keep class com.callscreener.app.model.** { *; }
-keepclassmembers class com.callscreener.app.model.** { *; }
-keep class com.callscreener.app.data.** { *; }
-keepclassmembers class com.callscreener.app.data.** { *; }
-keep class com.callscreener.app.ota.** { *; }
-keepclassmembers class com.callscreener.app.ota.** { *; }

# 4. Hardware Audio HAL, ALSA, Root Privilege, and Speech Jammer Engines
-keep class com.callscreener.app.engine.** { *; }
-keepclassmembers class com.callscreener.app.engine.** { *; }
-keep class com.callscreener.app.root.** { *; }
-keepclassmembers class com.callscreener.app.root.** { *; }
-keep class com.callscreener.app.session.** { *; }
-keepclassmembers class com.callscreener.app.session.** { *; }
-keep class com.callscreener.app.telephony.** { *; }
-keepclassmembers class com.callscreener.app.telephony.** { *; }
-keep class com.callscreener.app.ui.overlay.** { *; }
-keepclassmembers class com.callscreener.app.ui.overlay.** { *; }

# 5. Jetpack Compose Runtime & State
-keepclassmembers class * {
    @androidx.compose.runtime.Composable *;
}
-keep class androidx.compose.runtime.** { *; }

# 6. Real-Time Frosted Glass Haze Blur
-keep class dev.chrisbanes.haze.** { *; }

# 7. Kotlin Coroutines & Flow
-keepclassmembers class kotlinx.coroutines.** { *; }

# 8. Preserve Line Numbers & Attributes for debugging if needed
-keepattributes SourceFile,LineNumberTable
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod


