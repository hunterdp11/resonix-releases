package com.callscreener.app.ota

import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.annotation.Keep
import androidx.core.content.FileProvider
import androidx.core.content.pm.PackageInfoCompat
import com.callscreener.app.root.RootPrivilegeManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

@Keep
data class UpdateInfo(
    val versionCode: Long,
    val versionName: String,
    val changelog: String,
    val downloadUrl: String,
    val fileSize: String = "",
    val releaseDate: String = "",
    val minSupportedVersion: Long = 1L,
    val isMandatory: Boolean = false
)

sealed class UpdateCheckStatus {
    object Idle : UpdateCheckStatus()
    object Checking : UpdateCheckStatus()
    data class UpdateAvailable(val info: UpdateInfo) : UpdateCheckStatus()
    data class UpToDate(val currentVersion: String) : UpdateCheckStatus()
    data class Error(val message: String) : UpdateCheckStatus()
}

sealed class DownloadStatus {
    object Idle : DownloadStatus()
    data class Downloading(
        val progress: Float,
        val downloadedBytes: Long,
        val totalBytes: Long
    ) : DownloadStatus()
    data class Downloaded(val apkFile: File, val info: UpdateInfo) : DownloadStatus()
    object Installing : DownloadStatus()
    data class Error(val message: String) : DownloadStatus()
}

object OtaUpdateManager {

    private const val TAG = "OtaUpdateManager"
    private const val PREFS_NAME = "resonix_ota_prefs"
    private const val KEY_UPDATE_URL = "ota_feed_url"

    const val DEFAULT_UPDATE_URL = "https://raw.githubusercontent.com/hunterdp11/resonix-releases/main/update.json"

    private val _checkStatus = MutableStateFlow<UpdateCheckStatus>(UpdateCheckStatus.Idle)
    val checkStatus: StateFlow<UpdateCheckStatus> = _checkStatus.asStateFlow()

    private val _downloadStatus = MutableStateFlow<DownloadStatus>(DownloadStatus.Idle)
    val downloadStatus: StateFlow<DownloadStatus> = _downloadStatus.asStateFlow()

    fun getUpdateUrl(context: Context? = null): String = DEFAULT_UPDATE_URL

    fun getCurrentVersionCode(context: Context): Long {
        return try {
            val pInfo: PackageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            PackageInfoCompat.getLongVersionCode(pInfo)
        } catch (e: Exception) {
            1L
        }
    }

    fun getCurrentVersionName(context: Context): String {
        return try {
            val pInfo: PackageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            pInfo.versionName ?: "1.0.0"
        } catch (e: Exception) {
            "1.0.0"
        }
    }

    fun resetState() {
        _checkStatus.value = UpdateCheckStatus.Idle
        _downloadStatus.value = DownloadStatus.Idle
    }

    suspend fun checkForUpdates(context: Context, customUrl: String? = null): UpdateCheckStatus = withContext(Dispatchers.IO) {
        _checkStatus.value = UpdateCheckStatus.Checking
        val feedUrl = customUrl?.takeIf { it.isNotBlank() } ?: getUpdateUrl(context)
        val currentCode = getCurrentVersionCode(context)
        val currentName = getCurrentVersionName(context)

        try {
            val jsonText = fetchUrlWithRedirects(feedUrl)
            val json = JSONObject(jsonText)

            val remoteVersionCode = json.optLong("versionCode", 0L)
            val remoteVersionName = json.optString("versionName", "")
            val changelog = json.optString("changelog", "• General improvements and stability fixes.")
            val downloadUrl = json.optString("downloadUrl", "")
            val fileSize = json.optString("fileSize", "")
            val releaseDate = json.optString("releaseDate", "")
            val minSupportedVersion = json.optLong("minSupportedVersion", 1L)
            val isMandatory = json.optBoolean("isMandatory", false)

            if (downloadUrl.isBlank()) {
                val err = UpdateCheckStatus.Error("Invalid update feed: downloadUrl is missing")
                _checkStatus.value = err
                return@withContext err
            }

            if (remoteVersionCode > currentCode) {
                val info = UpdateInfo(
                    versionCode = remoteVersionCode,
                    versionName = remoteVersionName.ifBlank { "v$remoteVersionCode" },
                    changelog = changelog,
                    downloadUrl = downloadUrl,
                    fileSize = fileSize,
                    releaseDate = releaseDate,
                    minSupportedVersion = minSupportedVersion,
                    isMandatory = isMandatory
                )
                val status = UpdateCheckStatus.UpdateAvailable(info)
                _checkStatus.value = status
                status
            } else {
                val status = UpdateCheckStatus.UpToDate(currentName)
                _checkStatus.value = status
                status
            }
        } catch (e: Exception) {
            Log.e(TAG, "OTA update check failed", e)
            val err = UpdateCheckStatus.Error(e.localizedMessage ?: "Network connection error")
            _checkStatus.value = err
            err
        }
    }

    suspend fun downloadUpdate(
        context: Context,
        info: UpdateInfo
    ): Result<File> = withContext(Dispatchers.IO) {
        val updatesDir = File(context.cacheDir, "updates")
        if (!updatesDir.exists()) updatesDir.mkdirs()

        val safeName = "Resonix-v${info.versionName}.apk"
        val outputFile = File(updatesDir, safeName)

        if (outputFile.exists()) outputFile.delete()

        try {
            _downloadStatus.value = DownloadStatus.Downloading(0.01f, 0L, 0L)

            val connection = openConnectionWithRedirects(info.downloadUrl)
            val totalBytes = connection.contentLength.toLong()

            connection.inputStream.use { input ->
                FileOutputStream(outputFile).use { output ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    var totalRead = 0L
                    var lastReportTime = System.currentTimeMillis()

                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        totalRead += bytesRead

                        val now = System.currentTimeMillis()
                        if (now - lastReportTime > 80 || totalRead == totalBytes) {
                            lastReportTime = now
                            val progress = if (totalBytes > 0) {
                                (totalRead.toFloat() / totalBytes.toFloat()).coerceIn(0.01f, 1.0f)
                            } else {
                                -1.0f
                            }
                            _downloadStatus.value = DownloadStatus.Downloading(progress, totalRead, totalBytes)
                        }
                    }
                    output.flush()
                }
            }

            if (!outputFile.exists() || outputFile.length() == 0L) {
                throw IllegalStateException("Downloaded APK file is empty or missing")
            }

            _downloadStatus.value = DownloadStatus.Downloaded(outputFile, info)
            Result.success(outputFile)
        } catch (e: Exception) {
            Log.e(TAG, "Download failed", e)
            outputFile.delete()
            _downloadStatus.value = DownloadStatus.Error(e.localizedMessage ?: "Download failed")
            Result.failure(e)
        }
    }

    fun canRequestPackageInstalls(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.packageManager.canRequestPackageInstalls()
        } else {
            true
        }
    }

    fun installUpdateStandard(context: Context, apkFile: File): Result<Unit> {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !context.packageManager.canRequestPackageInstalls()) {
                val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                    data = Uri.parse("package:${context.packageName}")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                return Result.failure(SecurityException("Please grant permission to install updates, then tap install again."))
            }

            val apkUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                apkFile
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            _downloadStatus.value = DownloadStatus.Installing
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Standard install trigger failed", e)
            Result.failure(e)
        }
    }

    suspend fun installUpdateRoot(context: Context, apkFile: File): Result<String> = withContext(Dispatchers.IO) {
        _downloadStatus.value = DownloadStatus.Installing
        val apkPath = apkFile.absolutePath

        val (pmSuccess, pmOutput) = RootPrivilegeManager.executeSuCommand("pm install -r \"$apkPath\"")

        if (!pmSuccess) {
            Log.w(TAG, "Root pm install failed: $pmOutput, falling back to standard installer")
            withContext(Dispatchers.Main) {
                installUpdateStandard(context, apkFile)
            }
            return@withContext Result.failure(Exception("Root install failed: $pmOutput"))
        }

        val modulePrivAppPath = "/data/adb/modules/resonix/system/priv-app/Resonix/Resonix.apk"
        val moduleDir = "/data/adb/modules/resonix"
        RootPrivilegeManager.executeSuCommand(
            "if [ -d \"$moduleDir\" ]; then cp -f \"$apkPath\" \"$modulePrivAppPath\" && chmod 644 \"$modulePrivAppPath\"; fi"
        )

        Result.success("Update installed successfully via Superuser! Resonix is now up to date.")
    }

    private fun fetchUrlWithRedirects(urlString: String, maxRedirects: Int = 5): String {
        var currentUrl = urlString
        var redirects = 0

        while (redirects < maxRedirects) {
            val url = URL(currentUrl)
            val conn = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 12000
                readTimeout = 15000
                instanceFollowRedirects = false
                setRequestProperty("User-Agent", "Resonix-OTA/1.0.0")
                setRequestProperty("Accept", "application/json, text/plain, */*")
            }

            conn.connect()
            val code = conn.responseCode

            if (code in 300..399) {
                val newLocation = conn.getHeaderField("Location")
                conn.disconnect()
                if (newLocation != null && newLocation.isNotBlank()) {
                    currentUrl = if (newLocation.startsWith("http")) {
                        newLocation
                    } else {
                        URL(url, newLocation).toString()
                    }
                    redirects++
                    continue
                } else {
                    throw IllegalStateException("HTTP redirect ($code) with no Location header")
                }
            }

            if (code != HttpURLConnection.HTTP_OK) {
                conn.disconnect()
                throw IllegalStateException("HTTP $code: ${conn.responseMessage}")
            }

            return conn.inputStream.bufferedReader().use { it.readText() }.also {
                conn.disconnect()
            }
        }

        throw IllegalStateException("Too many redirects ($redirects)")
    }

    private fun openConnectionWithRedirects(urlString: String, maxRedirects: Int = 5): HttpURLConnection {
        var currentUrl = urlString
        var redirects = 0

        while (redirects < maxRedirects) {
            val url = URL(currentUrl)
            val conn = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 15000
                readTimeout = 30000
                instanceFollowRedirects = false
                setRequestProperty("User-Agent", "Resonix-OTA/1.0.0")
            }

            val code = conn.responseCode
            if (code in 300..399) {
                val newLocation = conn.getHeaderField("Location")
                conn.disconnect()
                if (newLocation != null && newLocation.isNotBlank()) {
                    currentUrl = if (newLocation.startsWith("http")) {
                        newLocation
                    } else {
                        URL(url, newLocation).toString()
                    }
                    redirects++
                    continue
                } else {
                    throw IllegalStateException("HTTP redirect ($code) with no Location header")
                }
            }

            if (code != HttpURLConnection.HTTP_OK) {
                conn.disconnect()
                throw IllegalStateException("HTTP $code: ${conn.responseMessage}")
            }

            return conn
        }

        throw IllegalStateException("Too many redirects ($redirects)")
    }
}
