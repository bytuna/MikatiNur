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
import com.example.mkat_nur.util.TajweedRule
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

enum class QuranMode {
    MEALLI,   // Arapça + Türkçe Meal
    MEALSIZ   // Sadece Arapça
}

enum class SurahSortMode(val label: String) {
    KURAN("Kur'an"),
    NUZUL("Nüzul"),
    ALFABETIK("Alfabetik")
}

class QuranViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences("mkat_nur_prefs", android.content.Context.MODE_PRIVATE)
    private val apiService = QuranApiService.create()
    private val cacheManager = com.example.mkat_nur.util.QuranCacheManager(application)
    private var mediaPlayer: MediaPlayer? = null
    private var mediaSession: MediaSession? = null

    // Bellek İçi Sayfa Önbelleği (Göz kırpma / titremeyi tamamen yok eder)
    private val _pagesMap = MutableStateFlow<Map<String, List<Verse>>>(emptyMap())
    val pagesMap: StateFlow<Map<String, List<Verse>>> = _pagesMap

    private val _activePage = MutableStateFlow(1)
    val activePage: StateFlow<Int> = _activePage

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

    private val _quranMode = MutableStateFlow(
        try {
            QuranMode.valueOf(prefs.getString("quran_mode", QuranMode.MEALLI.name) ?: QuranMode.MEALLI.name)
        } catch (e: Exception) {
            QuranMode.MEALLI
        }
    )
    val quranMode: StateFlow<QuranMode> = _quranMode

    private val _arabicFontSize = MutableStateFlow(prefs.getFloat("quran_arabic_font_size", 28f))
    val arabicFontSize: StateFlow<Float> = _arabicFontSize

    // Ezan Vakti Pro Özellikleri State'leri
    private val _showTransliteration = MutableStateFlow(prefs.getBoolean("quran_show_transliteration", true))
    val showTransliteration: StateFlow<Boolean> = _showTransliteration

    private val _selectedMealSource = MutableStateFlow(prefs.getString("quran_selected_meal", "Diyanet İşleri Başkanlığı (Türkçe)") ?: "Diyanet İşleri Başkanlığı (Türkçe)")
    val selectedMealSource: StateFlow<String> = _selectedMealSource

    private val _selectedMealId = MutableStateFlow(prefs.getString("quran_selected_meal_id", "77") ?: "77")
    val selectedMealId: StateFlow<String> = _selectedMealId

    private val _isDownloadingMeal = MutableStateFlow<String?>(null)
    val isDownloadingMeal: StateFlow<String?> = _isDownloadingMeal

    private val _downloadedMeals = MutableStateFlow<Set<String>>(
        prefs.getStringSet("quran_downloaded_meals", setOf("77")) ?: setOf("77")
    )
    val downloadedMeals: StateFlow<Set<String>> = _downloadedMeals

    private val _surahSortMode = MutableStateFlow(SurahSortMode.KURAN)
    val surahSortMode: StateFlow<SurahSortMode> = _surahSortMode

    private val _displayMode = MutableStateFlow(
        try {
            com.example.mkat_nur.model.QuranViewDisplayMode.valueOf(
                prefs.getString("quran_display_mode", com.example.mkat_nur.model.QuranViewDisplayMode.METIN_GORUNUMU.name) 
                ?: com.example.mkat_nur.model.QuranViewDisplayMode.METIN_GORUNUMU.name
            )
        } catch (e: Exception) {
            com.example.mkat_nur.model.QuranViewDisplayMode.METIN_GORUNUMU
        }
    )
    val displayMode: StateFlow<com.example.mkat_nur.model.QuranViewDisplayMode> = _displayMode

    private val _audioHighlightMode = MutableStateFlow(prefs.getString("quran_audio_highlight", "Oklu takip (desteklenen hafızlarda)") ?: "Oklu takip (desteklenen hafızlarda)")
    val audioHighlightMode: StateFlow<String> = _audioHighlightMode

    private val _tajweedEnabled = MutableStateFlow(prefs.getBoolean("quran_tajweed_enabled", true))
    val tajweedEnabled: StateFlow<Boolean> = _tajweedEnabled

    val defaultTajweedRules: Set<TajweedRule> = TajweedRule.entries.toSet()

    private val _tajweedActiveRules = MutableStateFlow<Set<TajweedRule>>(
        prefs.getStringSet("quran_tajweed_active_rules_m3", null)?.mapNotNull { name ->
            try { TajweedRule.valueOf(name) } catch (e: Exception) { null }
        }?.toSet() ?: defaultTajweedRules
    )
    val tajweedActiveRules: StateFlow<Set<TajweedRule>> = _tajweedActiveRules

    fun setTajweedEnabled(enabled: Boolean) {
        _tajweedEnabled.value = enabled
        prefs.edit().putBoolean("quran_tajweed_enabled", enabled).apply()

        if (enabled) {
            val allRules = TajweedRule.entries.toSet()
            _tajweedActiveRules.value = allRules
            prefs.edit().putStringSet("quran_tajweed_active_rules_m3", allRules.map { it.name }.toSet()).apply()
        } else {
            _tajweedActiveRules.value = emptySet()
            prefs.edit().putStringSet("quran_tajweed_active_rules_m3", emptySet()).apply()
        }
    }

    fun toggleTajweedRule(rule: TajweedRule) {
        val current = _tajweedActiveRules.value.toMutableSet()
        if (current.contains(rule)) {
            current.remove(rule)
        } else {
            current.add(rule)
        }
        _tajweedActiveRules.value = current
        prefs.edit().putStringSet("quran_tajweed_active_rules_m3", current.map { it.name }.toSet()).apply()

        val isAnyActive = current.isNotEmpty()
        if (_tajweedEnabled.value != isAnyActive) {
            _tajweedEnabled.value = isAnyActive
            prefs.edit().putBoolean("quran_tajweed_enabled", isAnyActive).apply()
        }
    }

    private val _bookmarks = MutableStateFlow(prefs.getStringSet("quran_bookmarks", emptySet()) ?: emptySet())
    val bookmarks: StateFlow<Set<String>> = _bookmarks

    private val _isRepeatEnabled = MutableStateFlow(prefs.getBoolean("quran_repeat_enabled", false))
    val isRepeatEnabled: StateFlow<Boolean> = _isRepeatEnabled

    // --- Hatim Takibi, İsim Dağıtımı State'leri ---
    private val _hatimStartJuz = MutableStateFlow(prefs.getInt("hatim_start_juz", 1))
    val hatimStartJuz: StateFlow<Int> = _hatimStartJuz

    private val _hatimCompletedJuzs = MutableStateFlow<Set<Int>>(
        (prefs.getStringSet("hatim_completed_juzs", emptySet()) ?: emptySet())
            .mapNotNull { it.toIntOrNull() }.toSet()
    )
    val hatimCompletedJuzs: StateFlow<Set<Int>> = _hatimCompletedJuzs

    private val _isHatimActive = MutableStateFlow(prefs.getBoolean("hatim_active", false))
    val isHatimActive: StateFlow<Boolean> = _isHatimActive

    // --- YALNIZCA CÜZLER SEKMESİNE ÖZEL SON KALINAN YER STATE'LERİ ---
    private val _lastJuzTabReadJuz = MutableStateFlow(prefs.getInt("quran_last_juz_tab_read_juz", 1))
    val lastJuzTabReadJuz: StateFlow<Int> = _lastJuzTabReadJuz

    private val _lastJuzTabReadPage = MutableStateFlow(prefs.getInt("quran_last_juz_tab_read_page", 1))
    val lastJuzTabReadPage: StateFlow<Int> = _lastJuzTabReadPage

    private val _hatimAssignments = MutableStateFlow<Map<Int, String>>(loadHatimAssignments())
    val hatimAssignments: StateFlow<Map<Int, String>> = _hatimAssignments

    private fun loadHatimAssignments(): Map<Int, String> {
        val map = mutableMapOf<Int, String>()
        for (i in 1..30) {
            val name = prefs.getString("hatim_assign_$i", "") ?: ""
            if (name.isNotBlank()) {
                map[i] = name
            }
        }
        return map
    }

    fun assignHatimJuz(juzNumber: Int, personName: String) {
        val current = _hatimAssignments.value.toMutableMap()
        if (personName.isBlank()) {
            current.remove(juzNumber)
            prefs.edit().remove("hatim_assign_$juzNumber").apply()
        } else {
            current[juzNumber] = personName
            prefs.edit().putString("hatim_assign_$juzNumber", personName).apply()
        }
        _hatimAssignments.value = current
    }

    fun getHatimShareText(): String {
        val startJuz = _hatimStartJuz.value
        val sequence = getHatimSequence(startJuz)
        val completed = _hatimCompletedJuzs.value
        val assignments = _hatimAssignments.value

        val sb = java.lang.StringBuilder()
        sb.append("🤲 MÎKAT-I NUR - 30 CÜZ HATİM DAĞITIM LİSTESİ 🤲\n\n")
        
        sequence.forEachIndexed { idx, juzNum ->
            val person = assignments[juzNum]
            val isDone = completed.contains(juzNum)
            val status = when {
                isDone -> "✅ (Tamamlandı${if (!person.isNullOrBlank()) " - $person" else ""})"
                !person.isNullOrBlank() -> "👤 $person (Alındı)"
                else -> "🟩 [BOŞTA - Alınabilir]"
            }
            sb.append("#${idx + 1} — $juzNum. Cüz: $status\n")
        }

        val completedCount = completed.size
        sb.append("\n📊 İlerleme: $completedCount / 30 Cüz Tamamlandı\n")
        sb.append("Siz de cüz alarak hatimimize katılabilirsiniz! Rabbim kabul eylesin.\n")
        sb.append("Mîkat-ı Nur Kur'an-ı Kerim")
        return sb.toString()
    }

    private fun pageNumKey(pageId: Int, transId: String): String = "page_${pageId}_trans_$transId"

    // Yalnızca Cüzler Sekmesinden Yapılan Okumalarda Konumu Kaydet
    fun saveJuzTabLastReadPosition(juz: Int, page: Int) {
        _lastJuzTabReadJuz.value = juz
        _lastJuzTabReadPage.value = page
        prefs.edit()
            .putInt("quran_last_juz_tab_read_juz", juz)
            .putInt("quran_last_juz_tab_read_page", page)
            .apply()
    }

    fun startHatim(startJuz: Int) {
        _hatimStartJuz.value = startJuz
        _hatimCompletedJuzs.value = emptySet()
        _isHatimActive.value = true
        prefs.edit()
            .putInt("hatim_start_juz", startJuz)
            .putStringSet("hatim_completed_juzs", emptySet())
            .putBoolean("hatim_active", true)
            .apply()
    }

    fun toggleHatimJuzCompleted(juzNumber: Int) {
        val current = _hatimCompletedJuzs.value.toMutableSet()
        if (current.contains(juzNumber)) {
            current.remove(juzNumber)
        } else {
            current.add(juzNumber)
        }
        _hatimCompletedJuzs.value = current
        prefs.edit().putStringSet("hatim_completed_juzs", current.map { it.toString() }.toSet()).apply()
    }

    fun resetHatim() {
        _isHatimActive.value = false
        _hatimCompletedJuzs.value = emptySet()
        prefs.edit().putBoolean("hatim_active", false).putStringSet("hatim_completed_juzs", emptySet()).apply()
    }

    fun getHatimSequence(startJuz: Int): List<Int> {
        return (0..29).map { i -> ((startJuz - 1 + i) % 30) + 1 }
    }

    fun toggleRepeat() {
        val next = !_isRepeatEnabled.value
        _isRepeatEnabled.value = next
        prefs.edit().putBoolean("quran_repeat_enabled", next).apply()
    }

    fun setSurahSortMode(mode: SurahSortMode) {
        _surahSortMode.value = mode
    }

    fun setShowTransliteration(show: Boolean) {
        _showTransliteration.value = show
        prefs.edit().putBoolean("quran_show_transliteration", show).apply()
    }

    fun setSelectedMeal(mealId: String, mealName: String) {
        _selectedMealId.value = mealId
        _selectedMealSource.value = mealName
        prefs.edit().putString("quran_selected_meal_id", mealId).putString("quran_selected_meal", mealName).apply()
    }

    fun downloadMeal(translationId: String) {
        viewModelScope.launch {
            _isDownloadingMeal.value = translationId
            try {
                for (i in 1..114) {
                    val response = apiService.getSurahDetail(i, translations = translationId)
                    val key = "surah_${i}_trans_$translationId"
                    cacheManager.saveVerses(key, response)
                }
                val set = _downloadedMeals.value.toMutableSet()
                set.add(translationId)
                _downloadedMeals.value = set
                prefs.edit().putStringSet("quran_downloaded_meals", set).apply()
            } catch (e: Exception) {
                Log.e("QuranViewModel", "Error downloading meal $translationId", e)
            } finally {
                _isDownloadingMeal.value = null
            }
        }
    }

    fun setDisplayMode(mode: com.example.mkat_nur.model.QuranViewDisplayMode) {
        _displayMode.value = mode
        prefs.edit().putString("quran_display_mode", mode.name).apply()
    }

    fun setAudioHighlightMode(mode: String) {
        _audioHighlightMode.value = mode
        prefs.edit().putString("quran_audio_highlight", mode).apply()
    }

    fun toggleBookmark(verseKey: String) {
        val current = _bookmarks.value.toMutableSet()
        if (current.contains(verseKey)) {
            current.remove(verseKey)
        } else {
            current.add(verseKey)
        }
        _bookmarks.value = current
        prefs.edit().putStringSet("quran_bookmarks", current).apply()
    }

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

    fun setQuranMode(mode: QuranMode) {
        _quranMode.value = mode
        prefs.edit().putString("quran_mode", mode.name).apply()
    }

    fun setArabicFontSize(size: Float) {
        _arabicFontSize.value = size
        prefs.edit().putFloat("quran_arabic_font_size", size).apply()
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
                    
                    if (_isRepeatEnabled.value && currentVerseIndex != -1 && playlist.isNotEmpty()) {
                        val currentVerse = playlist[currentVerseIndex]
                        playCurrentIndex(currentVerse.surahId, currentVerse.verseNumber)
                    } else if (playlist.isNotEmpty() && currentVerseIndex < playlist.size - 1) {
                        currentVerseIndex++
                        val nextVerse = playlist[currentVerseIndex]
                        playCurrentIndex(nextVerse.surahId, nextVerse.verseNumber)
                    } else {
                        // Sayfa bittiğinde otomatik bir sonraki sayfaya geç ve okumaya devam et
                        val currPage = _activePage.value
                        if (currPage in 1..603) {
                            val nextPage = currPage + 1
                            playNextPage(nextPage)
                        } else {
                            stopAudio()
                        }
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

    private fun playNextPage(nextPage: Int) {
        viewModelScope.launch {
            _activePage.value = nextPage
            val transId = _selectedMealId.value
            val key = pageNumKey(nextPage, transId)
            
            val pageVerses = _pagesMap.value[key] ?: run {
                try {
                    val res = apiService.getVersesByPage(nextPage, translations = transId)
                    cacheManager.saveVerses(key, res)
                    _pagesMap.value = _pagesMap.value + (key to res.data)
                    res.data
                } catch (e: Exception) {
                    cacheManager.loadVerses(key)?.data ?: emptyList()
                }
            }

            if (pageVerses.isNotEmpty()) {
                playlist = pageVerses
                currentVerseIndex = 0
                val firstVerse = pageVerses.first()
                playCurrentIndex(firstVerse.surahId, firstVerse.verseNumber)
            } else {
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
                cacheManager.saveSurahs(response)
                _uiState.value = QuranUiState.Success(response.data)
            } catch (e: Exception) {
                val cached = cacheManager.loadSurahs()
                if (cached != null && cached.data.isNotEmpty()) {
                    _uiState.value = QuranUiState.Success(cached.data)
                } else {
                    _uiState.value = QuranUiState.Error(e.message ?: "İnternet bağlantısı yok")
                }
            }
        }
    }

    fun fetchSurahDetail(surahId: Int) {
        val transId = _selectedMealId.value
        val cacheKey = "surah_${surahId}_trans_$transId"
        fetchVerses(cacheKey) { apiService.getSurahDetail(surahId, translations = transId) }
    }

    fun fetchJuzDetail(juzId: Int) {
        val transId = _selectedMealId.value
        fetchVerses("juz_${juzId}_trans_$transId") { apiService.getVersesByJuz(juzId, translations = transId) }
    }

    fun fetchPageDetail(pageId: Int) {
        _activePage.value = pageId
        val transId = _selectedMealId.value

        viewModelScope.launch {
            loadAndCachePage(pageId, transId)

            // Ön yükleme: Bitişik sayfaları (P-1, P+1, P+2) arka planda sessizce yükle (0 ms göz kırpma)
            val adjacentPages = listOf(pageId - 1, pageId + 1, pageId + 2).filter { it in 1..604 }
            for (adjPage in adjacentPages) {
                val key = pageNumKey(adjPage, transId)
                if (!_pagesMap.value.containsKey(key)) {
                    launch { loadAndCachePage(adjPage, transId) }
                }
            }
        }
    }

    private suspend fun loadAndCachePage(pageId: Int, transId: String) {
        val key = pageNumKey(pageId, transId)
        if (_pagesMap.value.containsKey(key)) {
            val cached = _pagesMap.value[key]
            if (cached != null) {
                _detailUiState.value = SurahDetailUiState.Success(cached)
            }
            return
        }

        val diskCached = cacheManager.loadVerses(key)
        if (diskCached != null && diskCached.data.isNotEmpty()) {
            _pagesMap.value = _pagesMap.value + (key to diskCached.data)
            if (_activePage.value == pageId) {
                _detailUiState.value = SurahDetailUiState.Success(diskCached.data)
            }
            return
        }

        try {
            val response = apiService.getVersesByPage(pageId, translations = transId)
            cacheManager.saveVerses(key, response)
            _pagesMap.value = _pagesMap.value + (key to response.data)
            if (_activePage.value == pageId) {
                _detailUiState.value = SurahDetailUiState.Success(response.data)
            }
        } catch (e: Exception) {
            Log.e("QuranViewModel", "Error fetching page $pageId", e)
        }
    }

    fun saveScrollPosition(type: String, id: Int, index: Int) {
        prefs.edit().putInt("quran_scroll_${type}_$id", index).apply()
    }

    fun getSavedScrollPosition(type: String, id: Int): Int {
        return prefs.getInt("quran_scroll_${type}_$id", 0)
    }

    private fun fetchVerses(cacheKey: String, call: suspend () -> com.example.mkat_nur.model.VerseResponse) {
        viewModelScope.launch {
            val diskCached = cacheManager.loadVerses(cacheKey)
            if (diskCached != null && diskCached.data.isNotEmpty()) {
                _detailUiState.value = SurahDetailUiState.Success(diskCached.data)
                return@launch
            }

            _detailUiState.value = SurahDetailUiState.Loading
            try {
                val response = call()
                cacheManager.saveVerses(cacheKey, response)
                _detailUiState.value = SurahDetailUiState.Success(response.data)
            } catch (e: Exception) {
                _detailUiState.value = SurahDetailUiState.Error("Çevrimdışı içerik bulunamadı. Lütfen internete bağlıyken ilgili sureyi bir kez açınız.")
            }
        }
    }
}
