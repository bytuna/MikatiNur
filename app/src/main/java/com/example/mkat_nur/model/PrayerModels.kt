package com.example.mkat_nur.model

import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.*

data class PrayerResponse(
    val code: Int,
    val status: String,
    val data: PrayerData
)

data class PrayerData(
    val timings: Timings,
    val date: DateInfo,
    val moonUrl: String = ""
)

data class Timings(
    @SerializedName("Fajr") val fajr: String,
    @SerializedName("Sunrise") val sunrise: String,
    @SerializedName("Dhuhr") val dhuhr: String,
    @SerializedName("Asr") val asr: String,
    @SerializedName("Maghrib") val maghrib: String,
    @SerializedName("Isha") val isha: String,
    val kible: String = ""
) {
    val sabah: String
        get() = try {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            val sunriseDate = sdf.parse(sunrise.substringBefore(" "))!!
            val cal = Calendar.getInstance().apply {
                time = sunriseDate
                add(Calendar.MINUTE, -60)
            }
            sdf.format(cal.time)
        } catch (e: Exception) {
            "00:00"
        }
}

data class DateInfo(
    val readable: String,
    val hijri: HijriInfo,
    val fullMiladi: String = "",
    val fullHicri: String = ""
)

data class HijriInfo(
    val day: String,
    val month: MonthInfo
)

data class MonthInfo(
    val en: String
)

data class Province(
    val name: String, 
    val id: String = "",
    val lat: Double = 41.0082, 
    val lon: Double = 28.9784
)

data class Sehir(
    @SerializedName("SehirAdi") val sehirAdi: String,
    @SerializedName("SehirID") val sehirId: String
)

data class Ilce(
    @SerializedName("IlceAdi") val ilceAdi: String,
    @SerializedName("IlceID") val ilceId: String
)

data class DailyContent(
    val verse: String = "",
    val verseSource: String = "",
    val hadith: String = "",
    val hadithSource: String = "",
    val quote: String = "",
    val quoteSource: String = "",
    val name: String = "",
    val nameMeaning: String = ""
)


