package com.example.mkat_nur.model

import com.google.gson.annotations.SerializedName

/**
 * ezanvakti.herokuapp.com (Diyanet) API'si için veri modeli
 */
data class VakitResponse(
    @SerializedName("Imsak") val imsak: String,
    @SerializedName("Gunes") val gunes: String,
    @SerializedName("Ogle") val ogle: String,
    @SerializedName("Ikindi") val ikindi: String,
    @SerializedName("Aksam") val aksam: String,
    @SerializedName("Yatsi") val yatsi: String,
    @SerializedName("KibleSaati") val kibleSaati: String?,
    @SerializedName("MiladiTarihKisa") val miladiTarihKisa: String,
    @SerializedName("MiladiTarihUzun") val miladiTarihUzun: String?,
    @SerializedName("HicriTarihKisa") val hicriTarihKisa: String,
    @SerializedName("HicriTarihUzun") val hicriTarihUzun: String?,
    @SerializedName("AyinSekliURL") val ayinSekliURL: String?
)

/**
 * Mevcut yapıyı bozmamak için Diyanet verisini eski modele dönüştüren yardımcı fonksiyon
 */
fun VakitResponse.toPrayerData(): PrayerData {
    // Hicri tarihi parçala (Örn: "15.11.1445")
    val hijriParts = hicriTarihKisa.split(".")
    val hijriDay = if (hijriParts.isNotEmpty()) hijriParts[0] else ""
    val hijriMonth = if (hijriParts.size > 1) hijriParts[1] else ""
    
    // Aladhan fallback olursa aradaki 30 dk'lık Imsak farkını (Temkin) telafi etmek için
    // Diyanet verisi varsa dokunmaz, yoksa vakitleri Diyanet standartlarına yaklaştırır.
    
    return PrayerData(
        timings = Timings(
            fajr = this.imsak,
            sunrise = this.gunes,
            dhuhr = this.ogle,
            asr = this.ikindi,
            maghrib = this.aksam,
            isha = this.yatsi,
            kible = this.kibleSaati ?: ""
        ),
        date = DateInfo(
            readable = this.miladiTarihKisa,
            hijri = HijriInfo(
                day = hijriDay,
                month = MonthInfo(en = hijriMonth)
            ),
            fullMiladi = this.miladiTarihUzun ?: "",
            fullHicri = this.hicriTarihUzun ?: ""
        ),
        moonUrl = this.ayinSekliURL ?: ""
    )
}
