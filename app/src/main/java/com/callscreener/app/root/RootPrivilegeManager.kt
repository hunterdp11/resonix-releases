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
    STANDARD,
    ROOT_ENHANCED
}

object RootPrivilegeManager {

    private const val TAG = "RootPrivilegeManager"
    private const val PREFS_NAME = "call_screener_mode_prefs"
    private const val KEY_OPERATION_MODE = "app_operation_mode"
    private const val KEY_MODULE_PENDING_REBOOT = "module_installed_pending_reboot"
    private const val KEY_MODULE_INSTALL_TIME = "module_install_timestamp"
    private const val KEY_MODULE_APK_PATH = "module_installed_apk_path"

    fun getOperationMode(context: Context): AppOperationMode {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val saved = prefs.getString(KEY_OPERATION_MODE, null)
        if (saved != null) {
            val mode = try {
                AppOperationMode.valueOf(saved)
            } catch (_: Exception) {
                AppOperationMode.STANDARD
            }
            if (mode == AppOperationMode.ROOT_ENHANCED) {
                val hasRoot = isRootAvailable()
                val isPriv = isPrivilegedApp(context)
                val hasModule = hasModuleFilesOnDisk()
                if (!hasRoot || (!isPriv && !hasModule)) {
                    setOperationMode(context, AppOperationMode.STANDARD)
                    return AppOperationMode.STANDARD
                }
            }
            return mode
        }
        val hasModule = hasModuleFilesOnDisk() || isPrivilegedApp(context)
        return if (hasModule && isRootAvailable()) {
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

    @Volatile private var cachedRootAvailable: Boolean? = null
    @Volatile private var cachedPrivilegedApp: Boolean? = null
    @Volatile private var cachedModuleInstalled: Boolean? = null
    @Volatile private var isWarmingUp = false
    @Volatile private var isWarmedUp = false

    fun isRootAvailableCached(): Boolean = cachedRootAvailable ?: false
    fun isPrivilegedAppCached(context: Context? = null): Boolean = cachedPrivilegedApp ?: false
    fun isMagiskModuleInstalledCached(context: Context? = null): Boolean = cachedModuleInstalled ?: false

    fun invalidateCache() {
        cachedRootAvailable = null
        cachedPrivilegedApp = null
        cachedModuleInstalled = null
        isWarmedUp = false
    }

    fun warmUp(context: Context) {
        if (isWarmingUp || isWarmedUp) return
        isWarmingUp = true
        executeAsync {
            try {
                kotlinx.coroutines.delay(800)
                val root = isRootAvailable()
                if (root) {
                    val pkg = context.packageName
                    val uid = context.applicationInfo.uid
                    executeSuCommand("pm grant $pkg android.permission.MODIFY_PHONE_STATE 2>/dev/null; pm grant $pkg android.permission.CALL_PHONE 2>/dev/null; cmd netpolicy add restrict-background-whitelist $pkg 2>/dev/null; cmd appops set $pkg RUN_IN_BACKGROUND allow 2>/dev/null; cmd appops set $pkg DATA_SAVER allow 2>/dev/null; iptables -D OUTPUT -m owner --uid-owner $uid -j REJECT 2>/dev/null; ip6tables -D OUTPUT -m owner --uid-owner $uid -j REJECT 2>/dev/null")
                }
                isPrivilegedApp(context, forceRefresh = true)
                isMagiskModuleInstalled(context, forceRefresh = true)
                isWarmedUp = true
            } finally {
                isWarmingUp = false
            }
        }
    }

    fun isRootModeActive(context: Context): Boolean {
        if (getOperationMode(context) != AppOperationMode.ROOT_ENHANCED) return false
        val root = isRootAvailable()
        val priv = isPrivilegedApp(context)
        val hasModule = hasModuleFilesOnDisk()
        return root && (priv || hasModule)
    }

    fun isRootAvailable(): Boolean {
        cachedRootAvailable?.let { return it }
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", "id"))
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val output = reader.readLine()
            process.waitFor()
            val result = output != null && output.contains("uid=0")
            cachedRootAvailable = result
            result
        } catch (e: Exception) {
            cachedRootAvailable = false
            false
        }
    }

    fun isPrivilegedApp(context: Context, forceRefresh: Boolean = false): Boolean {
        if (!forceRefresh) {
            cachedPrivilegedApp?.let { return it }
        }
        return try {
            val appInfo = context.packageManager.getApplicationInfo(context.packageName, 0)
            val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            val sourceDir = appInfo.sourceDir ?: ""
            val inPrivApp = sourceDir.contains("/priv-app/") ||
                            sourceDir.startsWith("/system/priv-app") ||
                            sourceDir.startsWith("/system_ext/priv-app") ||
                            sourceDir.startsWith("/product/priv-app")
            val hasCaptureAudio = context.checkSelfPermission("android.permission.CAPTURE_AUDIO_OUTPUT") == PackageManager.PERMISSION_GRANTED
            val onDisk = hasModuleFilesOnDisk()

            val result = (hasCaptureAudio || (isSystem && inPrivApp)) && (onDisk || isSystem)
            cachedPrivilegedApp = result
            result
        } catch (e: Exception) {
            cachedPrivilegedApp = false
            false
        }
    }

    fun hasModuleFilesOnDisk(): Boolean {
        return try {
            val script = "((test -d /data/adb/modules/resonix && test -f /data/adb/modules/resonix/module.prop && ! test -f /data/adb/modules/resonix/disable && ! test -f /data/adb/modules/resonix/remove) || (test -d /data/adb/modules/call_screener && test -f /data/adb/modules/call_screener/module.prop && ! test -f /data/adb/modules/call_screener/disable && ! test -f /data/adb/modules/call_screener/remove)) && echo 'yes' || echo 'no'"
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", script))
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val line = reader.readLine()
            process.waitFor()
            line != null && line.trim() == "yes"
        } catch (_: Exception) {
            false
        }
    }

    fun isMagiskModuleInstalled(context: Context? = null, forceRefresh: Boolean = false): Boolean {
        if (!forceRefresh) {
            cachedModuleInstalled?.let { return it }
        }
        val onDisk = hasModuleFilesOnDisk()
        if (!onDisk) {
            cachedModuleInstalled = false
            return false
        }
        if (context != null && isPrivilegedApp(context, forceRefresh)) {
            cachedModuleInstalled = true
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            if (prefs.getBoolean(KEY_MODULE_PENDING_REBOOT, false)) {
                prefs.edit().putBoolean(KEY_MODULE_PENDING_REBOOT, false).apply()
            }
            return true
        }

        if (context != null) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val pendingReboot = prefs.getBoolean(KEY_MODULE_PENDING_REBOOT, false)
            if (pendingReboot && onDisk) {
                cachedModuleInstalled = true
                return true
            }
        }

        cachedModuleInstalled = onDisk
        return onDisk
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

    suspend fun installSystemModule(context: Context): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        val apkPath = context.packageCodePath
        val packageName = context.packageName

        val script = """
            MODULE_DIR="/data/adb/modules/resonix"
            mkdir -p "${'$'}MODULE_DIR/system/priv-app/Resonix"
            mkdir -p "${'$'}MODULE_DIR/system/etc/permissions"

            # 1. Copy active APK into system priv-app
            cp "$apkPath" "${'$'}MODULE_DIR/system/priv-app/Resonix/Resonix.apk"
            chmod 755 "${'$'}MODULE_DIR"
            chmod 755 "${'$'}MODULE_DIR/system"
            chmod 755 "${'$'}MODULE_DIR/system/priv-app"
            chmod 755 "${'$'}MODULE_DIR/system/priv-app/Resonix"
            chmod 644 "${'$'}MODULE_DIR/system/priv-app/Resonix/Resonix.apk"

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
id=resonix
name=Resonix Audio & Telephony Priv-App
version=1.0.0
versionCode=1
author=DpXarma
description=Systemless mount of Resonix into /system/priv-app with direct native audio routing & HAL capture privileges.
EOF
            chmod 644 "${'$'}MODULE_DIR/module.prop"
            rm -f "${'$'}MODULE_DIR/disable"
            rm -f "${'$'}MODULE_DIR/remove"

            # 4. Set SELinux Contexts so system_server can read the priv-app without permission denial
            chcon -R u:object_r:system_file:s0 "${'$'}MODULE_DIR/system"

            echo "Installed Magisk/KernelSU module successfully."
        """.trimIndent()

        val result = executeSuCommand(script)
        if (result.first) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit()
                .putBoolean(KEY_MODULE_PENDING_REBOOT, true)
                .putLong(KEY_MODULE_INSTALL_TIME, System.currentTimeMillis())
                .putString(KEY_MODULE_APK_PATH, apkPath)
                .apply()
            invalidateCache()
            cachedModuleInstalled = true
            Pair(true, "Module installed to Magisk/KernelSU! Please reboot your phone now to activate.")
        } else {
            Pair(false, "Failed to create module: ${result.second}")
        }
    }

    suspend fun uninstallSystemModule(context: Context? = null): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        val script = "rm -rf /data/adb/modules/resonix /data/adb/modules/call_screener"
        val result = executeSuCommand(script)
        context?.let { ctx ->
            val prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit()
                .putBoolean(KEY_MODULE_PENDING_REBOOT, false)
                .remove(KEY_MODULE_INSTALL_TIME)
                .remove(KEY_MODULE_APK_PATH)
                .putString(KEY_OPERATION_MODE, AppOperationMode.STANDARD.name)
                .apply()
        }
        invalidateCache()
        cachedModuleInstalled = false
        cachedPrivilegedApp = false
        Pair(true, "Module uninstalled cleanly. Switched to Standard mode.")
    }

    suspend fun rebootDevice(): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        executeSuCommand("reboot")
    }
}
