package com.example.mkat_nur.util

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings

/**
 * Android 14 (API 34) ve üzerinde "Tam ekran bildirimler" kullanıcı iznine bağlıdır.
 * Manifest'teki USE_FULL_SCREEN_INTENT tek başına yetmez; Google Play'den yüklenen,
 * arama/alarm uygulaması olarak sınıflandırılmamış uygulamalarda izin kapalı gelebilir.
 *
 * Bu yardımcı: iznin durumunu söyler ve kullanıcıyı doğrudan ilgili sistem ayarına götürür.
 */
object FullScreenIntentHelper {
    private const val PREFS = "mkat_nur_prefs"
    private const val KEY_ASKED = "full_screen_intent_asked"

    /** Tam ekran bildirim şu an kullanılabilir mi? (Android 13 ve altında her zaman evet) */
    fun canUse(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) return true
        return try {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.canUseFullScreenIntent()
        } catch (e: Exception) {
            true
        }
    }

    /** İzin eksik ve kullanıcıya daha önce hiç sorulmadıysa true. */
    fun shouldAsk(context: Context): Boolean {
        if (canUse(context)) return false
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return !prefs.getBoolean(KEY_ASKED, false)
    }

    fun markAsked(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_ASKED, true).apply()
    }

    /** Uygulamanın "Tam ekran bildirimler" ayar sayfasını açar (API 34+). */
    fun openSettings(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) return
        try {
            context.startActivity(
                Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT).apply {
                    data = Uri.parse("package:${context.packageName}")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            )
        } catch (e: Exception) {
            try {
                context.startActivity(
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.parse("package:${context.packageName}")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                )
            } catch (_: Exception) { }
        }
    }
}
