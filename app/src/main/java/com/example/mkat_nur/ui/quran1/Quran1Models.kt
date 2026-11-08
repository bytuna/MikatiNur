package com.example.mkat_nur.ui.quran1

import com.google.gson.annotations.SerializedName

// ---------------------------------------------------------------- AlQuran Cloud yanıt modelleri

data class AlQuranEditionInfo(
    @SerializedName("identifier") val identifier: String? = null,
    @SerializedName("language") val language: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("englishName") val englishName: String? = null,
    @SerializedName("format") val format: String? = null,
    @SerializedName("type") val type: String? = null
)

data class AlQuranSurahRef(
    @SerializedName("number") val number: Int = 0,
    @SerializedName("name") val name: String? = null,
    @SerializedName("englishName") val englishName: String? = null
)

data class AlQuranAyah(
    @SerializedName("number") val number: Int = 0,
    @SerializedName("text") val text: String? = null,
    @SerializedName("numberInSurah") val numberInSurah: Int = 0,
    @SerializedName("juz") val juz: Int? = null,
    @SerializedName("page") val page: Int? = null,
    @SerializedName("audio") val audio: String? = null,
    @SerializedName("audioSecondary") val audioSecondary: List<String>? = null,
    /** Cüz/sayfa isteklerinde ayetin hangi sureye ait olduğunu bildirir. */
    @SerializedName("surah") val surah: AlQuranSurahRef? = null
)

data class AlQuranSurahEdition(
    @SerializedName("number") val number: Int = 0,
    @SerializedName("name") val name: String? = null,
    @SerializedName("englishName") val englishName: String? = null,
    @SerializedName("numberOfAyahs") val numberOfAyahs: Int = 0,
    @SerializedName("revelationType") val revelationType: String? = null,
    @SerializedName("edition") val edition: AlQuranEditionInfo? = null,
    @SerializedName("ayahs") val ayahs: List<AlQuranAyah>? = null
)

data class AlQuranMultiResponse(
    @SerializedName("code") val code: Int = 0,
    @SerializedName("status") val status: String? = null,
    @SerializedName("data") val data: List<AlQuranSurahEdition>? = null
)

// ---------------------------------------------------------------- Uygulama içi birleşik modeller

/** Bir ayetin tüm edisyonlardaki karşılıkları. */
data class Quran1Ayah(
    val numberInSurah: Int,
    val globalNumber: Int,
    val page: Int?,
    val juz: Int?,
    val uthmani: String,
    val tajweed: String?,
    val transliteration: String?,
    val meal: String?,
    val audioUrl: String?,
    /** Cüz/sayfa okumasında ayetin sure bilgisi (sure okumasında da doldurulur). */
    val surahNumber: Int = 0,
    val surahTurkishName: String = ""
)

data class Quran1Surah(
    val number: Int,
    val arabicName: String,
    val turkishName: String,
    val ayahs: List<Quran1Ayah>,
    /** true ise bu bir cüz/sayfa okumasıdır; birden çok sureden ayet içerir. */
    val isSection: Boolean = false,
    val title: String = "",
    val subtitle: String = ""
)

/** Sure listesi satırı (assets/quran_surahs.json'dan, internetsiz gösterilir). */
data class Quran1SurahInfo(
    val number: Int,
    val arabicName: String,
    val turkishName: String,
    val verseCount: Int
)

/** Yazı tipi seçeneği; resName res/font içindeki dosya adıdır (uzantısız). */
data class Quran1FontOption(val key: String, val label: String, val resName: String)

object Quran1Fonts {
    val options = listOf(
        Quran1FontOption("uthman_taha", "KFGQPC Uthman Taha Naskh", "uthman_taha"),
        Quran1FontOption("scheherazade", "Scheherazade New", "scheherazade_new_regular"),
        Quran1FontOption("me_quran", "Me Quran", "me_quran"),
        // Dosya res/font/uthmanic_hafs.ttf olarak eklenirse otomatik görünür
        Quran1FontOption("uthmanic_hafs", "KFGQPC Uthmanic Script HAFS", "uthmanic_hafs")
    )
}
