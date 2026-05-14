package com.example.mkat_nur.receiver

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.View
import android.widget.RemoteViews
import com.example.mkat_nur.MainActivity
import com.example.mkat_nur.R
import com.example.mkat_nur.util.ContentManager
import com.example.mkat_nur.util.PrayerManager

class QuoteWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == "REFRESH_WIDGET" || intent.action == Intent.ACTION_TIME_TICK) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisWidget = ComponentName(context, QuoteWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
            for (appWidgetId in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId)
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
        val intent = Intent(context, QuoteWidgetProvider::class.java).apply {
            action = "REFRESH_WIDGET"
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, 999, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Her 1 saatte bir otomatik güncelle (Pil dostu inexact alarm)
        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis() + 3600000,
            3600000,
            pendingIntent
        )
    }

    private fun cancelPeriodicUpdate(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, QuoteWidgetProvider::class.java).apply {
            action = "REFRESH_WIDGET"
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, 999, intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
        }
    }

    companion object {
        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val contentManager = ContentManager(context)
            val prayerManager = PrayerManager(context)
            
            val content = contentManager.getRandomContent()
            val prayerData = prayerManager.getTodayPrayerData()

            val views = RemoteViews(context.packageName, R.layout.widget_layout)
            
            // Şeffaflık Uygula
            val transparency = prayerManager.getWidgetTransparency()
            views.setFloat(R.id.widget_bg_image, "setAlpha", transparency)
            
            // Renkleri Uygula
            val titleColor = prayerManager.getWidgetTitleColor()
            val textColor = prayerManager.getWidgetTextColor()
            
            views.setTextColor(R.id.widget_title, titleColor)
            views.setTextColor(R.id.widget_city, textColor)
            views.setTextColor(R.id.widget_text, textColor)
            views.setTextColor(R.id.widget_source, textColor)
            
            // Vakitlerin renklerini de güncelle
            views.setTextColor(R.id.tv_imsak, textColor)
            views.setTextColor(R.id.tv_gunes, textColor)
            views.setTextColor(R.id.tv_ogle, textColor)
            views.setTextColor(R.id.tv_ikindi, textColor)
            views.setTextColor(R.id.tv_aksam, textColor)
            views.setTextColor(R.id.tv_yatsi, textColor)
            
            // İçerik Güncelleme
            views.setTextViewText(R.id.widget_title, content.type)
            views.setTextViewText(R.id.widget_text, content.text)
            views.setTextViewText(R.id.widget_source, content.source)
            
            // Şehir ve Kerahat
            views.setTextViewText(R.id.widget_city, prayerManager.getCityName())
            
            if (prayerData != null) {
                views.setTextViewText(R.id.tv_imsak, prayerData.timings.fajr.substringBefore(" "))
                views.setTextViewText(R.id.tv_gunes, prayerData.timings.sunrise.substringBefore(" "))
                views.setTextViewText(R.id.tv_ogle, prayerData.timings.dhuhr.substringBefore(" "))
                views.setTextViewText(R.id.tv_ikindi, prayerData.timings.asr.substringBefore(" "))
                views.setTextViewText(R.id.tv_aksam, prayerData.timings.maghrib.substringBefore(" "))
                views.setTextViewText(R.id.tv_yatsi, prayerData.timings.isha.substringBefore(" "))
                
                if (prayerManager.isKerahat(prayerData)) {
                    views.setViewVisibility(R.id.widget_kerahat, View.VISIBLE)
                } else {
                    views.setViewVisibility(R.id.widget_kerahat, View.GONE)
                }

                // Vakit Vurgulama
                val currentVakit = prayerManager.getCurrentVakit(prayerData)
                
                val activeBg = R.drawable.widget_active_bg
                val normalBg = 0 // Saydam/Yok

                views.setInt(R.id.container_imsak, "setBackgroundResource", if (currentVakit == "imsak") activeBg else normalBg)
                views.setInt(R.id.container_gunes, "setBackgroundResource", if (currentVakit == "gunes") activeBg else normalBg)
                views.setInt(R.id.container_ogle, "setBackgroundResource", if (currentVakit == "ogle") activeBg else normalBg)
                views.setInt(R.id.container_ikindi, "setBackgroundResource", if (currentVakit == "ikindi") activeBg else normalBg)
                views.setInt(R.id.container_aksam, "setBackgroundResource", if (currentVakit == "aksam") activeBg else normalBg)
                views.setInt(R.id.container_yatsi, "setBackgroundResource", if (currentVakit == "yatsi") activeBg else normalBg)
            }

            // Tıklayınca uygulamayı aç
            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)

            // Yenileme butonu
            val refreshIntent = Intent(context, QuoteWidgetProvider::class.java).apply {
                action = "REFRESH_WIDGET"
            }
            val refreshPendingIntent = PendingIntent.getBroadcast(
                context, 1, refreshIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_refresh, refreshPendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
