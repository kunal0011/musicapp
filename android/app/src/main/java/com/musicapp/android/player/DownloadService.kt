package com.musicapp.android.player

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.musicapp.android.R
import com.musicapp.android.data.local.TrackDao
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URL
import javax.inject.Inject

/**
 * Foreground service that downloads tracks for offline playback.
 * Pass track ID and stream URL via intent extras.
 */
@AndroidEntryPoint
class DownloadService : LifecycleService() {

    @Inject lateinit var trackDao: TrackDao

    companion object {
        const val EXTRA_TRACK_ID = "track_id"
        const val EXTRA_STREAM_URL = "stream_url"
        const val EXTRA_TITLE = "title"
        const val CHANNEL_ID = "download_channel"
        const val NOTIFICATION_ID = 2001

        fun start(context: Context, trackId: Long, streamUrl: String, title: String) {
            val intent = Intent(context, DownloadService::class.java).apply {
                putExtra(EXTRA_TRACK_ID, trackId)
                putExtra(EXTRA_STREAM_URL, streamUrl)
                putExtra(EXTRA_TITLE, title)
            }
            context.startForegroundService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        val trackId = intent?.getLongExtra(EXTRA_TRACK_ID, -1L) ?: -1L
        val streamUrl = intent?.getStringExtra(EXTRA_STREAM_URL) ?: ""
        val title = intent?.getStringExtra(EXTRA_TITLE) ?: "Track"

        if (trackId == -1L || streamUrl.isEmpty()) {
            stopSelf()
            return START_NOT_STICKY
        }

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Downloading")
            .setContentText(title)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setOngoing(true)
            .setProgress(0, 0, true)
            .build()

        startForeground(NOTIFICATION_ID, notification)

        lifecycleScope.launch {
            try {
                val file = downloadTrack(trackId, streamUrl)
                trackDao.markDownloaded(trackId, file.absolutePath)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }

        return START_NOT_STICKY
    }

    private suspend fun downloadTrack(trackId: Long, url: String): File =
        withContext(Dispatchers.IO) {
            val offlineDir = File(filesDir, "offline")
            offlineDir.mkdirs()
            val file = File(offlineDir, "$trackId.mp3")

            URL(url).openStream().use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            file
        }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Track Downloads",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Notifications for track downloads"
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }
}
