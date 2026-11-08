package com.example.mkat_nur.ui.quran1

import android.content.Context
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Bir surenin tüm ayetlerini ExoPlayer'a oynatma listesi olarak yükler.
 * Çalan ayetin sırası [currentIndex] ile yayınlanır (liste sırası, 0'dan başlar).
 * Tüm çağrılar ana iş parçacığından yapılmalıdır.
 */
class QuranAudioPlayerManager(context: Context) {

    private val player: ExoPlayer = ExoPlayer.Builder(context.applicationContext).build().apply {
        setAudioAttributes(
            AudioAttributes.Builder()
                .setContentType(C.AUDIO_CONTENT_TYPE_SPEECH)
                .setUsage(C.USAGE_MEDIA)
                .build(),
            /* handleAudioFocus = */ true
        )
        setHandleAudioBecomingNoisy(true)
        repeatMode = Player.REPEAT_MODE_OFF
    }

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

    private val listener = object : Player.Listener {
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            _currentIndex.value = player.currentMediaItemIndex
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
        player.addListener(listener)
    }

    /** Aynı sure için tekrar çağrılırsa liste yeniden kurulmaz. */
    fun load(key: String, urls: List<String>) {
        if (loadedKey == key && itemCount == urls.size) return
        player.stop()
        player.clearMediaItems()
        player.setMediaItems(urls.map { MediaItem.fromUri(it) })
        itemCount = urls.size
        loadedKey = key
        _currentIndex.value = -1
        _error.value = null
        player.prepare()
    }

    fun playFrom(index: Int) {
        if (index !in 0 until itemCount) return
        _error.value = null
        player.seekTo(index, 0L)
        _currentIndex.value = index
        player.playWhenReady = true
        player.play()
    }

    fun togglePlayPause() {
        if (itemCount == 0) return
        if (player.isPlaying) {
            player.pause()
        } else {
            if (player.playbackState == Player.STATE_ENDED) {
                player.seekTo(0, 0L)
                _currentIndex.value = 0
            } else if (_currentIndex.value < 0) {
                _currentIndex.value = 0
            }
            player.play()
        }
    }

    fun pause() {
        if (player.isPlaying) player.pause()
    }

    fun next() {
        val i = player.currentMediaItemIndex
        if (i + 1 < itemCount) playFrom(i + 1)
    }

    fun previous() {
        val i = player.currentMediaItemIndex
        // 3 saniyeden fazla ilerlediyse aynı ayetin başına dön, değilse önceki ayete geç
        if (player.currentPosition > 3000L) playFrom(i.coerceAtLeast(0))
        else if (i - 1 >= 0) playFrom(i - 1)
        else playFrom(0)
    }

    fun stop() {
        player.stop()
        player.clearMediaItems()
        itemCount = 0
        loadedKey = null
        _currentIndex.value = -1
        _isPlaying.value = false
    }

    fun release() {
        player.removeListener(listener)
        player.release()
    }
}
