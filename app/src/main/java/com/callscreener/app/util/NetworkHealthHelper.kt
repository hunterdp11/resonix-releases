package com.callscreener.app.util

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import com.callscreener.app.root.RootPrivilegeManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.Socket

enum class NetworkHealthStatus {
    CONNECTED,
    RESTRICTED_BY_SYSTEM, // Crossed earth icon! (Data usage restricted by OEM/MIUI or Data Saver)
    NO_INTERNET,
    NO_NETWORK
}

object NetworkHealthHelper {

    private const val TAG = "NetworkHealthHelper"

    /**
     * Checks if this app is allowed to open network connections.
     * Accurately detects when MIUI/HyperOS or OEM Data Saver displays the crossed-earth icon.
     */
    suspend fun checkNetworkHealth(context: Context): NetworkHealthStatus = withContext(Dispatchers.IO) {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return@withContext NetworkHealthStatus.NO_NETWORK

        val activeNet = cm.activeNetwork
        if (activeNet == null) {
            return@withContext NetworkHealthStatus.NO_NETWORK
        }

        val caps = cm.getNetworkCapabilities(activeNet)
        if (caps == null || !caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
            return@withContext NetworkHealthStatus.NO_INTERNET
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (cm.restrictBackgroundStatus == ConnectivityManager.RESTRICT_BACKGROUND_STATUS_ENABLED) {
                return@withContext NetworkHealthStatus.RESTRICTED_BY_SYSTEM
            }
        }

        // Live TCP Socket probe to detect if OEM firewall (MIUI crossed earth) is blocking this app's sockets
        val isSocketAllowed = try {
            Socket().use { socket ->
                socket.connect(InetSocketAddress("8.8.8.8", 53), 1500)
                true
            }
        } catch (e: Exception) {
            Log.d(TAG, "Socket probe blocked: ${e.message}")
            false
        }

        if (!isSocketAllowed) {
            return@withContext NetworkHealthStatus.RESTRICTED_BY_SYSTEM
        }

        NetworkHealthStatus.CONNECTED
    }

    /**
     * Attempts to unblock network restrictions automatically using root privileges.
     * Fixes MIUI custom AppOps 10021 (WIFI) and 10022 (DATA), netpolicy whitelist, and iptables miui_firewall.
     */
    suspend fun unblockViaRoot(context: Context): Boolean = withContext(Dispatchers.IO) {
        if (!RootPrivilegeManager.isRootAvailable()) return@withContext false
        try {
            val pkg = context.packageName
            val uid = context.applicationInfo.uid
            val script = """
                cmd appops set $pkg 10021 allow 2>/dev/null
                cmd appops set $pkg 10022 allow 2>/dev/null
                cmd appops set $pkg 10023 allow 2>/dev/null
                cmd appops set $pkg 10021 0 2>/dev/null
                cmd appops set $pkg 10022 0 2>/dev/null
                cmd appops set $pkg 10023 0 2>/dev/null
                cmd netpolicy add restrict-background-whitelist $pkg 2>/dev/null
                cmd netpolicy remove restrict-background-blacklist $pkg 2>/dev/null
                cmd appops set $pkg RUN_IN_BACKGROUND allow 2>/dev/null
                cmd appops set $pkg DATA_SAVER allow 2>/dev/null
                iptables -D miui_firewall -m owner --uid-owner $uid -j DROP 2>/dev/null
                iptables -D miui_firewall -m owner --uid-owner $uid -j REJECT 2>/dev/null
                iptables -D OUTPUT -m owner --uid-owner $uid -j DROP 2>/dev/null
                iptables -D OUTPUT -m owner --uid-owner $uid -j REJECT 2>/dev/null
                ip6tables -D miui_firewall -m owner --uid-owner $uid -j DROP 2>/dev/null
                ip6tables -D miui_firewall -m owner --uid-owner $uid -j REJECT 2>/dev/null
                ip6tables -D OUTPUT -m owner --uid-owner $uid -j DROP 2>/dev/null
                ip6tables -D OUTPUT -m owner --uid-owner $uid -j REJECT 2>/dev/null
            """.trimIndent()
            val result = RootPrivilegeManager.executeSuCommand(script)
            result.first
        } catch (e: Exception) {
            Log.e(TAG, "Root unblock failed", e)
            false
        }
    }

    /**
     * Opens the system App Details / Data Usage page so the user can easily
     * enable Wi-Fi, Mobile Data, or unrestrict background data for CallScreener.
     */
    fun openDataUsageSettings(context: Context) {
        // 1. Try MIUI specific PermissionsEditorActivity
        try {
            val miuiIntent = Intent("miui.intent.action.APP_PERM_EDITOR").apply {
                setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.PermissionsEditorActivity")
                putExtra("extra_pkgname", context.packageName)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(miuiIntent)
            return
        } catch (_: Exception) {}

        // 2. Try standard Android App Details (On Xiaomi, ColorOS, Samsung, etc. this directly exposes 'Restrict data usage')
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            return
        } catch (_: Exception) {}

        // 3. Fallback to Ignore Background Data Restrictions
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val intent = Intent(Settings.ACTION_IGNORE_BACKGROUND_DATA_RESTRICTIONS_SETTINGS).apply {
                    data = Uri.parse("package:${context.packageName}")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open settings", e)
        }
    }
}
