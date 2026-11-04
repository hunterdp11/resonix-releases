package com.callscreener.app.root

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

enum class AppOperationMode {
    STANDARD,      // Non-rooted OEM dialer & standard telephony
    ROOT_ENHANCED  // Root-powered ALSA mixer, direct audio HAL injection, and dual-side recording
}

object RootPrivilegeManager {

    private const val TAG = "RootPrivilegeManager"
    private const val PREFS_NAME = "call_screener_mode_prefs"
    private const val KEY_OPERATION_MODE = "app_operation_mode"

    fun getOperationMode(context: Context): AppOperationMode {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val saved = prefs.getString(KEY_OPERATION_MODE, null)
        if (saved != null) {
            return try {
                AppOperationMode.valueOf(saved)
            } catch (_: Exception) {
                AppOperationMode.STANDARD
            }
        }
        return if (isRootAvailable() || isPrivilegedApp(context)) {
            AppOperationMode.ROOT_ENHANCED
        } else {
            AppOperationMode.STANDARD
        }
    }

    fun setOperationMode(context: Context, mode: AppOperationMode) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_OPERATION_MODE, mode.name).apply()
        Log.i(TAG, "Operation mode switched to: $mode")
    }

    fun isRootModeActive(context: Context): Boolean {
        return getOperationMode(context) == AppOperationMode.ROOT_ENHANCED && isRootAvailable()
    }

    fun isRootAvailable(): Boolean {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", "id"))
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val output = reader.readLine()
            process.waitFor()
            output != null && output.contains("uid=0")
        } catch (e: Exception) {
            false
        }
    }

    fun isPrivilegedApp(context: Context): Boolean {
        return try {
            val appInfo = context.packageManager.getApplicationInfo(context.packageName, 0)
            val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            val isUpdatedSystem = (appInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
            val inPrivApp = appInfo.sourceDir.contains("/priv-app/")
            val hasCaptureAudio = context.checkSelfPermission("android.permission.CAPTURE_AUDIO_OUTPUT") == PackageManager.PERMISSION_GRANTED
            val hasModifyPhone = context.checkSelfPermission("android.permission.MODIFY_PHONE_STATE") == PackageManager.PERMISSION_GRANTED

            isSystem || isUpdatedSystem || inPrivApp || hasCaptureAudio || hasModifyPhone
        } catch (e: Exception) {
            false
        }
    }

    fun isMagiskModuleInstalled(): Boolean {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", "test -f /data/adb/modules/call_screener/module.prop && echo 'yes' || echo 'no'"))
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val line = reader.readLine()
            process.waitFor()
            line != null && line.trim() == "yes"
        } catch (e: Exception) {
            false
        }
    }

    fun executeAsync(block: suspend () -> Unit) {
        kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
            try {
                block()
            } catch (e: Exception) {
                Log.w(TAG, "Async root task failed", e)
            }
        }
    }

    suspend fun executeSuCommand(command: String): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        try {
            val process = Runtime.getRuntime().exec("su")
            process.outputStream.bufferedWriter().use { writer ->
                writer.write(command)
                writer.newLine()
                writer.write("exit\n")
                writer.flush()
            }
            val output = process.inputStream.bufferedReader().readText()
            val error = process.errorStream.bufferedReader().readText()
            val exitCode = process.waitFor()
            if (exitCode == 0) {
                Pair(true, output.ifBlank { "Success" })
            } else {
                Pair(false, error.ifBlank { "Exit code $exitCode" })
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error executing root command: $command", e)
            Pair(false, e.localizedMessage ?: "Unknown error")
        }
    }

    /**
     * Creates a seamless Magisk / KernelSU / APatch module that mounts Call Screener into
     * /system/priv-app with full system audio routing and capture permissions!
     */
    suspend fun installSystemModule(context: Context): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        val apkPath = context.packageCodePath
        val packageName = context.packageName

        val script = """
            MODULE_DIR="/data/adb/modules/call_screener"
            mkdir -p "${'$'}MODULE_DIR/system/priv-app/CallScreener"
            mkdir -p "${'$'}MODULE_DIR/system/etc/permissions"
            
            # 1. Copy active APK into system priv-app
            cp "$apkPath" "${'$'}MODULE_DIR/system/priv-app/CallScreener/CallScreener.apk"
            chmod 755 "${'$'}MODULE_DIR"
            chmod 755 "${'$'}MODULE_DIR/system"
            chmod 755 "${'$'}MODULE_DIR/system/priv-app"
            chmod 755 "${'$'}MODULE_DIR/system/priv-app/CallScreener"
            chmod 644 "${'$'}MODULE_DIR/system/priv-app/CallScreener/CallScreener.apk"
            
            # 2. Write privapp-permissions whitelist for Android system_server
            cat << 'EOF' > "${'$'}MODULE_DIR/system/etc/permissions/privapp-permissions-$packageName.xml"
<?xml version="1.0" encoding="utf-8"?>
<permissions>
    <privapp-permissions package="$packageName">
        <permission name="android.permission.CAPTURE_AUDIO_OUTPUT"/>
        <permission name="android.permission.MODIFY_PHONE_STATE"/>
        <permission name="android.permission.CONTROL_INCALL_EXPERIENCE"/>
        <permission name="android.permission.MODIFY_AUDIO_ROUTING"/>
    </privapp-permissions>
</permissions>
EOF
            chmod 755 "${'$'}MODULE_DIR/system/etc"
            chmod 755 "${'$'}MODULE_DIR/system/etc/permissions"
            chmod 644 "${'$'}MODULE_DIR/system/etc/permissions/privapp-permissions-$packageName.xml"
            
            # 3. Create standard Magisk/KernelSU module metadata
            cat << 'EOF' > "${'$'}MODULE_DIR/module.prop"
id=call_screener
name=Call Screener AI Priv-App
version=1.0.1
versionCode=2
author=CallScreener
description=Systemless mount of Call Screener AI into /system/priv-app with direct audio routing privileges.
EOF
            chmod 644 "${'$'}MODULE_DIR/module.prop"
            rm -f "${'$'}MODULE_DIR/disable"
            
            # 4. Set SELinux Contexts so system_server can read the priv-app without permission denial
            chcon -R u:object_r:system_file:s0 "${'$'}MODULE_DIR/system"
            
            # 5. Clear all user-space package copies (Android 8 through Android 14) so PackageManager binds to /system/priv-app on reboot
            rm -rf /data/app/*$packageName*
            rm -rf /data/app/~~*/*$packageName*
            find /data/app -type d -name "*$packageName*" -exec rm -rf {} + 2>/dev/null || true
            pm uninstall -k $packageName 2>/dev/null || true
            
            echo "Installed Magisk/KernelSU module successfully."
        """.trimIndent()

        val result = executeSuCommand(script)
        if (result.first) {
            Pair(true, "Module installed to Magisk/KernelSU! Please reboot your phone now to activate.")
        } else {
            Pair(false, "Failed to create module: ${result.second}")
        }
    }

    suspend fun uninstallSystemModule(): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        val script = "rm -rf /data/adb/modules/call_screener"
        val result = executeSuCommand(script)
        if (result.first) {
            Pair(true, "Module removed cleanly. Reboot to complete uninstallation.")
        } else {
            Pair(false, "Failed to remove module: ${result.second}")
        }
    }

    suspend fun rebootDevice(): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        executeSuCommand("reboot")
    }
}
