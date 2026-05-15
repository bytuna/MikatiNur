package com.example.mkat_nur.util

import android.content.Context
import java.text.SimpleDateFormat
import java.util.*

object AppConfig {
    const val VERSION_NAME = "1.0.0"
    const val VERSION_CODE = 1
    const val DEVELOPER = "ByTuna"
    const val PROJECT_NAME = "Mîkat-ı Nur"
    const val BUILD_DATE = "2026"

    // Quran API Config
    const val QURAN_API_BASE_URL = "https://acikkaynakkuran.diyanet.gov.tr/api/"
    const val QURAN_API_TOKEN = "1106|r79EB70cL2Bc9Fh7mTwy7k8uZTnHGBdMAvQhQLeO46308dae"

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
            versionName = "1.0.0",
            versionCode = 1,
            date = "2026-05-14",
            description = "İlk sürüm yayınlandı. Cuma mesajları ve temel özellikler eklendi.",
            developer = "ByTuna"
        )
        // Yeni versiyonlar buraya eklenebilir
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
