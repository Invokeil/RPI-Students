package com.rpi.student.update

import android.app.DownloadManager
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import com.rpi.student.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * OTA update — GitHub Releases থেকে:
 *  1) /releases/latest চেক (tag: v1.0.N → N = versionCode)
 *  2) নতুন হলে .apk asset DownloadManager দিয়ে নামায়
 *  3) শেষ হলে ACTION_VIEW install intent (REQUEST_INSTALL_PACKAGES)
 * Play Store publish হলে এই রাস্তা Play in-app update দিয়ে replace হবে।
 */
object UpdateManager {

    data class UpdateInfo(
        val versionCode: Long,
        val versionName: String,
        val apkUrl: String,
        val notes: String,
    )

    sealed class CheckResult {
        data object UpToDate : CheckResult()
        data class Available(val info: UpdateInfo) : CheckResult()
        data object NoRelease : CheckResult()
        data class Error(val message: String) : CheckResult()
    }

    suspend fun check(): CheckResult = withContext(Dispatchers.IO) {
        runCatching {
            val conn = URL("https://api.github.com/repos/${BuildConfig.GITHUB_REPO}/releases/latest")
                .openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.setRequestProperty("Accept", "application/vnd.github+json")
            conn.setRequestProperty("User-Agent", "${BuildConfig.APP_NAME}-android-ota")
            conn.connectTimeout = 12_000
            conn.readTimeout = 12_000

            val code = conn.responseCode
            if (code == 404) return@withContext CheckResult.NoRelease
            if (code != 200) return@withContext CheckResult.Error("GitHub HTTP $code")

            val body = conn.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(body)
            val tag = json.optString("tag_name", "")
            val remoteCode = tag.substringAfterLast('.').toLongOrNull() ?: 0L
            val notes = json.optString("body", "")

            val assets = json.getJSONArray("assets")
            var apkUrl: String? = null
            for (i in 0 until assets.length()) {
                val a = assets.getJSONObject(i)
                if (a.optString("name", "").endsWith(".apk")) {
                    apkUrl = a.optString("browser_download_url", "")
                    break
                }
            }

            if (apkUrl == null) return@withContext CheckResult.NoRelease
            if (remoteCode <= BuildConfig.VERSION_CODE.toLong()) return@withContext CheckResult.UpToDate

            CheckResult.Available(
                UpdateInfo(
                    versionCode = remoteCode,
                    versionName = tag.removePrefix("v"),
                    apkUrl = apkUrl,
                    notes = notes,
                )
            )
        }.getOrElse { CheckResult.Error(it.message ?: "নেটওয়ার্ক সমস্যা") }
    }

    /** DownloadManager enqueue → downloadId। */
    fun startDownload(context: Context, info: UpdateInfo): Long {
        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val req = DownloadManager.Request(Uri.parse(info.apkUrl)).apply {
            setTitle("${BuildConfig.APP_NAME} ${info.versionName}")
            setDescription("OTA update ডাউনলোড হচ্ছে...")
            setMimeType("application/vnd.android.package-archive")
            setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "${BuildConfig.APP_NAME}-${info.versionName}.apk")
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setAllowedOverMetered(true)
        }
        return dm.enqueue(req)
    }

    sealed class DownloadState {
        data class Progress(val percent: Int) : DownloadState()
        data object Done : DownloadState()
        data class Failed(val message: String) : DownloadState()
    }

    fun queryState(context: Context, downloadId: Long): DownloadState {
        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val q = DownloadManager.Query().setFilterById(downloadId)
        val c: Cursor = dm.query(q)
        c.use {
            if (it.moveToFirst()) {
                val status = it.getInt(it.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                val downloaded = it.getLong(it.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                val total = it.getLong(it.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                val reason = it.getInt(it.getColumnIndexOrThrow(DownloadManager.COLUMN_REASON))
                return when (status) {
                    DownloadManager.STATUS_SUCCESSFUL -> DownloadState.Done
                    DownloadManager.STATUS_FAILED -> DownloadState.Failed("Download fail (reason $reason)")
                    DownloadManager.STATUS_RUNNING, DownloadManager.STATUS_PAUSED, DownloadManager.STATUS_PENDING ->
                        DownloadState.Progress(if (total > 0) (downloaded * 100 / total).toInt() else 0)
                    else -> DownloadState.Progress(0)
                }
            }
        }
        return DownloadState.Failed("Download পাওয়া যায়নি")
    }

    /** ডাউনলোড শেষ APK install prompt। */
    fun install(context: Context, downloadId: Long) {
        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val uri: Uri = dm.getUriForDownloadedFile(downloadId) ?: return
        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(intent)
    }

    fun canInstall(context: Context): Boolean =
        if (android.os.Build.VERSION.SDK_INT >= 26) {
            context.packageManager.canRequestPackageInstalls()
        } else true

    fun requestInstallPermission(context: Context) {
        if (android.os.Build.VERSION.SDK_INT >= 26) {
            val intent = android.content.Intent(
                android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                Uri.parse("package:${context.packageName}"),
            )
            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }
}
