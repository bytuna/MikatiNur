package com.example.mkat_nur.model

import com.google.gson.annotations.SerializedName

data class TajweedAnnotation(
    @SerializedName("rule") val rule: String,
    @SerializedName("start") val start: Int,
    @SerializedName("end") val end: Int
)

data class AyahTajweedData(
    @SerializedName("surah") val surah: Int,
    @SerializedName("ayah") val ayah: Int,
    @SerializedName("annotations") val annotations: List<TajweedAnnotation>
)
