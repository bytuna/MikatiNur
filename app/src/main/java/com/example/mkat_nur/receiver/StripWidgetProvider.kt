package com.example.mkat_nur.receiver

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import com.example.mkat_nur.MainActivity
import com.example.mkat_nur.R
import com.example.mkat_nur.util.PrayerManager

class StripWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action in listOf("REFRESH_WIDGET", Intent.ACTION_TIME_TICK, Intent.ACTION_BOOT_COMPLETED)) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(ComponentName(context, StripWidgetProvider::class.java))
            for (appWidgetId in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId)
            }
        }
    }

    companion object {
        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val prayerManager = PrayerManager(context)
            val prayerData = prayerManager.getTodayPrayerData()
            val views = RemoteViews(context.packageName, R.layout.widget_strip)

            views.setFloat(R.id.widget_bg_image, "setAlpha", prayerManager.getWidgetTransparency())

            if (prayerData != null) {
                views.setTextViewText(R.id.tv_imsak, prayerData.timings.fajr.substringBefore(" "))
                views.setTextViewText(R.id.tv_gunes, prayerData.timings.sunrise.substringBefore(" "))
                views.setTextViewText(R.id.tv_ogle, prayerData.timings.dhuhr.substringBefore(" "))
                views.setTextViewText(R.id.tv_ikindi, prayerData.timings.asr.substringBefore(" "))
                views.setTextViewText(R.id.tv_aksam, prayerData.timings.maghrib.substringBefore(" "))
                views.setTextViewText(R.id.tv_yatsi, prayerData.timings.isha.substringBefore(" "))

                val currentVakit = prayerManager.getCurrentVakit(prayerData)
                val activeBg = R.drawable.widget_active_bg
                views.setInt(R.id.container_imsak, "setBackgroundResource", if (currentVakit == "imsak") activeBg else 0)
                views.setInt(R.id.container_gunes, "setBackgroundResource", if (currentVakit == "gunes") activeBg else 0)
                views.setInt(R.id.container_ogle, "setBackgroundResource", if (currentVakit == "ogle") activeBg else 0)
                views.setInt(R.id.container_ikindi, "setBackgroundResource", if (currentVakit == "ikindi") activeBg else 0)
                views.setInt(R.id.container_aksam, "setBackgroundResource", if (currentVakit == "aksam") activeBg else 0)
                views.setInt(R.id.container_yatsi, "setBackgroundResource", if (currentVakit == "yatsi") activeBg else 0)
            }

            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(context, 20, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            views.setOnClickPendingIntent(R.id.widget_strip_container, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
