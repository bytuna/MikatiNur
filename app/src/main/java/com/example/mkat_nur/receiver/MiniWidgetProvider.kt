package com.example.mkat_nur.receiver

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.util.TypedValue
import android.widget.RemoteViews
import com.example.mkat_nur.MainActivity
import com.example.mkat_nur.R
import com.example.mkat_nur.util.PrayerManager

class MiniWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action in listOf("REFRESH_WIDGET", Intent.ACTION_TIME_TICK, Intent.ACTION_BOOT_COMPLETED)) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(ComponentName(context, MiniWidgetProvider::class.java))
            for (appWidgetId in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId)
            }
        }
    }

    companion object {
        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val prayerManager = PrayerManager(context)
            val prayerData = prayerManager.getTodayPrayerData()
            val views = RemoteViews(context.packageName, R.layout.widget_mini)

            // Transparency
            views.setFloat(R.id.widget_bg_image, "setAlpha", prayerManager.getWidgetTransparency())

            // Content
            views.setTextViewText(R.id.widget_city, prayerManager.getCityName())

            if (prayerData != null) {
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
                    
                    val remainingMillis = time - System.currentTimeMillis()
                    views.setChronometer(R.id.widget_countdown, SystemClock.elapsedRealtime() + remainingMillis, null, true)
                    views.setChronometerCountDown(R.id.widget_countdown, true)
                }
            }

            // Click to Open App
            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(context, 10, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            views.setOnClickPendingIntent(R.id.widget_mini_container, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
