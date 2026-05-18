package com.example.mkat_nur.util

import com.example.mkat_nur.model.RisalePage
import com.google.gson.GsonBuilder
import java.io.File

/**
 * Senior Geliştirici Notu: 
 * Bu sınıf, ham metin dosyalarını uygulamanın anladığı JSON formatına çevirmek için tasarlanmıştır.
 * PDF'den kopyaladığınız metinleri sayfa sayfa ayırarak JSON'a dönüştürür.
 */
object RisaleImporter {
    private val gson = GsonBuilder().setPrettyPrinting().create()

    /**
     * Ham metni sayfa işaretlerine göre böler ve JSON üretir.
     * Örn: Metin içinde [SAYFA 1], [SAYFA 2] gibi işaretler olduğunu varsayalım.
     */
    fun convertRawTextToJson(bookId: String, rawText: String, outputPath: String) {
        val pages = mutableListOf<RisalePage>()
        
        // Basit bir regex ile sayfaları ayırıyoruz (Örn: [1], [2] veya PDF sayfa sonları)
        val rawPages = rawText.split(Regex("\\[SAYFA \\d+\\]"))
        
        rawPages.forEachIndexed { index, content ->
            if (content.isNotBlank()) {
                val formattedContent = formatContent(content.trim())
                pages.add(RisalePage(bookId, index + 1, formattedContent))
            }
        }

        val json = gson.toJson(pages)
        File(outputPath).writeText(json)
        println("Başarıyla oluşturuldu: $outputPath")
    }

    /**
     * Metni otomatik zenginleştirir.
     * Arapça karakterleri algılayıp kırmızı yapar.
     */
    private fun formatContent(text: String): String {
        var formatted = text
        
        // Arapça karakter aralığı (Unicode: \u0600-\u06FF)
        // Bu regex ile Arapça kelimeleri bulup kırmızı etiket içine alıyoruz.
        val arabicRegex = Regex("([\\u0600-\\u06FF\\s]{3,})")
        formatted = arabicRegex.replace(formatted) { matchResult ->
            "<font color='#D32F2F'>${matchResult.value.trim()}</font>"
        }

        // Başlıkları tahmin et (Tümü büyük harf olan kısa satırlar)
        val lines = formatted.split("\n")
        val processedLines = lines.map { line ->
            if (line.length < 50 && line == line.uppercase() && line.isNotBlank()) {
                "<h1>$line</h1>"
            } else {
                line
            }
        }

        return processedLines.joinToString("\n")
    }
}
