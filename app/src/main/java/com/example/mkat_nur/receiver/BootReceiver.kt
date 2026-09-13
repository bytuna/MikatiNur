package com.example.mkat_nur.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.mkat_nur.model.PrayerData
import com.example.mkat_nur.service.PrayerNotificationService
import com.google.gson.Gson

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.d("BootReceiver", "Received boot or update event: $action")

        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_MY_PACKAGE_REPLACED ||
            action == "android.intent.action.QUICKBOOT_POWERON"
        ) {
            try {
                val prefs = context.getSharedPreferences("mkat_nur_prefs", Context.MODE_PRIVATE)
                val cachedJson = prefs.getString("notif_cached_data", null)
                val cachedProvince = prefs.getString("notif_cached_province", "") ?: ""

                if (!cachedJson.isNullOrEmpty()) {
                    val data = Gson().fromJson(cachedJson, PrayerData::class.java)
                    if (data != null) {
                        PrayerNotificationService.startService(context, cachedProvince, data)
                        Log.d("BootReceiver", "PrayerNotificationService automatically restarted.")
                    }
                }

                QuoteWidgetProvider.updateAllWidgets(context)
            } catch (e: Exception) {
                Log.e("BootReceiver", "Error restoring service on boot: ${e.message}")
            }
        }
    }
}
