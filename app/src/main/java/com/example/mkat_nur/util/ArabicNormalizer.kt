package com.example.mkat_nur.util

/**
 * Risale-i Nur Legacy-to-Unicode Bridge.
 * Eski kodlamalı metinleri modern standartlara dönüştürür.
 */
object ArabicNormalizer {

    fun fixLegacyArabic(input: String): String {
        if (input.isBlank()) return input

        // 1. Çok karakterli kodları çevir
        var text = input
            .replace("َلِلّه", "اللّٰه")
            .replace("la", "لا")
            .replace("mj", "جم")
            
        // 2. Özel harf eşleşmeleri (Legacy Mapping)
        val mapping = mapOf(
            'l' to 'ل', 'n' to 'ن', 'm' to 'م', 'r' to 'ر', 's' to 'س',
            'b' to 'ب', 'y' to 'ي', 'h' to 'ه', 'v' to 'و', 'f' to 'ف',
            'z' to 'ز', 'x' to 'خ', 't' to 'ت', 'a' to 'ا', 'i' to 'ِ',
            'u' to 'ُ', 'e' to 'َ'
        )
        
        val sb = StringBuilder()
        for (char in text) {
            sb.append(mapping[char] ?: char)
        }
        text = sb.toString()

        // 3. Visual to Logical (Görselden Mantıksal Sıraya)
        // Legacy verilerde metin tam ters sırada saklanır.
        text = text.reversed()

        // 4. Profesyonel Boşluk Temizliği
        // Harfler birbirine değerse font onları otomatik birleştirir (Shaping).
        // Sadece kelime aralarındaki büyük boşlukları koruyoruz.
        return text.replace(Regex(" +"), " ").trim()
    }
}
