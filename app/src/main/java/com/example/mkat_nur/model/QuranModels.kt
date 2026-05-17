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
    @SerializedName("juz_number") val juzNumber: Int?
) {
    val translation: String? get() = translations?.firstOrNull()?.text?.replace(Regex("<[^>]*>"), "")
    val surahId: Int get() = verseKey?.split(":")?.firstOrNull()?.toIntOrNull() ?: 0
}
