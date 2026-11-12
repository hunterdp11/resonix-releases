-keep public class com.callscreener.app.CallScreenerApp { *; }
-keep public class com.callscreener.app.ui.MainActivity { *; }
-keep public class com.callscreener.app.ui.ScreeningCallActivity { *; }
-keep public class * extends android.app.Service { *; }
-keep public class * extends android.content.BroadcastReceiver { *; }
-keep public class * extends android.content.ContentProvider { *; }

-keep class com.callscreener.app.service.** { *; }

-keep class com.callscreener.app.model.** { *; }
-keepclassmembers class com.callscreener.app.model.** { *; }
-keep class com.callscreener.app.data.** { *; }
-keepclassmembers class com.callscreener.app.data.** { *; }
-keep class com.callscreener.app.ota.** { *; }
-keepclassmembers class com.callscreener.app.ota.** { *; }

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

-keepclassmembers class * {
    @androidx.compose.runtime.Composable *;
}
-keep class androidx.compose.runtime.** { *; }

-keep class dev.chrisbanes.haze.** { *; }

-keepclassmembers class kotlinx.coroutines.** { *; }

-keepattributes SourceFile,LineNumberTable
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod
