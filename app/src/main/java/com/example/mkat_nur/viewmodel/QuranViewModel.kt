package com.example.mkat_nur.viewmodel

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log
import androidx.lifecycle.ViewModel
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

class QuranViewModel : ViewModel() {
    private val apiService = QuranApiService.create()
    private var mediaPlayer: MediaPlayer? = null

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

    init {
        fetchSurahs()
    }

    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused

    private var playlist: List<Verse> = emptyList()
    private var currentVerseIndex: Int = -1
    private var currentSurahId: Int = -1

    fun playVerse(surahId: Int, verseNumber: Int, verses: List<Verse> = emptyList()) {
        if (_currentPlayingVerse.value == verseNumber && _currentPlayingSurahId.value == surahId && (_isPlaying.value || _isPaused.value)) {
            togglePauseResume()
            return
        }

        stopAudio()
        
        currentSurahId = surahId
        playlist = verses
        currentVerseIndex = if (verses.isNotEmpty()) {
            // Hem sure ID hem de ayet numarasına bakarak doğru sırayı buluyoruz
            verses.indexOfFirst { it.surahId == surahId && it.verseNumber == verseNumber }
        } else {
            -1
        }

        playCurrentIndex(surahId, verseNumber)
    }

    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed

    private val _selectedReciter = MutableStateFlow("Alafasy_128kbps")
    val selectedReciter: StateFlow<String> = _selectedReciter

    private val _selectedFont = MutableStateFlow("System")
    val selectedFont: StateFlow<String> = _selectedFont

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
        
        if (wasPlaying && currentVerse != null) {
            playVerse(currentSurah, currentVerse, playlist)
        }
    }

    fun setFont(fontName: String) {
        _selectedFont.value = fontName
    }

    private fun playCurrentIndex(surahId: Int, verseNumber: Int) {
        val surahStr = surahId.toString().padStart(3, '0')
        val verseStr = verseNumber.toString().padStart(3, '0')
        val url = "https://everyayah.com/data/${_selectedReciter.value}/$surahStr$verseStr.mp3"

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
                }
                setOnCompletionListener { 
                    _isPlaying.value = false
                    _currentPlayingVerse.value = null
                    
                    if (playlist.isNotEmpty() && currentVerseIndex < playlist.size - 1) {
                        currentVerseIndex++
                        val nextVerse = playlist[currentVerseIndex]
                        // Surenin değişip değişmediğini kontrol et
                        val nextSurahId = nextVerse.surahId
                        playCurrentIndex(nextSurahId, nextVerse.verseNumber)
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
            } else {
                it.start()
                _isPlaying.value = true
                _isPaused.value = false
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
                // Rastgele bir sure ve o sureden rastgele bir ayet seç
                val surahs = (apiService.getSurahs().data)
                if (surahs.isNotEmpty()) {
                    val randomSurah = surahs.random()
                    _randomSurahName.value = randomSurah.name
                    val randomVerseNum = (1..randomSurah.verseCount).random()
                    val verseKey = "${randomSurah.id}:$randomVerseNum"
                    
                    val response = apiService.getVerseByKey(verseKey)
                    _randomVerse.value = response.data
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
    }

    fun fetchSurahs() {
        viewModelScope.launch {
            _uiState.value = QuranUiState.Loading
            try {
                Log.d("QuranViewModel", "Fetching surahs")
                val response = apiService.getSurahs()
                val surahs = response.data
                Log.d("QuranViewModel", "Surahs fetched successfully: ${surahs.size} surahs")
                _uiState.value = QuranUiState.Success(surahs)
            } catch (e: Exception) {
                Log.e("QuranViewModel", "Error fetching surahs", e)
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

    private fun fetchVerses(call: suspend () -> com.example.mkat_nur.model.VerseResponse) {
        viewModelScope.launch {
            _detailUiState.value = SurahDetailUiState.Loading
            try {
                val response = call()
                val verses = response.data
                _detailUiState.value = SurahDetailUiState.Success(verses)
            } catch (e: Exception) {
                Log.e("QuranViewModel", "Error fetching verses", e)
                _detailUiState.value = SurahDetailUiState.Error(e.message ?: "Bir hata oluştu")
            }
        }
    }
}
