package com.example.mkat_nur.util

import androidx.compose.ui.graphics.Color

// Kullanıcının ayarlardan seçtiği tecvid filtre durumlarını tutan model
data class TajweedFilterSettings(
    val showMadd: Boolean = true,       // Tüm uzatmalar açık/kapalı
    val showGhunnah: Boolean = true,     // İhfa, İklab, Ğunne açık/kapalı
    val showIdghaam: Boolean = true,     // Tüm idgam kuralları açık/kapalı
    val showQalqalah: Boolean = true,    // Kalkale açık/kapalı
    val showSilent: Boolean = true       // Okunmayan harfler açık/kapalı
)

object TajweedColorMapper {

    // JSON'dan gelen string kuralı, bizim Enum kuralımıza eşler
    fun getRuleType(rule: String): TajweedRule? {
        return when (rule.lowercase().trim()) {
            "madd_6" -> TajweedRule.MADD_LAZIM
            "madd_muttasil", "madd_munfasil" -> TajweedRule.MADD_MUTTASIL_MUNFASIL
            "madd_246" -> TajweedRule.MADD_OPTIONAL
            "madd_2" -> TajweedRule.MADD_TABII

            "ghunnah", "ikhfa", "ikhfa_shafawi", "iqlab" -> TajweedRule.GHUNNAH_IHFA

            "idghaam_ghunnah", "idghaam_no_ghunnah", "idghaam_shafawi",
            "idghaam_mutajanisayn", "idghaam_mutaqaribayn" -> TajweedRule.GHUNNAH_IHFA

            "qalqalah" -> TajweedRule.QALQALAH

            "hamzat_wasl", "lam_shamsiyyah", "silent" -> TajweedRule.SILENT

            else -> null
        }
    }

    // Kullanıcının ayarlarına göre kuralın boyanıp boyanmayacağına karar verir
    fun getTajweedColor(
        rule: String,
        activeRules: Set<TajweedRule>,
        filterSettings: TajweedFilterSettings = TajweedFilterSettings()
    ): Color {
        val ruleType = getRuleType(rule) ?: return Color.Unspecified

        // Eğer kural genel set içinde yoksa zaten boyama
        if (!activeRules.contains(ruleType)) return Color.Unspecified

        // JSON'dan gelen kural ismine göre kullanıcının aç/kapa ayarını kontrol et
        val ruleLower = rule.lowercase().trim()
        val isAllowedByFilter = when {
            ruleLower.startsWith("madd_") -> filterSettings.showMadd
            ruleLower.contains("ghunnah") || ruleLower.contains("ikhfa") || ruleLower == "iqlab" -> filterSettings.showGhunnah
            ruleLower.startsWith("idghaam_") -> filterSettings.showIdghaam
            ruleLower == "qalqalah" -> filterSettings.showQalqalah
            ruleLower == "silent" || ruleLower == "lam_shamsiyyah" || ruleLower == "hamzat_wasl" -> filterSettings.showSilent
            else -> true
        }

        // Eğer kullanıcı butonu kapatmışsa renksiz (Unspecified) bırak, açıksa kuralın rengini bas
        return if (isAllowedByFilter) ruleType.color else Color.Unspecified
    }
}
