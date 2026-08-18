package com.example.mkat_nur.util

import android.content.Context
import java.text.SimpleDateFormat
import java.util.*

object AppConfig {
    // Versiyon kontrolü yapan fonksiyonumuz
    fun checkUpdate() {
        println("Yeni güncellemeler kontrol ediliyor...")
    }

    const val VERSION_NAME = "1.1.1"
    const val VERSION_CODE = 4
    const val DEVELOPER = "ByTuna"
    const val PROJECT_NAME = "Mîkat-ı Nur"
    const val BUILD_DATE = "2026"

    // Güncelleme kontrolü için ham URL
    private const val UPDATE_JSON_URL = "https://raw.githubusercontent.com/bytuna/MikatiNur/master/update_info.json"
    const val DOWNLOAD_URL = "https://www.mikatinur.com.tr/indir"

    fun isNewerVersion(latestVersionName: String): Boolean {
        val currentParts = VERSION_NAME.removePrefix("v").split(".").mapNotNull { it.toIntOrNull() }
        val latestParts = latestVersionName.removePrefix("v").split(".").mapNotNull { it.toIntOrNull() }
        
        val maxLength = maxOf(currentParts.size, latestParts.size)
        for (i in 0 until maxLength) {
            val currentVal = currentParts.getOrNull(i) ?: 0
            val latestVal = latestParts.getOrNull(i) ?: 0
            if (latestVal > currentVal) return true
            if (latestVal < currentVal) return false
        }
        return false
    }

    // Quran API Config
    const val QURAN_API_BASE_URL = "https://api.quran.com/api/v4/"
    const val QURAN_API_TOKEN = "" // Quran.com API v4 public endpoint doesn't strictly require token for basic usage

    // Versiyonlama yapısı
    data class VersionHistory(
        val versionName: String,
        val versionCode: Int,
        val date: String,
        val description: String,
        val developer: String
    )

    val history = listOf(
        VersionHistory(
            versionName = "1.1.1",
            versionCode = 4,
            date = "2026-05-21",
            description = "Bildirim paneli ve menü optimizasyonları yapıldı. Güncelleme sistemi iyileştirildi.",
            developer = "ByTuna"
        ),
        VersionHistory(
            versionName = "1.1.0",
            versionCode = 3,
            date = "2026-05-20",
            description = "Risale-i Nur WebView entegrasyonu tamamlandı, eski yerel veritabanları temizlendi.",
            developer = "ByTuna"
        ),
        VersionHistory(
            versionName = "1.0.1",
            versionCode = 2,
            date = "2026-05-15",
            description = "Uygulama paylaşma ve otomatik güncelleme kontrolü eklendi.",
            developer = "ByTuna"
        ),
        VersionHistory(
            versionName = "1.0.0",
            versionCode = 1,
            date = "2026-05-14",
            description = "İlk sürüm yayınlandı. Cuma mesajları ve temel özellikler eklendi.",
            developer = "ByTuna"
        )
    )

    val currentVersion = history.first()

    /**
     * Uygulamanın sistemdeki son güncelleme tarihini döndürür.
     */
    fun getAppLastUpdateTime(context: Context): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            val lastUpdateTime = packageInfo.lastUpdateTime
            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            formatter.format(Date(lastUpdateTime))
        } catch (e: Exception) {
            currentVersion.date
        }
    }
}
