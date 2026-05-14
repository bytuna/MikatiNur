package com.example.mkat_nur.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import com.example.mkat_nur.FridayMessageActivity
import com.example.mkat_nur.PrayerPopupActivity
import com.example.mkat_nur.util.ContentManager
import com.example.mkat_nur.util.NotificationHelper

class PrayerNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val isFridayMessage = intent.getBooleanExtra("IS_FRIDAY_MESSAGE", false)
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
            
            val contentManager = ContentManager(context)
            val helper = NotificationHelper(context)

            if (isFridayMessage) {
                helper.showNotification("Hayırlı Cumalar", "Günün Cuma mesajı hazır. Görmek için tıklayın.")
                
                val fridayIntent = Intent(context, FridayMessageActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                context.startActivity(fridayIntent)
                return
            }

            val randomContent = contentManager.getRandomContent()
            
            val title = if (minutesLeft == 0) "Ezan Vakti: $prayerName" else "Namaz Vakti Yaklaşıyor"
            
            val contentBody = if (randomContent.source.isNotEmpty()) {
                "${randomContent.text} (${randomContent.source})"
            } else {
                randomContent.text
            }
            
            val message = if (minutesLeft == 0) contentBody else "$prayerName vaktine $minutesLeft dakika kaldı."
            
            helper.showNotification(title, message)

            // EĞER VAKİT TAM GİRDİYSE (0. dakika), HARİCİ EKRANI AÇ
            if (minutesLeft == 0) {
                val popupIntent = Intent(context, PrayerPopupActivity::class.java).apply {
                    putExtra("TYPE", randomContent.type)
                    putExtra("CONTENT", randomContent.text)
                    putExtra("SOURCE", randomContent.source)
                    putExtra("PRAYER_NAME", prayerName)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                context.startActivity(popupIntent)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (wakeLock.isHeld) wakeLock.release()
        }
    }
}
