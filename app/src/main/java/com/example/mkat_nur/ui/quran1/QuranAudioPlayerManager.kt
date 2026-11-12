package com.example.mkat_nur.ui.quran1

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.mkat_nur.MainActivity
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Bir surenin tüm ayetlerini oynatma listesi olarak yükler.
 * Çalan ayetin sırası [currentIndex] ile yayınlanır (liste sırası, 0'dan başlar).
 * Tüm çağrılar ana iş parçacığından yapılmalıdır.
 *
 * Oynatma, [QuranPlaybackService] içindeki MediaSession üzerinden yürütülür:
 * ekran kapalıyken ses kesilmez, kulaklık düğmeleri çalışmaya devam eder.
 * Bu sınıf yalnızca servisdeki oynatıcıyı kontrol eden bir MediaController sarmalayıcısıdır;
 * dış arayüz (StateFlow'lar ve komutlar) öncekiyle birebir aynıdır.
 */
class QuranAudioPlayerManager(context: Context) {

    private val appContext = context.applicationContext

    private val controllerFuture = MediaController.Builder(
        appContext,
        SessionToken(appContext, ComponentName(appContext, QuranPlaybackService::class.java))
    ).buildAsync()

    private var controller: MediaController? = null

    private val _currentIndex = MutableStateFlow(-1)
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _isBuffering = MutableStateFlow(false)
    val isBuffering: StateFlow<Boolean> = _isBuffering.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private var loadedKey: String? = null
    private var itemCount = 0
    private var pendingSpeed: Float? = null

    private val listener = object : Player.Listener {
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            _currentIndex.value = controller?.currentMediaItemIndex ?: -1
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _isPlaying.value = isPlaying
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            _isBuffering.value = playbackState == Player.STATE_BUFFERING
            if (playbackState == Player.STATE_ENDED) {
                _isPlaying.value = false
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            _error.value = "Ses çalınamadı. İnternet bağlantınızı kontrol edin."
            _isPlaying.value = false
        }
    }

    init {
        controllerFuture.addListener({
            val c = controllerFuture.get()
            c.addListener(listener)
            controller = c
            pendingSpeed?.let { c.setPlaybackSpeed(it) }
            pendingSpeed = null
        }, MoreExecutors.directExecutor())
    }

    /** Aynı sure için tekrar çağrılırsa liste yeniden kurulmaz. */
    fun load(key: String, urls: List<String>) {
        if (loadedKey == key && itemCount == urls.size) return
        val c = controller ?: return
        loadedKey = key
        itemCount = urls.size
        _currentIndex.value = -1
        _error.value = null
        c.setMediaItems(urls.map { MediaItem.fromUri(it) })
        c.prepare()
    }

    fun playFrom(index: Int) {
        if (index !in 0 until itemCount) return
        val c = controller ?: return
        _error.value = null
        c.seekTo(index, 0L)
        _currentIndex.value = index
        c.playWhenReady = true
        c.play()
    }

    fun togglePlayPause() {
        if (itemCount == 0) return
        val c = controller ?: return
        if (c.isPlaying) {
            c.pause()
        } else {
            if (c.playbackState == Player.STATE_ENDED) {
                c.seekTo(0, 0L)
                _currentIndex.value = 0
            } else if (_currentIndex.value < 0) {
                _currentIndex.value = 0
            }
            c.play()
        }
    }

    /** Oynatma hızı (ses perdesi değişmez). Sonraki ayetlerde de korunur. */
    fun setSpeed(speed: Float) {
        val c = controller
        if (c != null) c.setPlaybackSpeed(speed) else pendingSpeed = speed
    }

    fun pause() {
        val c = controller ?: return
        if (c.isPlaying) c.pause()
    }

    fun next() {
        val c = controller ?: return
        val i = c.currentMediaItemIndex
        if (i + 1 < itemCount) playFrom(i + 1)
    }

    fun previous() {
        val c = controller ?: return
        val i = c.currentMediaItemIndex
        // 3 saniyeden fazla ilerlediyse aynı ayetin başına dön, değilse önceki ayete geç
        if (c.currentPosition > 3000L) playFrom(i.coerceAtLeast(0))
        else if (i - 1 >= 0) playFrom(i - 1)
        else playFrom(0)
    }

    fun stop() {
        val c = controller ?: return
        c.stop()
        c.clearMediaItems()
        itemCount = 0
        loadedKey = null
        _currentIndex.value = -1
        _isPlaying.value = false
    }

    fun release() {
        controller?.removeListener(listener)
        controller?.release()
        controller = null
        MediaController.releaseFuture(controllerFuture)
    }
}
