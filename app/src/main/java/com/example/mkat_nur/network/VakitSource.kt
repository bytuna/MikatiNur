package com.example.mkat_nur.network

import com.example.mkat_nur.model.VakitResponse
import com.example.mkat_nur.viewmodel.RetrofitClient

/**
 * Vakit kaynağı:
 *  - Mekke / Medine: Ümmü'l-Kurâ takvimi, kendi sitemizden (hesaplama).
 *  - Diğer tüm yerler: eskisi gibi Diyanet verisi (Türkiye'de hiçbir şey değişmez).
 */
object VakitSource {
    private val UMMALQURA_IDS = setOf("16309", "16308")

    fun isUmmAlQura(id: String): Boolean = id in UMMALQURA_IDS

    suspend fun fetch(cityIdentifier: String): List<VakitResponse> {
        if (isUmmAlQura(cityIdentifier)) {
            try {
                val list = RetrofitClient.mikatInstance.vakitler(cityIdentifier)
                if (list.isNotEmpty()) return list
            } catch (_: Exception) {
            }
        }
        return try {
            RetrofitClient.diyanetInstance.getVakitler(cityIdentifier)
        } catch (_: Exception) {
            emptyList()
        }
    }
}
