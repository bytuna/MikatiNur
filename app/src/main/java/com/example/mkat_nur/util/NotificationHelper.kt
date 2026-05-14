package com.example.mkat_nur.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.mkat_nur.MainActivity
import com.example.mkat_nur.R

class NotificationHelper(val context: Context) {
    private val prefs = context.getSharedPreferences("mkat_nur_prefs", Context.MODE_PRIVATE)
    
    // Android 8.0+ için kanal ID'si her ses değişiminde farklı olmalıdır, yoksa ses güncellenmez.
    private fun getDynamicChannelId(): String {
        val soundUri = prefs.getString("notif_sound_uri", "default")
        // Ses URI'sini güvenli bir stringe çevirip kanal ID'sine ekliyoruz
        return "prayer_channel_" + soundUri.hashCode().toString()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = getDynamicChannelId()
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            // Eğer bu ses için kanal zaten varsa dokunma
            if (manager.getNotificationChannel(channelId) != null) return

            val soundUriStr = prefs.getString("notif_sound_uri", null)
            val soundUri = if (soundUriStr != null) Uri.parse(soundUriStr) else RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            val channel = NotificationChannel(
                channelId,
                "Namaz Vakti Uyarıları",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Vakit öncesi hatırlatıcı bildirimleri"
                setSound(soundUri, AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build())
                enableLights(true)
                enableVibration(true)
            }
            manager.createNotificationChannel(channel)
        }
    }

    fun showNotification(title: String, message: String) {
        createNotificationChannel() // Kanalın varlığından emin ol
        
        val channelId = getDynamicChannelId()
        val soundUriStr = prefs.getString("notif_sound_uri", null)
        val soundUri = if (soundUriStr != null) Uri.parse(soundUriStr) else RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 
            0, 
            intent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_mosque)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setSound(soundUri)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setFullScreenIntent(pendingIntent, true) // Kilit ekranında göstermek için

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Sesin kesin çalması için manuel tetikleme (Yedek mekanizma)
        try {
            val r = RingtoneManager.getRingtone(context, soundUri)
            r.play()
        } catch (e: Exception) {
            Log.e("NotificationHelper", "Ringtone play error: ${e.message}")
        }

        manager.notify(System.currentTimeMillis().toInt(), builder.build())
    }
}
