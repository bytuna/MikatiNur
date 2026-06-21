package com.example.mkat_nur.receiver

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.SystemClock
import android.widget.RemoteViews
import com.example.mkat_nur.MainActivity
import com.example.mkat_nur.R
import com.example.mkat_nur.model.PrayerData
import com.example.mkat_nur.util.PrayerManager
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MiniWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
        schedulePeriodicUpdate(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val actions = listOf(
            "REFRESH_WIDGET",
            Intent.ACTION_TIME_TICK,
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED
        )
        if (intent.action in actions) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisWidget = ComponentName(context, MiniWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
            for (appWidgetId in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId)
            }
            
            if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == Intent.ACTION_TIMEZONE_CHANGED) {
                schedulePeriodicUpdate(context)
            }
        }
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        schedulePeriodicUpdate(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        cancelPeriodicUpdate(context)
    }

    private fun schedulePeriodicUpdate(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, MiniWidgetProvider::class.java).apply {
            action = "REFRESH_WIDGET"
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, 1001, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Her 1 dakikada bir kontrol et (Genel tazelik için)
        alarmManager.setInexactRepeating(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            SystemClock.elapsedRealtime() + 60000,
            60000,
            pendingIntent
        )
    }

    private fun cancelPeriodicUpdate(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, MiniWidgetProvider::class.java).apply {
            action = "REFRESH_WIDGET"
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, 1001, intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
        }
    }

    companion object {
        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val prayerManager = PrayerManager(context)
            var prayerData = prayerManager.getTodayPrayerData()
            val views = RemoteViews(context.packageName, R.layout.widget_mini)

            // Transparency
            views.setFloat(R.id.widget_bg_image, "setAlpha", prayerManager.getWidgetTransparency())

            // Content
            views.setTextViewText(R.id.widget_city, prayerManager.getCityName())

            if (prayerData != null) {
                // Kullanıcı ofsetlerini uygula (Senior dokunuşu: Uygulama ile senkronize olsun)
                prayerData = applyOffsets(context, prayerData)
                
                val nextInfo = prayerManager.getNextVakitInfo(prayerData)
                if (nextInfo != null) {
                    val (label, time) = nextInfo
                    val suffix = when (label) {
                        "İmsak" -> "İmsak'a"
                        "Güneş" -> "Güneş'e"
                        "Öğle" -> "Öğle'ye"
                        "İkindi" -> "İkindi'ye"
                        "Akşam" -> "Akşam'a"
                        "Yatsı" -> "Yatsı'ya"
                        else -> label
                    }
                    views.setTextViewText(R.id.widget_vakit_name, suffix)
                    
                    val now = System.currentTimeMillis()
                    val remainingMillis = time - now
                    
                    // Eksiye düşmemesi için kontrol
                    if (remainingMillis > 0) {
                        views.setChronometer(R.id.widget_countdown, SystemClock.elapsedRealtime() + remainingMillis, null, true)
                        views.setChronometerCountDown(R.id.widget_countdown, true)
                        
                        // Vakit dolduğunda widget'ı tazelemek için tam zamanına alarm kur
                        scheduleExactUpdate(context, time)
                    } else {
                        // Vakit geçtiyse hemen bir sonraki vakti bulmaya çalış
                        val nextVakit = prayerManager.getNextVakitInfo(prayerData)
                        if (nextVakit != null && nextVakit.second > now) {
                            val newRemaining = nextVakit.second - now
                            views.setChronometer(R.id.widget_countdown, SystemClock.elapsedRealtime() + newRemaining, null, true)
                            views.setChronometerCountDown(R.id.widget_countdown, true)
                        } else {
                            views.setChronometer(R.id.widget_countdown, SystemClock.elapsedRealtime(), null, true)
                        }
                    }
                }
            }

            // Click to Open App
            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(context, 10, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            views.setOnClickPendingIntent(R.id.widget_mini_container, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private fun applyOffsets(context: Context, data: PrayerData): PrayerData {
            val prefs = context.getSharedPreferences("mkat_nur_prefs", Context.MODE_PRIVATE)
            val commonOffset = prefs.getInt("time_offset", 0)
            val imsakOffset = prefs.getInt("offset_imsak", 0)
            val sunriseOffset = prefs.getInt("offset_sunrise", 0)
            val dhuhrOffset = prefs.getInt("offset_dhuhr", 0)
            val asrOffset = prefs.getInt("offset_asr", 0)
            val maghribOffset = prefs.getInt("offset_maghrib", 0)
            val ishaOffset = prefs.getInt("offset_isha", 0)

            fun apply(timeStr: String, offset: Int): String {
                if (offset == 0) return timeStr
                return try {
                    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                    val date = sdf.parse(timeStr.substringBefore(" "))!!
                    val cal = Calendar.getInstance().apply {
                        time = date
                        add(Calendar.MINUTE, offset)
                    }
                    sdf.format(cal.time)
                } catch (e: Exception) { timeStr }
            }

            return data.copy(
                timings = data.timings.copy(
                    fajr = apply(data.timings.fajr, commonOffset + imsakOffset),
                    sunrise = apply(data.timings.sunrise, commonOffset + sunriseOffset),
                    dhuhr = apply(data.timings.dhuhr, commonOffset + dhuhrOffset),
                    asr = apply(data.timings.asr, commonOffset + asrOffset),
                    maghrib = apply(data.timings.maghrib, commonOffset + maghribOffset),
                    isha = apply(data.timings.isha, commonOffset + ishaOffset)
                )
            )
        }

        private fun scheduleExactUpdate(context: Context, time: Long) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, MiniWidgetProvider::class.java).apply {
                action = "REFRESH_WIDGET"
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context, 1002, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pendingIntent)
                    } else {
                        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pendingIntent)
                    }
                } else {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pendingIntent)
                }
            } catch (e: Exception) {
                alarmManager.set(AlarmManager.RTC_WAKEUP, time, pendingIntent)
            }
        }
    }
}
