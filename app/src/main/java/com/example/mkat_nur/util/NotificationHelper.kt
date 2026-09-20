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
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }
            manager.createNotificationChannel(channel)
        }
    }

    fun showNotification(title: String, message: String, fullScreenIntent: Intent? = null) {
        createNotificationChannel() // Kanalın varlığından emin ol
        
        val channelId = getDynamicChannelId()
        
        val trLocale = java.util.Locale("tr", "TR")
        val lowerText = (title + " " + message).lowercase(trLocale)
        
        // Vaktinden önceki erken hatırlatıcı kontrolü (15 dk, 30 dk, 45 dk vb.)
        val isPreReminder = lowerText.contains("kaldı") || lowerText.contains("kalmıştır") || 
                lowerText.contains("dk") || lowerText.contains("hatırlatıcı")

        val prayerKey = when {
            lowerText.contains("imsak") -> "imsak"
            lowerText.contains("sabah") -> "sabah"
            lowerText.contains("öğle") || lowerText.contains("ogle") -> "ogle"
            lowerText.contains("ikindi") -> "ikindi"
            lowerText.contains("akşam") || lowerText.contains("aksam") || lowerText.contains("iftar") -> "aksam"
            lowerText.contains("yatsı") || lowerText.contains("yatsi") -> "yatsi"
            else -> "generic"
        }

        val soundMode = if (prayerKey != "generic") {
            prefs.getString("prayer_sound_mode_$prayerKey", "ezan") ?: "ezan"
        } else "system"

        val ezanResMap = mapOf(
            "sabah" to R.raw.sabah_ezani,
            "ogle" to R.raw.ogle_ezani,
            "ikindi" to R.raw.ikindi_ezani,
            "aksam" to R.raw.aksam_ezani,
            "yatsi" to R.raw.yatsi_ezani,
            "imsak" to R.raw.sabah_ezani
        )

        val soundUri: Uri? = if (isPreReminder) {
            // Vaktinden önceki erken hatırlatıcılar SADECE kısa sistem sesi çalar (asla uzun ezan okumaz)
            val soundUriStr = prefs.getString("notif_sound_uri", null)
            if (soundUriStr != null) Uri.parse(soundUriStr) else RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        } else {
            // Tam Vaktinde ("0 dk"): Vakfe özel seçilen moda (Ezan / Sistem Sesi / Sessiz) uyulur!
            when (soundMode) {
                "silent" -> null
                "ezan" -> {
                    val resId = ezanResMap[prayerKey]
                    if (resId != null) Uri.parse("android.resource://${context.packageName}/$resId") else {
                        val soundUriStr = prefs.getString("notif_sound_uri", null)
                        if (soundUriStr != null) Uri.parse(soundUriStr) else RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                    }
                }
                else -> {
                    val soundUriStr = prefs.getString("notif_sound_uri", null)
                    if (soundUriStr != null) Uri.parse(soundUriStr) else RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                }
            }
        }

        val mainIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val mainPendingIntent = PendingIntent.getActivity(
            context, 
            0, 
            mainIntent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_mosque)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL) // Ses, Titreşim ve Işıkları varsayılan yap
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setSound(soundUri)
            .setAutoCancel(true)
            .setContentIntent(mainPendingIntent)

        if (fullScreenIntent != null) {
            val popupPendingIntent = PendingIntent.getActivity(
                context,
                1,
                fullScreenIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.setFullScreenIntent(popupPendingIntent, true)
        } else {
            builder.setFullScreenIntent(mainPendingIntent, true) // Kilit ekranında göstermek için varsayılan
        }

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
