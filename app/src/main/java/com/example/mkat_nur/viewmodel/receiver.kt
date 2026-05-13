package com.example.mkat_nur.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import com.example.mkat_nur.util.NotificationHelper

class PrayerNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val prayerName = intent.getStringExtra("PRAYER_NAME") ?: "Vakit"
        val minutesLeft = intent.getIntExtra("MINUTES_LEFT", 0)

        // Telefon uykudaysa uyandır (WakeLock)
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "MkatNur:NotificationWakeLock"
        )
        
        try {
            wakeLock.acquire(3000L) // 3 saniye uyanık tut
            
            val helper = NotificationHelper(context)
            val title = if (minutesLeft == 0) "Ezan Vakti" else "Namaz Vakti Yaklaşıyor"
            val message = if (minutesLeft == 0) "$prayerName vakti girdi." else "$prayerName vaktine $minutesLeft dakika kaldı."
            
            helper.showNotification(title, message)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (wakeLock.isHeld) wakeLock.release()
        }
    }
}
