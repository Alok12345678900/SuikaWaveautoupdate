/**
 * SuikaaWave Project (C) 2026
 * Licensed under GPL-3.0 | See git history for contributors
 */

package com.suikaawave.music.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.suikaawave.music.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

sealed interface UpdateState {
    data object Idle : UpdateState

    data class Downloading(
        val version: String,
        val downloadedBytes: Long,
        val totalBytes: Long,
    ) : UpdateState {
        val progress: Float
            get() = if (totalBytes > 0) (downloadedBytes.toFloat() / totalBytes).coerceIn(0f, 1f) else 0f
    }

    data class Installing(val version: String) : UpdateState

    data class Failed(val message: String) : UpdateState
}

/**
 * Download APK update langsung dari dalam app (jalan di latar belakang lewat foreground
 * service), lalu langsung diinstall lewat PackageInstaller tanpa buka browser / file manager.
 */
object AppUpdateInstaller {
    private val _state = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val state: StateFlow<UpdateState> = _state

    internal fun setState(newState: UpdateState) {
        _state.value = newState
    }

    fun reset() {
        _state.value = UpdateState.Idle
    }

    fun canInstallPackages(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.O || context.packageManager.canRequestPackageInstalls()

    /** Buka layar "Izinkan install dari sumber ini" untuk app ini. */
    fun openInstallPermissionSettings(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        context.startActivity(
            Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:${context.packageName}"))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
    }

    /**
     * Mulai download + install. Return false kalau izin "install app tidak dikenal" belum
     * diberikan (layar izin dibuka, user perlu menekan Update lagi setelah mengizinkan).
     */
    fun start(
        context: Context,
        url: String,
        version: String,
    ): Boolean {
        if (!canInstallPackages(context)) {
            openInstallPermissionSettings(context)
            return false
        }
        if (_state.value is UpdateState.Downloading || _state.value is UpdateState.Installing) return true
        _state.value = UpdateState.Downloading(version, 0L, -1L)
        ContextCompat.startForegroundService(
            context,
            Intent(context, UpdateDownloadService::class.java)
                .putExtra(UpdateDownloadService.EXTRA_URL, url)
                .putExtra(UpdateDownloadService.EXTRA_VERSION, version),
        )
        return true
    }

    /** Sama seperti [start], tapi kasih Toast petunjuk kalau izin install belum diberikan. */
    fun startOrExplain(
        context: Context,
        url: String,
        version: String,
    ) {
        if (!start(context, url, version)) {
            Toast.makeText(context, R.string.update_allow_install, Toast.LENGTH_LONG).show()
        }
    }

    fun cancel(context: Context) {
        context.startService(
            Intent(context, UpdateDownloadService::class.java).setAction(UpdateDownloadService.ACTION_CANCEL),
        )
        _state.value = UpdateState.Idle
    }
}
