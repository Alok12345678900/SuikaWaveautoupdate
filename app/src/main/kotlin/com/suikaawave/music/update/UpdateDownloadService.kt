/**
 * SuikaaWave Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.suikaawave.music.update

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageInstaller
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.suikaawave.music.MainActivity
import com.suikaawave.music.R
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import timber.log.Timber
import java.io.File
import java.io.IOException

class UpdateDownloadService : Service() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var job: Job? = null
    private val client = OkHttpClient()

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {
        if (intent?.action == ACTION_CANCEL) {
            job?.cancel()
            client.dispatcher.cancelAll()
            AppUpdateInstaller.reset()
            stopForegroundCompat()
            stopSelf()
            return START_NOT_STICKY
        }

        val url = intent?.getStringExtra(EXTRA_URL)
        val version = intent?.getStringExtra(EXTRA_VERSION).orEmpty()
        if (url == null) {
            stopSelf()
            return START_NOT_STICKY
        }

        createChannel()
        startForegroundCompat(buildNotification(version, 0, -1L, indeterminate = true))

        job?.cancel()
        job =
            scope.launch {
                try {
                    val apk = download(url, version)
                    AppUpdateInstaller.setState(UpdateState.Installing(version))
                    notify(buildNotification(version, 100, -1L, indeterminate = true, installing = true))
                    install(apk)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Timber.tag(TAG).e(e, "Update gagal")
                    AppUpdateInstaller.setState(UpdateState.Failed(e.message ?: "Update gagal"))
                    stopForegroundCompat()
                    stopSelf()
                }
            }
        return START_NOT_STICKY
    }

    private suspend fun download(
        url: String,
        version: String,
    ): File {
        val dir = File(cacheDir, "update").apply { mkdirs() }
        dir.listFiles()?.forEach { it.delete() }
        val target = File(dir, "update.apk")

        client.newCall(Request.Builder().url(url).build()).execute().use { response ->
            if (!response.isSuccessful) throw IOException("HTTP ${response.code}")
            val body = response.body ?: throw IOException("Respons kosong")
            val total = body.contentLength()
            var downloaded = 0L
            var lastUpdate = 0L
            body.byteStream().use { input ->
                target.outputStream().use { output ->
                    val buffer = ByteArray(64 * 1024)
                    while (true) {
                        val read = input.read(buffer)
                        if (read < 0) break
                        output.write(buffer, 0, read)
                        downloaded += read
                        val now = System.currentTimeMillis()
                        if (now - lastUpdate > 300) {
                            lastUpdate = now
                            currentCoroutineContext().ensureActive()
                            AppUpdateInstaller.setState(UpdateState.Downloading(version, downloaded, total))
                            val percent = if (total > 0) (downloaded * 100 / total).toInt() else 0
                            notify(buildNotification(version, percent, total, indeterminate = total <= 0))
                        }
                    }
                }
            }
            if (total > 0 && downloaded != total) throw IOException("Download tidak lengkap")
            AppUpdateInstaller.setState(UpdateState.Downloading(version, downloaded, total))
        }
        return target
    }

    private fun install(apk: File) {
        val installer = packageManager.packageInstaller
        val params =
            PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL).apply {
                setAppPackageName(packageName)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    // Android 12+: update tanpa dialog konfirmasi kalau syarat sistem terpenuhi
                    setRequireUserAction(PackageInstaller.SessionParams.USER_ACTION_NOT_REQUIRED)
                }
            }
        val sessionId = installer.createSession(params)
        installer.openSession(sessionId).use { session ->
            apk.inputStream().use { input ->
                session.openWrite("update.apk", 0, apk.length()).use { out ->
                    input.copyTo(out)
                    session.fsync(out)
                }
            }
            val pending =
                PendingIntent.getBroadcast(
                    this,
                    sessionId,
                    Intent(this, UpdateInstallReceiver::class.java).setPackage(packageName),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE,
                )
            session.commit(pending.intentSender)
        }
        // Hasil install diterima UpdateInstallReceiver; service boleh berhenti.
        stopForegroundCompat()
        stopSelf()
    }

    private fun createChannel() {
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, getString(R.string.update_channel_name), NotificationManager.IMPORTANCE_LOW),
        )
    }

    private fun buildNotification(
        version: String,
        percent: Int,
        total: Long,
        indeterminate: Boolean,
        installing: Boolean = false,
    ): Notification {
        val openApp =
            PendingIntent.getActivity(
                this,
                0,
                Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
                PendingIntent.FLAG_IMMUTABLE,
            )
        val cancel =
            PendingIntent.getService(
                this,
                1,
                Intent(this, UpdateDownloadService::class.java).setAction(ACTION_CANCEL),
                PendingIntent.FLAG_IMMUTABLE,
            )
        return NotificationCompat
            .Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.update)
            .setContentTitle(getString(if (installing) R.string.update_installing else R.string.update_downloading))
            .setContentText(if (installing || total <= 0) version else "$version • $percent%")
            .setProgress(100, percent, indeterminate)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(openApp)
            .apply {
                if (!installing) addAction(R.drawable.close, getString(R.string.update_cancel), cancel)
            }.build()
    }

    private fun notify(notification: Notification) {
        getSystemService(NotificationManager::class.java).notify(NOTIFICATION_ID, notification)
    }

    private fun startForegroundCompat(notification: Notification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun stopForegroundCompat() {
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    companion object {
        private const val TAG = "UpdateDownloadService"
        const val EXTRA_URL = "url"
        const val EXTRA_VERSION = "version"
        const val ACTION_CANCEL = "com.suikaawave.music.action.CANCEL_UPDATE"
        const val CHANNEL_ID = "app_update_download"
        const val NOTIFICATION_ID = 7421
        const val CONFIRM_NOTIFICATION_ID = 7422
    }
}
