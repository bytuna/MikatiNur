package com.example.mkat_nur.receiver

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.mkat_nur.MainActivity
import com.example.mkat_nur.R
import com.example.mkat_nur.util.ContentManager
import com.example.mkat_nur.util.PrayerManager

class DailyQuoteWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action in listOf("REFRESH_WIDGET", Intent.ACTION_BOOT_COMPLETED)) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(ComponentName(context, DailyQuoteWidgetProvider::class.java))
            for (appWidgetId in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId)
            }
        }
    }

    companion object {
        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val contentManager = ContentManager(context)
            val prayerManager = PrayerManager(context)
            val content = contentManager.getRandomContent()
            val views = RemoteViews(context.packageName, R.layout.widget_quote)

            views.setFloat(R.id.widget_bg_image, "setAlpha", prayerManager.getWidgetTransparency())
            
            views.setTextViewText(R.id.widget_title, content.type)
            views.setTextViewText(R.id.widget_text, content.text)
            views.setTextViewText(R.id.widget_source, content.source)

            // Refresh Button
            val refreshIntent = Intent(context, DailyQuoteWidgetProvider::class.java).apply {
                action = "REFRESH_WIDGET"
            }
            val refreshPendingIntent = PendingIntent.getBroadcast(context, 3, refreshIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            views.setOnClickPendingIntent(R.id.widget_refresh, refreshPendingIntent)

            // Open App
            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(context, 30, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            views.setOnClickPendingIntent(R.id.widget_quote_container, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
