/**
 * SuikaaWave Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.suikaawave.music.update

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.os.Build
import androidx.core.app.NotificationCompat
import com.suikaawave.music.R
import timber.log.Timber
import java.io.File

/** Menerima hasil sesi PackageInstaller dari [UpdateDownloadService]. */
class UpdateInstallReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        val nm = context.getSystemService(NotificationManager::class.java)
        when (val status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE)) {
            PackageInstaller.STATUS_PENDING_USER_ACTION -> {
                // Android minta konfirmasi user (install pertama kali / syarat update diam-diam tidak terpenuhi)
                val confirm = confirmIntent(intent) ?: return
                confirm.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

                // Kalau app lagi di latar belakang, startActivity bisa diblok sistem,
                // jadi sediakan juga notifikasi yang bisa diketuk.
                val tap =
                    PendingIntent.getActivity(context, 0, confirm, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
                nm.notify(
                    UpdateDownloadService.CONFIRM_NOTIFICATION_ID,
                    NotificationCompat
                        .Builder(context, UpdateDownloadService.CHANNEL_ID)
                        .setSmallIcon(R.drawable.update)
                        .setContentTitle(context.getString(R.string.update_ready_title))
                        .setContentText(context.getString(R.string.update_ready_message))
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setCategory(NotificationCompat.CATEGORY_STATUS)
                        .setContentIntent(tap)
                        .setAutoCancel(true)
                        .build(),
                )
                runCatching { context.startActivity(confirm) }
                    .onFailure { Timber.w(it, "Gagal membuka konfirmasi install, pakai notifikasi") }
            }

            PackageInstaller.STATUS_SUCCESS -> {
                nm.cancel(UpdateDownloadService.CONFIRM_NOTIFICATION_ID)
                File(context.cacheDir, "update").deleteRecursively()
                AppUpdateInstaller.reset()
            }

            else -> {
                nm.cancel(UpdateDownloadService.CONFIRM_NOTIFICATION_ID)
                val message = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE) ?: "Install gagal ($status)"
                Timber.w("Install update gagal: $message")
                AppUpdateInstaller.setState(UpdateState.Failed(message))
            }
        }
    }

    private fun confirmIntent(intent: Intent): Intent? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(Intent.EXTRA_INTENT, Intent::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(Intent.EXTRA_INTENT)
        }
}
