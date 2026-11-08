package com.example.mkat_nur.ui.quran1

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mkat_nur.model.SurahResponse
import com.example.mkat_nur.util.QuranTransliterationHelper
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

sealed class Quran1UiState {
    /** Sure listesi görünüyor, açık sure yok. */
    object ListView : Quran1UiState()
    object Loading : Quran1UiState()
    data class Success(val surah: Quran1Surah, val fromCache: Boolean) : Quran1UiState()
    data class Error(val message: String) : Quran1UiState()
}

class Quran1ViewModel(application: Application) : AndroidViewModel(application) {

    private val gson = Gson()
    private val api = AlQuranCloudApi.instance
    val audio = QuranAudioPlayerManager(application)

    private val _surahList = MutableStateFlow<kotlin.collections.List<Quran1SurahInfo>>(emptyList())
    val surahList: StateFlow<kotlin.collections.List<Quran1SurahInfo>> = _surahList.asStateFlow()

    private val _uiState = MutableStateFlow<Quran1UiState>(Quran1UiState.ListView)
    val uiState: StateFlow<Quran1UiState> = _uiState.asStateFlow()

    private val _openSurahNumber = MutableStateFlow<Int?>(null)
    val openSurahNumber: StateFlow<Int?> = _openSurahNumber.asStateFlow()

    /** Çalan ayetin sure içindeki sırası (0'dan). Oynatıcıdan gelir. */
    val currentPlayingAyahIndex: StateFlow<Int> = audio.currentIndex
    val isPlaying: StateFlow<Boolean> = audio.isPlaying
    val isBuffering: StateFlow<Boolean> = audio.isBuffering
    val audioError: StateFlow<String?> = audio.error

    init {
        Quran1Settings.init(application)
        loadSurahList()
    }

    private fun loadSurahList() {
        viewModelScope.launch {
            val list = withContext(Dispatchers.IO) {
                try {
                    getApplication<Application>().assets.open("quran_surahs.json").bufferedReader().use { r ->
                        gson.fromJson(r, SurahResponse::class.java).data.map {
                            Quran1SurahInfo(
                                number = it.id,
                                arabicName = it.arabicName ?: "",
                                turkishName = it.name ?: "",
                                verseCount = it.verseCount
                            )
                        }
                    }
                } catch (e: Exception) {
                    Log.e("Quran1", "Sure listesi okunamadı: ${e.message}")
                    emptyList()
                }
            }
            _surahList.value = list
        }
    }

    fun openSurah(number: Int) {
        _openSurahNumber.value = number
        loadSurah(number)
    }

    fun closeSurah() {
        audio.stop()
        _openSurahNumber.value = null
        _uiState.value = Quran1UiState.ListView
    }

    fun retry() {
        _openSurahNumber.value?.let { loadSurah(it) }
    }

    // ------------------------------------------------------------------ Veri

    private fun cacheFile(number: Int): File {
        val dir = File(getApplication<Application>().filesDir, "quran1_cache")
        if (!dir.exists()) dir.mkdirs()
        return File(dir, "surah_$number.json")
    }

    private fun loadSurah(number: Int) {
        viewModelScope.launch {
            audio.stop()
            _uiState.value = Quran1UiState.Loading

            val result: Pair<kotlin.collections.List<AlQuranSurahEdition>, Boolean>? = withContext(Dispatchers.IO) {
                try {
                    val response = try {
                        api.getSurahEditions(number, AlQuranCloudApi.ALL_EDITIONS)
                    } catch (first: Exception) {
                        // Okunuş edisyonu bulunamazsa onsuz yeniden dene (okunuş uygulama içi kaynaktan tamamlanır)
                        Log.w("Quran1", "Tam istek başarısız (${first.message}), okunuşsuz deneniyor")
                        api.getSurahEditions(number, AlQuranCloudApi.EDITIONS_WITHOUT_TRANSLITERATION)
                    }
                    val data = response.data
                    if (response.code == 200 && !data.isNullOrEmpty()) {
                        try { cacheFile(number).writeText(gson.toJson(data)) } catch (_: Exception) { }
                        data to false
                    } else null
                } catch (e: Exception) {
                    Log.e("Quran1", "Sure alınamadı: ${e.message}")
                    // Çevrimdışı: son indirilen kopyayı kullan
                    try {
                        val f = cacheFile(number)
                        if (f.exists()) {
                            val arr = gson.fromJson(f.readText(), Array<AlQuranSurahEdition>::class.java)
                            arr.toList() to true
                        } else null
                    } catch (_: Exception) { null }
                }
            }

            if (result == null) {
                _uiState.value = Quran1UiState.Error("Sure yüklenemedi. İnternet bağlantınızı kontrol edip tekrar deneyin.")
                return@launch
            }

            val surah = merge(number, result.first)
            if (surah == null) {
                _uiState.value = Quran1UiState.Error("Sure verisi okunamadı.")
                return@launch
            }

            val urls = surah.ayahs.map { it.audioUrl }
            if (urls.all { !it.isNullOrBlank() }) {
                audio.load("surah_$number", urls.map { it!! })
            }
            _uiState.value = Quran1UiState.Success(surah, result.second)
        }
    }

    private fun merge(number: Int, editions: kotlin.collections.List<AlQuranSurahEdition>): Quran1Surah? {
        fun find(id: String) = editions.firstOrNull { it.edition?.identifier == id }

        val uthmani = find(AlQuranCloudApi.EDITION_UTHMANI) ?: editions.firstOrNull() ?: return null
        val tajweed = find(AlQuranCloudApi.EDITION_TAJWEED)
        val translit = find(AlQuranCloudApi.EDITION_TRANSLITERATION)
        val meal = find(AlQuranCloudApi.EDITION_MEAL)
        val audio = find(AlQuranCloudApi.EDITION_AUDIO)

        fun byNumber(e: AlQuranSurahEdition?) = e?.ayahs?.associateBy { it.numberInSurah } ?: emptyMap()
        val tajweedMap = byNumber(tajweed)
        val translitMap = byNumber(translit)
        val mealMap = byNumber(meal)
        val audioMap = byNumber(audio)

        val info = _surahList.value.firstOrNull { it.number == number }

        val ayahs = (uthmani.ayahs ?: return null).map { a ->
            val arabic = a.text.orEmpty()
            Quran1Ayah(
                numberInSurah = a.numberInSurah,
                globalNumber = a.number,
                page = a.page,
                juz = a.juz,
                uthmani = arabic,
                tajweed = tajweedMap[a.numberInSurah]?.text,
                transliteration = translitMap[a.numberInSurah]?.text?.takeIf { it.isNotBlank() }
                    ?: QuranTransliterationHelper.getTransliteration("$number:${a.numberInSurah}", arabic),
                meal = mealMap[a.numberInSurah]?.text,
                audioUrl = audioMap[a.numberInSurah]?.let { it.audio ?: it.audioSecondary?.firstOrNull() }
            )
        }

        return Quran1Surah(
            number = number,
            arabicName = info?.arabicName ?: uthmani.name.orEmpty(),
            turkishName = info?.turkishName ?: uthmani.englishName.orEmpty(),
            ayahs = ayahs
        )
    }

    // ------------------------------------------------------------------ Ses

    fun playFrom(index: Int) = audio.playFrom(index)
    fun togglePlayPause() = audio.togglePlayPause()
    fun nextAyah() = audio.next()
    fun previousAyah() = audio.previous()
    fun pauseAudio() = audio.pause()

    override fun onCleared() {
        audio.release()
        super.onCleared()
    }
}
