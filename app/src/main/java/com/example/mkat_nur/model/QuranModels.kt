package com.example.mkat_nur.model

import com.google.gson.annotations.SerializedName

data class Surah(
    @SerializedName("id", alternate = ["SureId"]) val id: Int,
    @SerializedName("name", alternate = ["SureNameTurkish"]) val name: String,
    @SerializedName("name_arabic", alternate = ["SureNameArabic"]) val arabicName: String?,
    @SerializedName("verse_count", alternate = ["AyetCount"]) val verseCount: Int,
    @SerializedName("revelation_place", alternate = ["Yer"]) val revelationPlace: String?
)

data class Translation(
    @SerializedName("text", alternate = ["translation"]) val text: String
)

data class Verse(
    @SerializedName("id") val id: Int,
    @SerializedName("surah_id", alternate = ["SureId"]) val surahId: Int,
    @SerializedName("verse_no", alternate = ["AyetNo"]) val verseNumber: Int,
    @SerializedName("content", alternate = ["arabic_script"]) val content: String,
    @SerializedName("translations") val translations: List<Translation>?,
    @SerializedName("page_number", alternate = ["sayfa_no"]) val pageNumber: Int?
)

data class SurahDetail(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("verses") val verses: List<Verse>
)

data class SurahListResponse(
    @SerializedName("data") val data: List<Surah>
)

data class SurahDetailResponse(
    @SerializedName("data") val data: SurahDetail
)
