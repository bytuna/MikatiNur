package com.example.mkat_nur.ui.quran1

import android.app.PendingIntent
import android.content.Intent
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.example.mkat_nur.MainActivity

/**
 * Kur'an ses oynatmayı ön plan (foreground) servisiyle sürdürür.
 * Ekran kapalıyken de oynatmaya devam eder; sistem medya bildirimi ve
 * kulaklık düğmeleri bu servisteki oturuma yönlendirilir.
 * [QuranAudioPlayerManager] bu servise MediaController üzerinden bağlanır.
 */
class QuranPlaybackService : MediaSessionService() {

    private var mediaSession: MediaSession? = null

    override fun onCreate() {
        super.onCreate()
        val player = ExoPlayer.Builder(this)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(C.AUDIO_CONTENT_TYPE_SPEECH)
                    .setUsage(C.USAGE_MEDIA)
                    .build(),
                /* handleAudioFocus = */ true
            )
            // Ekran kapandığında CPU uyumasın; ses kesilmesin.
            .setWakeMode(C.WAKE_MODE_LOCAL)
            .setHandleAudioBecomingNoisy(true)
            .build()
        val pendingIntent = PendingIntent.getActivity(
            this,
            /* requestCode = */ 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        mediaSession = MediaSession.Builder(this, player)
            .setSessionActivity(pendingIntent)
            .build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? =
        mediaSession

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
        }
        mediaSession = null
        super.onDestroy()
    }
}
