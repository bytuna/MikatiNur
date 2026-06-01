package com.example.mkat_nur.data.local.model

import com.google.gson.annotations.SerializedName

data class RisaleJsonModel(
    @SerializedName("kitap_adi") val kitapAdi: String,
    @SerializedName("sayfalar") val sayfalar: List<PageJsonModel>
)

data class PageJsonModel(
    @SerializedName("sayfa_no") val sayfaNo: Int,
    @SerializedName("bolum_adi") val bolumAdi: String,
    @SerializedName("sayfa_icerigi") val sayfaIcerigi: String,
    @SerializedName("sayfa_hasiyeleri") val sayfaHasiyeleri: Map<String, String>
)
