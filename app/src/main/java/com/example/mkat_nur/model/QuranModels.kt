package com.example.mkat_nur.model

import com.google.gson.annotations.SerializedName

data class Surah(
    @SerializedName("id") val id: Int,
    @SerializedName("name_arabic") val arabicName: String?,
    @SerializedName("verses_count") val verseCount: Int,
    @SerializedName("revelation_order") val revelationOrder: Int?,
    @SerializedName("translated_name") val translatedName: TranslatedName?
) {
    val name: String? get() = translatedName?.name
}

data class TranslatedName(
    @SerializedName("name") val name: String?
)

data class SurahResponse(
    @SerializedName("chapters") val data: List<Surah>
)

data class VerseResponse(
    @SerializedName("verses") val data: List<Verse>
)

data class SingleVerseResponse(
    @SerializedName("verse") val data: Verse
)

data class Translation(
    @SerializedName("text") val text: String?
)

data class Verse(
    @SerializedName("id") val id: Int,
    @SerializedName("verse_number") val verseNumber: Int,
    @SerializedName("verse_key") val verseKey: String?,
    @SerializedName("text_uthmani") val arabicText: String?,
    @SerializedName("translations") val translations: List<Translation>?,
    @SerializedName("page_number") val pageNumber: Int?,
    @SerializedName("juz_number") val juzNumber: Int?,
    val customTransliteration: String? = null
) {
    val translation: String? get() = translations?.getOrNull(0)?.text?.replace(Regex("<[^>]*>"), "")
    val transliterationApi: String? get() = translations?.getOrNull(1)?.text?.replace(Regex("<[^>]*>"), "")
    val surahId: Int get() = verseKey?.split(":")?.firstOrNull()?.toIntOrNull() ?: 0
}

enum class QuranViewDisplayMode {
    METIN_GORUNUMU, // Metin Görünümü (Ayet ayet, altında Türkçe okunuş ve meal)
    KURAN_SAYFASI   // Mushaf / Kuran Sayfası Görünümü
}

data class MealSource(
    val id: String,
    val title: String,
    val isSelected: Boolean = false,
    val isDownloaded: Boolean = true
)

data class TajweedRule(
    val id: String,
    val name: String,
    val description: String,
    val arabicExample: String,
    val isEnabled: Boolean = true,
    val colorHex: String = "#FF9800"
)
