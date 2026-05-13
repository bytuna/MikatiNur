package com.example.mkat_nur.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.mkat_nur.R

class PrayerAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val prayerName = intent.getStringExtra("prayer_name") ?: "Vakit"
        Log.d("PrayerAlarm", "Received alarm for $prayerName")

        // Play sound
        try {
            val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val r = RingtoneManager.getRingtone(context, notification)
            r.play()
        } catch (e: Exception) {
            Log.e("PrayerAlarm", "Error playing sound: ${e.message}")
        }

        // Show a temporary heads-up notification for the arrival of the prayer time
        showArrivalNotification(context, prayerName)
    }

    private fun showArrivalNotification(context: Context, prayerName: String) {
        val channelId = "prayer_arrival_channel"
        val notificationId = 1002

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Ezan Vakti")
            .setContentText("$prayerName vakti girdi.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        try {
            with(NotificationManagerCompat.from(context)) {
                notify(notificationId, builder.build())
            }
        } catch (e: SecurityException) {
            Log.e("PrayerAlarm", "Permission not granted for notification: ${e.message}")
        } catch (e: Exception) {
            Log.e("PrayerAlarm", "Error showing notification: ${e.message}")
        }
    }
}
