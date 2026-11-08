package com.example.mkat_nur.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.mkat_nur.model.PrayerData
import com.example.mkat_nur.receiver.PrayerNotificationReceiver
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Cuma tebrik kartı: her Cuma, öğle vaktinden 45 dakika önce.
 *
 * Uygulama o gün açılmasa bile çalışsın diye her zaman SIRADAKİ Cuma için alarm kurar.
 * Alarm çaldıktan sonra, açılışta, telefon yeniden başlayınca ve günde iki kez arka planda
 * (FridayCheckWorker) yeniden kurulur.
 */
object FridayScheduler {
    private const val REQUEST_CODE = 9999
    private const val MINUTES_BEFORE_DHUHR = 45
    private const val TAG = "FridayScheduler"

    private fun pendingIntent(context: Context, flags: Int): PendingIntent? {
        val intent = Intent(context, PrayerNotificationReceiver::class.java).apply {
            putExtra("IS_FRIDAY_MESSAGE", true)
        }
        return PendingIntent.getBroadcast(context, REQUEST_CODE, intent, flags or PendingIntent.FLAG_IMMUTABLE)
    }

    fun scheduleNext(context: Context) {
        try {
            val app = context.applicationContext
            val alarmManager = app.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val prefs = app.getSharedPreferences("mkat_nur_prefs", Context.MODE_PRIVATE)
            val pi = pendingIntent(app, PendingIntent.FLAG_UPDATE_CURRENT) ?: return

            // Kadın özel modu açıksa bildirimler kapalıdır
            if (prefs.getBoolean("is_women_special", false)) {
                alarmManager.cancel(pi)
                return
            }

            val triggerAt = nextTriggerMillis(prefs) ?: return

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
            }
            Log.d(TAG, "Sıradaki Cuma kartı: ${java.util.Date(triggerAt)}")
        } catch (e: Exception) {
            Log.e(TAG, "Cuma alarmı kurulamadı: ${e.message}")
        }
    }

    private fun nextTriggerMillis(prefs: android.content.SharedPreferences): Long? {
        val cityId = prefs.getString("selected_city_id", "9541") ?: "9541"
        val json = prefs.getString("vakitler_cache_$cityId", null) ?: return null
        val list: List<PrayerData> = try {
            Gson().fromJson(json, object : TypeToken<List<PrayerData>>() {}.type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
        if (list.isEmpty()) return null

        val offset = prefs.getInt("time_offset", 0) + prefs.getInt("offset_dhuhr", 0)
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        val now = Calendar.getInstance()

        for (d in 0..7) {
            val day = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, d) }
            if (day.get(Calendar.DAY_OF_WEEK) != Calendar.FRIDAY) continue

            // O Cuma'nın verisi yoksa (veri penceresi dışı) en yakın bilinen öğle saati kullanılır
            val entry = list.find { it.date.readable == dateFormat.format(day.time) } ?: list.first()
            val parts = entry.timings.dhuhr.substringBefore(" ").split(":")
            val hour = parts.getOrNull(0)?.trim()?.toIntOrNull() ?: continue
            val minute = parts.getOrNull(1)?.trim()?.toIntOrNull() ?: continue

            day.set(Calendar.HOUR_OF_DAY, hour)
            day.set(Calendar.MINUTE, minute)
            day.set(Calendar.SECOND, 0)
            day.set(Calendar.MILLISECOND, 0)
            day.add(Calendar.MINUTE, offset - MINUTES_BEFORE_DHUHR)

            if (day.after(now)) return day.timeInMillis
        }
        return null
    }
}
