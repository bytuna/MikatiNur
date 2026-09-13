package com.example.mkat_nur.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.example.mkat_nur.MainActivity
import com.example.mkat_nur.R
import com.example.mkat_nur.model.PrayerData
import com.example.mkat_nur.viewmodel.CountdownState
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.*

class PrayerNotificationService : Service() {

    companion object {
        private const val CHANNEL_ID = "prayer_times_channel"
        private const val NOTIFICATION_ID = 1001

        private var currentPrayerData: PrayerData? = null
        private var currentProvinceName: String = ""

        fun startService(context: Context, provinceName: String, data: PrayerData) {
            currentProvinceName = provinceName
            currentPrayerData = data

            // Save data to SharedPreferences for survival after process kill or reboot
            try {
                val prefs = context.getSharedPreferences("mkat_nur_prefs", Context.MODE_PRIVATE)
                prefs.edit()
                    .putString("notif_cached_data", Gson().toJson(data))
                    .putString("notif_cached_province", provinceName)
                    .apply()
            } catch (e: Exception) {
                Log.e("PrayerService", "Error caching notification data: ${e.message}")
            }

            val intent = Intent(context, PrayerNotificationService::class.java)
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                Log.e("PrayerService", "Service start failed: ${e.message}")
            }
        }

        fun getTurkishHijri(monthEn: String): String {
            val input = monthEn.trim()
            return when {
                input.contains("Muharram", true) || input == "1" -> "Muharrem"
                input.contains("Safar", true) || input == "2" -> "Safer"
                input.contains("Rabi", true) && input.contains("awwal", true) || input == "3" -> "Rebiülevvel"
                input.contains("Rabi", true) && (input.contains("thani", true) || input.contains("akhir", true)) || input == "4" -> "Rebiülahir"
                input.contains("Jumada", true) && (input.contains("ula", true) || input.contains("1", true)) || input == "5" -> "Cemaziyelevvel"
                input.contains("Jumada", true) && (input.contains("akhira", true) || input.contains("2", true)) || input == "6" -> "Cemaziyelahir"
                input.contains("Rajab", true) || input == "7" -> "Recep"
                input.contains("Sha", true) && input.contains("ban", true) || input == "8" -> "Şaban"
                input.contains("Ramadan", true) || input == "9" -> "Ramazan"
                input.contains("Shawwal", true) || input == "10" -> "Şevval"
                input.contains("Qi", true) || input.contains("Qa", true) || input == "11" -> "Zilkade"
                input.contains("Hijjah", true) || input == "12" -> "Zilhicce"
                else -> input
            }
        }
    }

    private var timer: Timer? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        restoreCachedDataIfNeeded()
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        restoreCachedDataIfNeeded()
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(NOTIFICATION_ID, createPlaceholderNotification(), android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
            } else {
                startForeground(NOTIFICATION_ID, createPlaceholderNotification())
            }
        } catch (e: Throwable) {
            Log.e("PrayerService", "startForeground error: ${e.message}")
        }
        startTimer()
    }

    private fun restoreCachedDataIfNeeded() {
        if (currentPrayerData == null) {
            try {
                val prefs = getSharedPreferences("mkat_nur_prefs", Context.MODE_PRIVATE)
                val json = prefs.getString("notif_cached_data", null)
                val province = prefs.getString("notif_cached_province", "") ?: ""
                if (!json.isNullOrEmpty()) {
                    val data = Gson().fromJson(json, PrayerData::class.java)
                    if (data != null) {
                        currentPrayerData = data
                        currentProvinceName = province
                    }
                }
            } catch (e: Exception) {
                Log.e("PrayerService", "Error restoring cached data: ${e.message}")
            }
        }
    }

    private fun startTimer() {
        timer?.cancel()
        timer = Timer()
        timer?.schedule(object : TimerTask() {
            override fun run() {
                try {
                    updateNotification()
                } catch (e: Throwable) {
                    Log.e("PrayerService", "Error in timer execution: ${e.message}")
                }
            }
        }, 0, 1000)
    }

    private fun createPlaceholderNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_mosque)
            .setContentTitle("Mîkat-ı Nur")
            .setContentText("Vakitler takip ediliyor...")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setSilent(true)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification() {
        restoreCachedDataIfNeeded()
        val data = currentPrayerData ?: return
        val province = currentProvinceName
        
        // Vakitleri hesapla
        val countdown = calculateCountdown(data)

        try {
            val remoteViews = RemoteViews(applicationContext.packageName, R.layout.notification_prayer)
            remoteViews.setTextViewText(R.id.notif_city, province)
            
            val prefs = getSharedPreferences("mkat_nur_prefs", Context.MODE_PRIVATE)
            val activeColor = prefs.getInt("highlight_color", 0xFF2196F3.toInt())
            val defaultColor = android.graphics.Color.BLACK

            countdown?.let {
                val isRamadan = data.date.hijri.month.en.contains("Ramadan", true)
                val nextP = when(it.nextPrayer) {
                    "Sabah" -> "Sabah'a"
                    "İmsak" -> "İmsak'a"
                    "Akşam" -> if (isRamadan) "İftar'a" else "Akşam'a"
                    "Güneş" -> "Güneş'e"
                    "Öğle" -> "Öğle'ye"
                    "İkindi" -> "İkindi'ye"
                    "Yatsı" -> "Yatsı'ya"
                    else -> "${it.nextPrayer}'e"
                }
                val countdownStr = String.format(Locale.getDefault(), "%s %02d:%02d:%02d", nextP, it.hours, it.minutes, it.seconds)
                remoteViews.setTextViewText(R.id.notif_current_prayer_time, countdownStr)
                remoteViews.setTextColor(R.id.notif_current_prayer_time, activeColor)

                if (it.isKerahat) {
                    remoteViews.setViewVisibility(R.id.notif_kerahat_warning, android.view.View.VISIBLE)
                } else {
                    remoteViews.setViewVisibility(R.id.notif_kerahat_warning, android.view.View.GONE)
                }
            }

            // Vakitleri doldur
            remoteViews.setTextViewText(R.id.time_imsak, data.timings.fajr.substringBefore(" "))
            remoteViews.setTextViewText(R.id.time_sabah, data.timings.sabah.substringBefore(" "))
            remoteViews.setTextViewText(R.id.time_sunrise, data.timings.sunrise.substringBefore(" "))
            remoteViews.setTextViewText(R.id.time_dhuhr, data.timings.dhuhr.substringBefore(" "))
            remoteViews.setTextViewText(R.id.time_asr, data.timings.asr.substringBefore(" "))
            remoteViews.setTextViewText(R.id.time_maghrib, data.timings.maghrib.substringBefore(" "))
            remoteViews.setTextViewText(R.id.time_isha, data.timings.isha.substringBefore(" "))

            val prayerNames = listOf("İmsak", "Sabah", "Güneş", "Öğle", "İkindi", "Akşam", "Yatsı")
            val timeIds = listOf(R.id.time_imsak, R.id.time_sabah, R.id.time_sunrise, R.id.time_dhuhr, R.id.time_asr, R.id.time_maghrib, R.id.time_isha)
            val labelIds = listOf(R.id.label_imsak, R.id.label_sabah, R.id.label_sunrise, R.id.label_dhuhr, R.id.label_asr, R.id.label_maghrib, R.id.label_isha)

            for (i in prayerNames.indices) {
                val color = if (prayerNames[i] == countdown?.currentPrayer) activeColor else defaultColor
                remoteViews.setTextColor(timeIds[i], color)
                remoteViews.setTextColor(labelIds[i], color)
            }

            val notificationIntent = Intent(this, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                this, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_mosque)
                .setCustomContentView(remoteViews)
                .setCustomBigContentView(remoteViews)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .setSilent(true)
                .build()

            val manager = getSystemService(NotificationManager::class.java)
            manager.notify(NOTIFICATION_ID, notification)
        } catch (e: Throwable) {
            Log.e("PrayerService", "Notification update failed: ${e.message}")
        }
    }

    private fun calculateCountdown(data: PrayerData): CountdownState? {
        try {
            val now = Calendar.getInstance()
            val format = SimpleDateFormat("HH:mm", Locale.getDefault())

            val timings = mapOf(
                "İmsak" to data.timings.fajr,
                "Sabah" to data.timings.sabah,
                "Güneş" to data.timings.sunrise,
                "Öğle" to data.timings.dhuhr,
                "İkindi" to data.timings.asr,
                "Akşam" to data.timings.maghrib,
                "Yatsı" to data.timings.isha
            )

            val sortedTimes = timings.mapNotNull { entry ->
                try {
                    val timeStr = entry.value.substringBefore(" ").trim()
                    val dateParsed = format.parse(timeStr) ?: return@mapNotNull null
                    val cal = Calendar.getInstance().apply {
                        val temp = Calendar.getInstance().apply { time = dateParsed }
                        set(Calendar.HOUR_OF_DAY, temp.get(Calendar.HOUR_OF_DAY))
                        set(Calendar.MINUTE, temp.get(Calendar.MINUTE))
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    entry.key to cal
                } catch (e: Exception) {
                    null
                }
            }.sortedBy { it.second.timeInMillis }

            if (sortedTimes.isEmpty()) return null

            var nextN = "İmsak"
            var nextT: Calendar? = null
            var currN = "Yatsı"

            for (i in sortedTimes.indices) {
                if (now.before(sortedTimes[i].second)) {
                    nextN = sortedTimes[i].first
                    nextT = sortedTimes[i].second
                    currN = if (i == 0) "Yatsı" else sortedTimes[i - 1].first
                    break
                }
            }

            if (nextT == null && sortedTimes.isNotEmpty()) {
                nextT = (sortedTimes.first().second.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, 1) }
            }

            if (nextT == null) return null

            // Kerahat Vakti Kontrolü
            var isKerahat = false
            try {
                val sunriseCal = sortedTimes.find { it.first == "Güneş" }?.second
                val dhuhrCal = sortedTimes.find { it.first == "Öğle" }?.second
                val maghribCal = sortedTimes.find { it.first == "Akşam" }?.second

                if (sunriseCal != null) {
                    val sunriseEnd = (sunriseCal.clone() as Calendar).apply { add(Calendar.MINUTE, 45) }
                    if (now.after(sunriseCal) && now.before(sunriseEnd)) isKerahat = true
                }
                if (dhuhrCal != null) {
                    val dhuhrStart = (dhuhrCal.clone() as Calendar).apply { add(Calendar.MINUTE, -45) }
                    if (now.after(dhuhrStart) && now.before(dhuhrCal)) isKerahat = true
                }
                if (maghribCal != null) {
                    val maghribStart = (maghribCal.clone() as Calendar).apply { add(Calendar.MINUTE, -45) }
                    if (now.after(maghribStart) && now.before(maghribCal)) isKerahat = true
                }
            } catch (e: Exception) {
                Log.e("PrayerService", "Kerahat kontrol hatası: ${e.message}")
            }

            val diff = (nextT.timeInMillis - now.timeInMillis).coerceAtLeast(0)
            return CountdownState(
                (diff / (1000 * 60 * 60)).toInt(),
                ((diff / (1000 * 60)) % 60).toInt(),
                ((diff / 1000) % 60).toInt(),
                nextN,
                currN,
                isKerahat
            )
        } catch (e: Exception) {
            Log.e("PrayerService", "calculateCountdown error: ${e.message}")
            return null
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)
            val serviceChannel = NotificationChannel(
                CHANNEL_ID, "Vakit Takip Servisi",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Namaz vakitlerini bildirim panelinde gösterir"
                setShowBadge(false)
            }
            manager.createNotificationChannel(serviceChannel)
        }
    }

    override fun onDestroy() {
        timer?.cancel()
        super.onDestroy()
    }
}
