package com.example.mkat_nur.service

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log

object EzanPlayerManager {
    private var activePlayer: MediaPlayer? = null

    fun playEzan(context: Context, soundUri: Uri) {
        stopEzan()
        try {
            activePlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .build()
                )
                setDataSource(context, soundUri)
                prepare()
                start()
                setOnCompletionListener {
                    stopEzan()
                }
            }
        } catch (e: Exception) {
            Log.e("EzanPlayerManager", "Play error: ${e.message}")
        }
    }

    fun stopEzan() {
        try {
            activePlayer?.stop()
            activePlayer?.release()
        } catch (_: Exception) {}
        activePlayer = null
    }

    fun isPlaying(): Boolean = activePlayer?.isPlaying == true
}

class EzanStopReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_STOP_EZAN) {
            EzanPlayerManager.stopEzan()
            try {
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.cancel(NOTIFICATION_ID_EZAN)
            } catch (_: Exception) {}
        }
    }

    companion object {
        const val ACTION_STOP_EZAN = "com.example.mkat_nur.ACTION_STOP_EZAN"
        const val NOTIFICATION_ID_EZAN = 99123
    }
}
