package com.example.mkat_nur.service

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.util.Log

object EzanPlayerManager {
    private var activePlayer: MediaPlayer? = null
    private var activeRingtone: Ringtone? = null

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
            Log.e("EzanPlayerManager", "Play ezan error: ${e.message}")
        }
    }

    fun playRingtone(context: Context, soundUri: Uri) {
        stopEzan()
        try {
            activeRingtone = RingtoneManager.getRingtone(context, soundUri)?.apply {
                play()
            }
        } catch (e: Exception) {
            Log.e("EzanPlayerManager", "Play ringtone error: ${e.message}")
        }
    }

    fun stopEzan() {
        try {
            activePlayer?.stop()
            activePlayer?.release()
        } catch (_: Exception) {}
        activePlayer = null

        try {
            activeRingtone?.stop()
        } catch (_: Exception) {}
        activeRingtone = null
    }

    fun isPlaying(): Boolean = (activePlayer?.isPlaying == true) || (activeRingtone?.isPlaying == true)
}

class EzanStopReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("EzanStopReceiver", "Received stop ezan action: ${intent.action}")
        EzanPlayerManager.stopEzan()
        try {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(NOTIFICATION_ID_EZAN)
            notificationManager.cancelAll()
        } catch (e: Exception) {
            Log.e("EzanStopReceiver", "Cancel error: ${e.message}")
        }
    }

    companion object {
        const val ACTION_STOP_EZAN = "com.example.mkat_nur.ACTION_STOP_EZAN"
        const val NOTIFICATION_ID_EZAN = 99123
    }
}
