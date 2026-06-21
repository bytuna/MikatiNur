package com.example.mkat_nur.viewmodel

import android.app.Application
import android.media.AudioAttributes
import android.media.MediaMetadata
import android.media.MediaPlayer
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mkat_nur.model.Surah
import com.example.mkat_nur.model.Verse
import com.example.mkat_nur.network.QuranApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class QuranUiState {
    object Loading : QuranUiState()
    data class Success(val surahs: List<Surah>) : QuranUiState()
    data class Error(val message: String) : QuranUiState()
}

sealed class SurahDetailUiState {
    object Idle : SurahDetailUiState()
    object Loading : SurahDetailUiState()
    data class Success(val verses: List<Verse>) : SurahDetailUiState()
    data class Error(val message: String) : SurahDetailUiState()
}

class QuranViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences("mkat_nur_prefs", android.content.Context.MODE_PRIVATE)
    private val apiService = QuranApiService.create()
    private var mediaPlayer: MediaPlayer? = null
    private var mediaSession: MediaSession? = null

    private val _uiState = MutableStateFlow<QuranUiState>(QuranUiState.Loading)
    val uiState: StateFlow<QuranUiState> = _uiState

    private val _detailUiState = MutableStateFlow<SurahDetailUiState>(SurahDetailUiState.Idle)
    val detailUiState: StateFlow<SurahDetailUiState> = _detailUiState

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    private val _currentPlayingVerse = MutableStateFlow<Int?>(null)
    val currentPlayingVerse: StateFlow<Int?> = _currentPlayingVerse

    private val _currentPlayingSurahId = MutableStateFlow<Int?>(null)
    val currentPlayingSurahId: StateFlow<Int?> = _currentPlayingSurahId

    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused

    private var playlist: List<Verse> = emptyList()
    private var currentVerseIndex: Int = -1
    private var currentSurahId: Int = -1

    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed

    private val _selectedReciter = MutableStateFlow(prefs.getString("quran_reciter", "Yasser_Ad-Dussary_128kbps") ?: "Yasser_Ad-Dussary_128kbps")
    val selectedReciter: StateFlow<String> = _selectedReciter

    private val _selectedFont = MutableStateFlow(prefs.getString("quran_font", "Uthman Taha") ?: "Uthman Taha")
    val selectedFont: StateFlow<String> = _selectedFont

    init {
        fetchSurahs()
        setupMediaSession()
    }

    private fun setupMediaSession() {
        mediaSession = MediaSession(getApplication(), "QuranMediaSession").apply {
            setCallback(object : MediaSession.Callback() {
                override fun onPlay() {
                    togglePauseResume()
                }

                override fun onPause() {
                    togglePauseResume()
                }

                override fun onStop() {
                    stopAudio()
                }

                override fun onSkipToNext() {
                    playNextVerse()
                }

                override fun onSkipToPrevious() {
                    playPreviousVerse()
                }
            })
            isActive = true
        }
        updatePlaybackState(PlaybackState.STATE_STOPPED)
    }

    private fun updatePlaybackState(state: Int) {
        val speed = _playbackSpeed.value
        val position = mediaPlayer?.currentPosition?.toLong() ?: 0L
        val playbackState = PlaybackState.Builder()
            .setActions(
                PlaybackState.ACTION_PLAY or
                PlaybackState.ACTION_PAUSE or
                PlaybackState.ACTION_PLAY_PAUSE or
                PlaybackState.ACTION_STOP or
                PlaybackState.ACTION_SKIP_TO_NEXT or
                PlaybackState.ACTION_SKIP_TO_PREVIOUS
            )
            .setState(state, position, speed)
            .build()
        mediaSession?.setPlaybackState(playbackState)
    }

    private fun playNextVerse() {
        if (playlist.isNotEmpty() && currentVerseIndex < playlist.size - 1) {
            currentVerseIndex++
            val nextVerse = playlist[currentVerseIndex]
            playCurrentIndex(nextVerse.surahId, nextVerse.verseNumber)
        }
    }

    private fun playPreviousVerse() {
        if (playlist.isNotEmpty() && currentVerseIndex > 0) {
            currentVerseIndex--
            val prevVerse = playlist[currentVerseIndex]
            playCurrentIndex(prevVerse.surahId, prevVerse.verseNumber)
        }
    }

    fun playVerse(surahId: Int, verseNumber: Int, verses: List<Verse> = emptyList()) {
        if (_currentPlayingVerse.value == verseNumber && _currentPlayingSurahId.value == surahId && (_isPlaying.value || _isPaused.value)) {
            togglePauseResume()
            return
        }

        stopAudio()
        
        currentSurahId = surahId
        playlist = verses
        currentVerseIndex = if (verses.isNotEmpty()) {
            verses.indexOfFirst { it.surahId == surahId && it.verseNumber == verseNumber }
        } else {
            -1
        }

        playCurrentIndex(surahId, verseNumber)
    }

    fun setPlaybackSpeed(speed: Float) {
        _playbackSpeed.value = speed
        mediaPlayer?.let {
            if (it.isPlaying || _isPaused.value) {
                try {
                    it.playbackParams = it.playbackParams.setSpeed(speed)
                } catch (e: Exception) {
                    Log.e("QuranViewModel", "Error setting playback speed", e)
                }
            }
        }
    }

    fun setReciter(reciterKey: String) {
        val wasPlaying = _isPlaying.value
        val currentVerse = _currentPlayingVerse.value
        val currentSurah = currentSurahId
        
        _selectedReciter.value = reciterKey
        prefs.edit().putString("quran_reciter", reciterKey).apply()
        
        if (wasPlaying && currentVerse != null) {
            playVerse(currentSurah, currentVerse, playlist)
        }
    }

    fun setFont(fontName: String) {
        _selectedFont.value = fontName
        prefs.edit().putString("quran_font", fontName).apply()
    }

    private fun updateMetadata(surahId: Int, verseNumber: Int) {
        val metadata = MediaMetadata.Builder()
            .putString(MediaMetadata.METADATA_KEY_TITLE, "$verseNumber. Ayet")
            .putString(MediaMetadata.METADATA_KEY_ARTIST, "Sure: $surahId")
            .putString(MediaMetadata.METADATA_KEY_ALBUM, "Mîkat-ı Nur Kur'an")
            .build()
        mediaSession?.setMetadata(metadata)
    }

    private fun playCurrentIndex(surahId: Int, verseNumber: Int) {
        val surahStr = surahId.toString().padStart(3, '0')
        val verseStr = verseNumber.toString().padStart(3, '0')
        val url = "https://everyayah.com/data/${_selectedReciter.value}/$surahStr$verseStr.mp3"

        mediaPlayer?.release()
        mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            try {
                setDataSource(url)
                setOnPreparedListener { 
                    try {
                        it.playbackParams = it.playbackParams.setSpeed(_playbackSpeed.value)
                    } catch (e: Exception) {
                        Log.e("QuranViewModel", "Error setting speed on prepared", e)
                    }
                    it.start()
                    _isPlaying.value = true
                    _isPaused.value = false
                    _currentPlayingSurahId.value = surahId
                    _currentPlayingVerse.value = verseNumber
                    updatePlaybackState(PlaybackState.STATE_PLAYING)
                    updateMetadata(surahId, verseNumber)
                }
                setOnCompletionListener { 
                    _isPlaying.value = false
                    _currentPlayingVerse.value = null
                    
                    if (playlist.isNotEmpty() && currentVerseIndex < playlist.size - 1) {
                        currentVerseIndex++
                        val nextVerse = playlist[currentVerseIndex]
                        playCurrentIndex(nextVerse.surahId, nextVerse.verseNumber)
                    } else {
                        stopAudio()
                    }
                }
                setOnErrorListener { _, what, extra ->
                    Log.e("QuranViewModel", "MediaPlayer Error: what=$what extra=$extra")
                    stopAudio()
                    false
                }
                prepareAsync()
                updatePlaybackState(PlaybackState.STATE_BUFFERING)
            } catch (e: Exception) {
                Log.e("QuranViewModel", "Error setting data source", e)
                stopAudio()
            }
        }
    }

    fun togglePauseResume() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                _isPlaying.value = false
                _isPaused.value = true
                updatePlaybackState(PlaybackState.STATE_PAUSED)
            } else {
                it.start()
                _isPlaying.value = true
                _isPaused.value = false
                updatePlaybackState(PlaybackState.STATE_PLAYING)
            }
        }
    }

    fun stopAudio() {
        mediaPlayer?.apply {
            try { if (isPlaying) stop() } catch (e: Exception) {}
            release()
        }
        mediaPlayer = null
        _isPlaying.value = false
        _isPaused.value = false
        _currentPlayingSurahId.value = null
        _currentPlayingVerse.value = null
        updatePlaybackState(PlaybackState.STATE_STOPPED)
    }

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading

    private val _randomVerse = MutableStateFlow<Verse?>(null)
    val randomVerse: StateFlow<Verse?> = _randomVerse

    private val _randomSurahName = MutableStateFlow<String?>(null)
    val randomSurahName: StateFlow<String?> = _randomSurahName

    fun fetchRandomVerse() {
        viewModelScope.launch {
            _isAiLoading.value = true
            try {
                val response = apiService.getSurahs()
                val surahs = response.data
                if (surahs.isNotEmpty()) {
                    val randomSurah = surahs.random()
                    _randomSurahName.value = randomSurah.name
                    val randomVerseNum = (1..randomSurah.verseCount).random()
                    val verseKey = "${randomSurah.id}:$randomVerseNum"
                    
                    val resVerse = apiService.getVerseByKey(verseKey)
                    _randomVerse.value = resVerse.data
                }
            } catch (e: Exception) {
                Log.e("QuranViewModel", "Error fetching random verse", e)
            } finally {
                _isAiLoading.value = false
            }
        }
    }

    fun shareWithAi(context: android.content.Context, title: String, content: String, source: String, style: com.example.mkat_nur.util.AiImageService.ShareStyle = com.example.mkat_nur.util.AiImageService.ShareStyle.MINIMALIST, arabicText: String? = null) {
        viewModelScope.launch {
            _isAiLoading.value = true
            val aiBitmap = com.example.mkat_nur.util.AiImageService.generateAiBackground(content, style)
            _isAiLoading.value = false
            com.example.mkat_nur.util.ShareUtils.shareInfoAsImage(context, title, content, source, aiBitmap, arabicText)
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopAudio()
        mediaSession?.release()
        mediaSession = null
    }

    fun fetchSurahs() {
        viewModelScope.launch {
            _uiState.value = QuranUiState.Loading
            try {
                val response = apiService.getSurahs()
                _uiState.value = QuranUiState.Success(response.data)
            } catch (e: Exception) {
                _uiState.value = QuranUiState.Error(e.message ?: "Bir hata oluştu")
            }
        }
    }

    fun fetchSurahDetail(surahId: Int) {
        fetchVerses { apiService.getSurahDetail(surahId) }
    }

    fun fetchJuzDetail(juzId: Int) {
        fetchVerses { apiService.getVersesByJuz(juzId) }
    }

    fun fetchPageDetail(pageId: Int) {
        fetchVerses { apiService.getVersesByPage(pageId) }
    }

    fun saveScrollPosition(type: String, id: Int, index: Int) {
        prefs.edit().putInt("quran_scroll_${type}_$id", index).apply()
    }

    fun getSavedScrollPosition(type: String, id: Int): Int {
        return prefs.getInt("quran_scroll_${type}_$id", 0)
    }

    private fun fetchVerses(call: suspend () -> com.example.mkat_nur.model.VerseResponse) {
        viewModelScope.launch {
            _detailUiState.value = SurahDetailUiState.Loading
            try {
                val response = call()
                _detailUiState.value = SurahDetailUiState.Success(response.data)
            } catch (e: Exception) {
                _detailUiState.value = SurahDetailUiState.Error(e.message ?: "Bir hata oluştu")
            }
        }
    }
}
