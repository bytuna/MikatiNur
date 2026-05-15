package com.example.mkat_nur.util

import android.content.Context
import com.example.mkat_nur.model.PrayerData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

class PrayerManager(private val context: Context) {
    private val prefs = context.getSharedPreferences("mkat_nur_prefs", Context.MODE_PRIVATE)

    fun getTodayPrayerData(): PrayerData? {
        val cityId = prefs.getString("selected_city_id", "9541") ?: "9541"
        val json = prefs.getString("vakitler_cache_$cityId", null) ?: return null
        
        return try {
            val type = object : TypeToken<List<PrayerData>>() {}.type
            val list: List<PrayerData> = Gson().fromJson(json, type)
            val todayStr = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date())
            list.find { it.date.readable == todayStr } ?: list.getOrNull(0)
        } catch (e: Exception) {
            null
        }
    }

    fun getCityName(): String {
        return prefs.getString("selected_city_name", "İstanbul") ?: "İstanbul"
    }

    fun getWidgetTransparency(): Float {
        return prefs.getFloat("widget_transparency", 0.9f)
    }

    fun getWidgetTitleColor(): Int {
        return prefs.getInt("widget_title_color", 0xFFFFD700.toInt())
    }

    fun getWidgetTextColor(): Int {
        return prefs.getInt("widget_text_color", 0xFFFFFFFF.toInt())
    }

    fun isKerahat(data: PrayerData): Boolean {
        val now = Calendar.getInstance()
        try {
            val sunriseCal = getCalFromTime(data.timings.sunrise.substringBefore(" "))
            val dhuhrCal = getCalFromTime(data.timings.dhuhr.substringBefore(" "))
            val maghribCal = getCalFromTime(data.timings.maghrib.substringBefore(" "))

            // Sunrise + 45 min
            val sunriseEnd = (sunriseCal.clone() as Calendar).apply { add(Calendar.MINUTE, 45) }
            if (now.after(sunriseCal) && now.before(sunriseEnd)) return true

            // Dhuhr - 45 min
            val dhuhrStart = (dhuhrCal.clone() as Calendar).apply { add(Calendar.MINUTE, -45) }
            if (now.after(dhuhrStart) && now.before(dhuhrCal)) return true

            // Maghrib - 45 min
            val maghribStart = (maghribCal.clone() as Calendar).apply { add(Calendar.MINUTE, -45) }
            if (now.after(maghribStart) && now.before(maghribCal)) return true

        } catch (e: Exception) {
            return false
        }
        return false
    }

    fun getCurrentVakit(data: PrayerData): String {
        val now = Calendar.getInstance()
        try {
            val imsak = getCalFromTime(data.timings.fajr.substringBefore(" "))
            val gunes = getCalFromTime(data.timings.sunrise.substringBefore(" "))
            val ogle = getCalFromTime(data.timings.dhuhr.substringBefore(" "))
            val ikindi = getCalFromTime(data.timings.asr.substringBefore(" "))
            val aksam = getCalFromTime(data.timings.maghrib.substringBefore(" "))
            val yatsi = getCalFromTime(data.timings.isha.substringBefore(" "))

            return when {
                now.after(yatsi) || now.before(imsak) -> "yatsi"
                now.after(aksam) -> "aksam"
                now.after(ikindi) -> "ikindi"
                now.after(ogle) -> "ogle"
                now.after(gunes) -> "gunes"
                else -> "imsak"
            }
        } catch (e: Exception) {
            return ""
        }
    }

    fun getNextVakitInfo(data: PrayerData): Pair<String, Long>? {
        val now = Calendar.getInstance()
        try {
            val imsak = getCalFromTime(data.timings.fajr.substringBefore(" "))
            val gunes = getCalFromTime(data.timings.sunrise.substringBefore(" "))
            val ogle = getCalFromTime(data.timings.dhuhr.substringBefore(" "))
            val ikindi = getCalFromTime(data.timings.asr.substringBefore(" "))
            val aksam = getCalFromTime(data.timings.maghrib.substringBefore(" "))
            val yatsi = getCalFromTime(data.timings.isha.substringBefore(" "))

            val result = when {
                now.before(imsak) -> "İmsak" to imsak
                now.before(gunes) -> "Güneş" to gunes
                now.before(ogle) -> "Öğle" to ogle
                now.before(ikindi) -> "İkindi" to ikindi
                now.before(aksam) -> "Akşam" to aksam
                now.before(yatsi) -> "Yatsı" to yatsi
                else -> {
                    imsak.add(Calendar.DAY_OF_YEAR, 1)
                    "İmsak" to imsak
                }
            }
            return result.first to result.second.timeInMillis
        } catch (e: Exception) {
            return null
        }
    }

    private fun getCalFromTime(timeStr: String): Calendar {
        val format = SimpleDateFormat("HH:mm", Locale.getDefault())
        val date = format.parse(timeStr)!!
        return Calendar.getInstance().apply {
            val temp = Calendar.getInstance().apply { time = date }
            set(Calendar.HOUR_OF_DAY, temp.get(Calendar.HOUR_OF_DAY))
            set(Calendar.MINUTE, temp.get(Calendar.MINUTE))
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }
}
