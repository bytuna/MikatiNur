package com.example.mkat_nur.ui.quran1

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mkat_nur.model.SurahResponse
import com.example.mkat_nur.util.QuranTransliterationHelper
import com.example.mkat_nur.util.QuranUtils
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

sealed class Quran1UiState {
    /** Liste (sure/cüz/sayfa/hatim sekmeleri) görünüyor, açık okuma yok. */
    object ListView : Quran1UiState()
    object Loading : Quran1UiState()
    data class Success(val surah: Quran1Surah, val fromCache: Boolean, val startIndex: Int = 0) : Quran1UiState()
    data class Error(val message: String) : Quran1UiState()
}

/** Okunacak bölüm: bir sure, bir cüz ya da bir mushaf sayfası. */
sealed class Quran1Target {
    data class Surah(val number: Int) : Quran1Target()
    data class Juz(val number: Int) : Quran1Target()
    data class Page(val number: Int) : Quran1Target()

    val cacheName: String
        get() = when (this) {
            is Surah -> "surah_$number"
            is Juz -> "juz_$number"
            is Page -> "page_$number"
        }
}

class Quran1ViewModel(application: Application) : AndroidViewModel(application) {

    private val gson = Gson()
    private val api = AlQuranCloudApi.instance
    val audio = QuranAudioPlayerManager(application)

    private val _surahList = MutableStateFlow<kotlin.collections.List<Quran1SurahInfo>>(emptyList())
    val surahList: StateFlow<kotlin.collections.List<Quran1SurahInfo>> = _surahList.asStateFlow()

    private val _uiState = MutableStateFlow<Quran1UiState>(Quran1UiState.ListView)
    val uiState: StateFlow<Quran1UiState> = _uiState.asStateFlow()

    private val _openTarget = MutableStateFlow<Quran1Target?>(null)
    val openTarget: StateFlow<Quran1Target?> = _openTarget.asStateFlow()

    /** Çalan ayetin okunan bölüm içindeki sırası (0'dan). Oynatıcıdan gelir. */
    val currentPlayingAyahIndex: StateFlow<Int> = audio.currentIndex
    val isPlaying: StateFlow<Boolean> = audio.isPlaying
    val isBuffering: StateFlow<Boolean> = audio.isBuffering
    val audioError: StateFlow<String?> = audio.error

    private var appliedReciterId: String

    init {
        Quran1Settings.init(application)
        Quran1HatimStore.init(application)
        appliedReciterId = Quran1Settings.reciterId.value
        audio.setSpeed(Quran1Settings.playbackSpeed.value)
        loadSurahList()

        // Hız değişince oynatıcıya uygula
        viewModelScope.launch {
            Quran1Settings.playbackSpeed.collect { audio.setSpeed(it) }
        }
        // Hafız değişince sesi yeni hafızla yeniden kur (çalıyorsa aynı ayetten sürdür)
        viewModelScope.launch {
            Quran1Settings.reciterId.collect { id ->
                if (id != appliedReciterId) {
                    appliedReciterId = id
                    rebuildAudio(resume = true)
                }
            }
        }
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

    // ------------------------------------------------------------------ Açma / kapama

    fun openSurah(number: Int) {
        resumeNext = true
        open(Quran1Target.Surah(number))
    }

    private var resumeNext = true

    /** Okunan/dinlenen ayeti hatırlar; bölüm yeniden açılınca oradan devam edilir. */
    fun savePosition(globalAyah: Int) {
        val t = _openTarget.value ?: return
        Quran1HatimStore.savePosition(t.cacheName, globalAyah)
    }

    /** resume=false: kayıtlı ayet yerine bölümün başından başla (önceki/sonraki gezinmesi). */
    fun openJuz(number: Int, resume: Boolean = true) {
        val n = number.coerceIn(1, 30)
        resumeNext = resume
        Quran1HatimStore.saveLastJuz(n)
        Quran1HatimStore.saveLastPage(QuranUtils.getJuzPageRange(n).first)
        open(Quran1Target.Juz(n))
    }

    fun openPage(number: Int, resume: Boolean = true) {
        val n = number.coerceIn(1, 604)
        resumeNext = resume
        Quran1HatimStore.saveLastPage(n)
        open(Quran1Target.Page(n))
    }

    private fun open(target: Quran1Target) {
        _openTarget.value = target
        loadTarget(target)
    }

    fun closeReader() {
        audio.stop()
        _openTarget.value = null
        _uiState.value = Quran1UiState.ListView
    }

    fun retry() {
        _openTarget.value?.let { loadTarget(it) }
    }

    // ------------------------------------------------------------------ Veri

    private fun cacheFile(name: String): File {
        val dir = File(getApplication<Application>().filesDir, "quran1_cache")
        if (!dir.exists()) dir.mkdirs()
        return File(dir, "$name.json")
    }

    /** Ağdan al; başarısızsa son indirilen kopyayı kullan. Dönen Boolean: çevrimdışı kopya mı. */
    private suspend fun fetchCached(
        name: String,
        fetch: suspend () -> kotlin.collections.List<AlQuranSurahEdition>?
    ): Pair<kotlin.collections.List<AlQuranSurahEdition>, Boolean>? {
        try {
            val data = fetch()
            if (!data.isNullOrEmpty()) {
                try { cacheFile(name).writeText(gson.toJson(data)) } catch (_: Exception) { }
                return data to false
            }
        } catch (e: Exception) {
            Log.e("Quran1", "$name alınamadı: ${e.message}")
        }
        return try {
            val f = cacheFile(name)
            if (f.exists()) gson.fromJson(f.readText(), Array<AlQuranSurahEdition>::class.java).toList() to true else null
        } catch (_: Exception) { null }
    }

    private suspend fun call(
        full: String,
        light: String,
        request: suspend (String) -> AlQuranMultiResponse
    ): kotlin.collections.List<AlQuranSurahEdition>? {
        // Okunuş edisyonu bulunamazsa onsuz yeniden dene (okunuş uygulama içi kaynaktan tamamlanır)
        val response = try { request(full) } catch (first: Exception) {
            Log.w("Quran1", "Tam istek başarısız (${first.message}), okunuşsuz deneniyor")
            request(light)
        }
        val data = response.data
        return if (response.code == 200 && !data.isNullOrEmpty()) data else null
    }

    private suspend fun fetchSurahEditions(number: Int) =
        fetchCached("surah_$number") {
            call(AlQuranCloudApi.ALL_EDITIONS, AlQuranCloudApi.EDITIONS_WITHOUT_TRANSLITERATION) {
                api.getSurahEditions(number, it)
            }
        }

    /** Bir cüz/sayfanın kapsadığı mushaf sayfa aralığı. */
    private fun pageRange(target: Quran1Target): IntRange = when (target) {
        is Quran1Target.Juz -> QuranUtils.getJuzPageRange(target.number).let { it.first..it.second }
        is Quran1Target.Page -> target.number..target.number
        is Quran1Target.Surah -> 1..604
    }

    /** Cüz/sayfa isteği başarısız olursa ilgili sureler tek tek alınıp birleştirilir. */
    private suspend fun assembleFromSurahs(target: Quran1Target): kotlin.collections.List<AlQuranSurahEdition>? = coroutineScope {
        val range = pageRange(target)
        val surahNumbers = (1..114).filter { s ->
            val start = QuranUtils.getStartPageForSurah(s)
            val nextStart = if (s == 114) 604 else QuranUtils.getStartPageForSurah(s + 1)
            start <= range.last && nextStart >= range.first
        }
        val parts = surahNumbers.map { n -> async { n to fetchSurahEditions(n)?.first } }.awaitAll()
        if (parts.any { it.second == null }) return@coroutineScope null

        fun matches(a: AlQuranAyah) = when (target) {
            is Quran1Target.Juz -> a.juz == target.number
            is Quran1Target.Page -> a.page == target.number
            is Quran1Target.Surah -> true
        }

        val wanted = HashSet<Int>()
        for ((_, eds) in parts) {
            val uth = eds!!.firstOrNull { it.edition?.identifier == AlQuranCloudApi.EDITION_UTHMANI } ?: eds.first()
            uth.ayahs.orEmpty().filter { matches(it) }.forEach { wanted.add(it.number) }
        }
        if (wanted.isEmpty()) return@coroutineScope null

        val protos = linkedMapOf<String, AlQuranSurahEdition>()
        val ayahsById = linkedMapOf<String, MutableList<AlQuranAyah>>()
        for ((n, eds) in parts) {
            for (ed in eds!!) {
                val id = ed.edition?.identifier ?: continue
                protos.getOrPut(id) { ed }
                val list = ayahsById.getOrPut(id) { mutableListOf() }
                ed.ayahs.orEmpty().filter { it.number in wanted }.forEach {
                    list.add(it.copy(surah = AlQuranSurahRef(n, ed.name, ed.englishName)))
                }
            }
        }
        protos.map { (id, ed) -> ed.copy(ayahs = ayahsById[id]) }
    }

    private fun loadTarget(target: Quran1Target) {
        viewModelScope.launch {
            audio.stop()
            _uiState.value = Quran1UiState.Loading

            val result = withContext(Dispatchers.IO) {
                when (target) {
                    is Quran1Target.Surah -> fetchSurahEditions(target.number)
                    is Quran1Target.Juz -> fetchCached(target.cacheName) {
                        val direct = try {
                            call(AlQuranCloudApi.SECTION_EDITIONS, AlQuranCloudApi.SECTION_EDITIONS_WITHOUT_TRANSLITERATION) {
                                api.getJuzEditions(target.number, it)
                            }
                        } catch (e: Exception) { null }
                        direct ?: assembleFromSurahs(target)
                    }
                    is Quran1Target.Page -> fetchCached(target.cacheName) {
                        val direct = try {
                            call(AlQuranCloudApi.SECTION_EDITIONS, AlQuranCloudApi.SECTION_EDITIONS_WITHOUT_TRANSLITERATION) {
                                api.getPageEditions(target.number, it)
                            }
                        } catch (e: Exception) { null }
                        direct ?: assembleFromSurahs(target)
                    }
                }
            }

            if (result == null) {
                _uiState.value = Quran1UiState.Error("Yüklenemedi. İnternet bağlantınızı kontrol edip tekrar deneyin.")
                return@launch
            }

            val surah = build(target, result.first)
            if (surah == null || surah.ayahs.isEmpty()) {
                _uiState.value = Quran1UiState.Error("Veri okunamadı.")
                return@launch
            }

            val saved = if (resumeNext) Quran1HatimStore.position(target.cacheName) else null
            if (!resumeNext) Quran1HatimStore.clearPosition(target.cacheName)
            val start = saved?.let { g -> surah.ayahs.indexOfFirst { it.globalNumber == g } }?.coerceAtLeast(0) ?: 0
            _uiState.value = Quran1UiState.Success(surah, result.second, start)
            applyAudio(surah, target)
        }
    }

    private fun build(target: Quran1Target, editions: kotlin.collections.List<AlQuranSurahEdition>): Quran1Surah? {
        fun find(id: String) = editions.firstOrNull { it.edition?.identifier == id }

        val uthmani = find(AlQuranCloudApi.EDITION_UTHMANI) ?: editions.firstOrNull() ?: return null
        val tajweedMap = find(AlQuranCloudApi.EDITION_TAJWEED)?.ayahs?.associateBy { it.number } ?: emptyMap()
        val translitMap = find(AlQuranCloudApi.EDITION_TRANSLITERATION)?.ayahs?.associateBy { it.number } ?: emptyMap()
        val mealMap = find(AlQuranCloudApi.EDITION_MEAL)?.ayahs?.associateBy { it.number } ?: emptyMap()
        val audioMap = find(AlQuranCloudApi.EDITION_AUDIO)?.ayahs?.associateBy { it.number } ?: emptyMap()
        val surahs = _surahList.value

        val ayahs = (uthmani.ayahs ?: return null).map { a ->
            val surahNo = when (target) {
                is Quran1Target.Surah -> target.number
                else -> a.surah?.number ?: 0
            }
            val arabic = a.text.orEmpty()
            Quran1Ayah(
                numberInSurah = a.numberInSurah,
                globalNumber = a.number,
                page = a.page,
                juz = a.juz,
                uthmani = arabic,
                tajweed = tajweedMap[a.number]?.text,
                transliteration = translitMap[a.number]?.text?.takeIf { it.isNotBlank() }
                    ?: QuranTransliterationHelper.getTransliteration("$surahNo:${a.numberInSurah}", arabic),
                meal = mealMap[a.number]?.text,
                audioUrl = audioMap[a.number]?.let { it.audio ?: it.audioSecondary?.firstOrNull() },
                surahNumber = surahNo,
                surahTurkishName = surahs.firstOrNull { it.number == surahNo }?.turkishName
                    ?: a.surah?.englishName.orEmpty()
            )
        }

        return when (target) {
            is Quran1Target.Surah -> {
                val info = surahs.firstOrNull { it.number == target.number }
                Quran1Surah(
                    number = target.number,
                    arabicName = info?.arabicName ?: uthmani.name.orEmpty(),
                    turkishName = info?.turkishName ?: uthmani.englishName.orEmpty(),
                    ayahs = ayahs
                )
            }
            is Quran1Target.Juz -> {
                val r = QuranUtils.getJuzPageRange(target.number)
                Quran1Surah(0, "", "", ayahs, isSection = true,
                    title = "${target.number}. Cüz", subtitle = "Sayfa ${r.first} – ${r.second}")
            }
            is Quran1Target.Page -> Quran1Surah(0, "", "", ayahs, isSection = true,
                title = "${target.number}. Sayfa", subtitle = "${QuranUtils.getJuzByPage(target.number)}. Cüz")
        }
    }

    // ------------------------------------------------------------------ Ses

    private fun audioKey(target: Quran1Target) = "${target.cacheName}_$appliedReciterId"

    private fun applyAudio(surah: Quran1Surah, target: Quran1Target) {
        val reciter = Quran1Reciters.byId(appliedReciterId)
        val urls = surah.ayahs.map { Quran1Reciters.audioUrl(reciter, it) }
        if (urls.isNotEmpty() && urls.all { !it.isNullOrBlank() }) {
            audio.load(audioKey(target), urls.map { it!! })
        }
    }

    private fun rebuildAudio(resume: Boolean) {
        val st = _uiState.value as? Quran1UiState.Success ?: return
        val target = _openTarget.value ?: return
        val index = audio.currentIndex.value
        val wasPlaying = audio.isPlaying.value
        applyAudio(st.surah, target)
        if (resume && wasPlaying && index >= 0) audio.playFrom(index)
    }

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
