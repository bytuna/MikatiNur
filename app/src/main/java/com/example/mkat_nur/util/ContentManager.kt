package com.example.mkat_nur.util

import android.content.Context
import org.json.JSONObject
import java.util.*

data class ManasContent(val type: String, val text: String, val source: String)
data class FridayContent(val ayet: ManasContent, val hadis: ManasContent, val vecize: ManasContent)

class ContentManager(val context: Context) {
    fun getRandomContent(): ManasContent {
        val types = listOf("ayet", "hadis", "vecize")
        return getContentByType(types.random())
    }

    fun getFridayContent(): FridayContent {
        return FridayContent(
            ayet = getContentByType("ayet"),
            hadis = getContentByType("hadis"),
            vecize = getContentByType("vecize")
        )
    }

    fun getContentByType(type: String): ManasContent {
        return try {
            val fileName = "texts/$type.txt"
            val typeLabel = when (type) {
                "ayet" -> "Günün Ayeti"
                "hadis" -> "Günün Hadisi"
                "vecize" -> "Günün Vecizesi"
                "esma" -> "Esmaü'l Hüsna"
                else -> "Günün Sözü"
            }

            val inputStream = context.assets.open(fileName)
            val lines = inputStream.bufferedReader().readLines().filter { it.isNotBlank() }
            if (lines.isEmpty()) return getFallbackContent(typeLabel)

            val randomLine = lines.random().trim()
            
            // Metin ve kaynağı ayırmak için daha güvenli bir yöntem
            var text = ""
            var source = ""

            when {
                // 1. Tab karakteri varsa (Dosyalarınızdaki asıl ayırıcı bu)
                randomLine.contains("\t") -> {
                    val firstTabIndex = randomLine.indexOf("\t")
                    text = randomLine.substring(0, firstTabIndex).trim()
                    // Geri kalan kısmın içindeki virgül, tab ve boşlukları temizleyerek kaynağı al
                    source = randomLine.substring(firstTabIndex)
                        .replace("\t", "")
                        .trim()
                    
                    if (source.startsWith(",")) {
                        source = source.substring(1).trim()
                    }
                }
                // 2. " , " (boşluk virgül boşluk) varsa
                randomLine.contains(" , ") -> {
                    val parts = randomLine.split(" , ", limit = 2)
                    text = parts[0].trim()
                    source = if (parts.size > 1) parts[1].trim() else ""
                }
                // 3. Hiçbir özel ayırıcı yoksa ama virgül varsa (Tehlikeli ama fallback)
                // Metin içinde de virgül olabileceği için bu kısmı daha dikkatli yapıyoruz
                randomLine.contains(",") -> {
                    val parts = randomLine.split(",")
                    if (parts.size > 1) {
                        // Son iki parça genellikle kaynak bilgisidir (Örn: Nahl, 17)
                        // Bu yüzden sondan bir önceki virgülü bulmaya çalışalım
                        val lastComma = randomLine.lastIndexOf(",")
                        val secondLastComma = randomLine.lastIndexOf(",", lastComma - 1)
                        
                        if (secondLastComma != -1 && secondLastComma > randomLine.length * 0.5) {
                            text = randomLine.substring(0, secondLastComma).trim()
                            source = randomLine.substring(secondLastComma + 1).trim()
                        } else {
                            text = randomLine.substring(0, lastComma).trim()
                            source = randomLine.substring(lastComma + 1).trim()
                        }
                    } else {
                        text = randomLine
                        source = ""
                    }
                }
                else -> {
                    text = randomLine
                    source = ""
                }
            }

            // Metnin sonunda kalmış olabilecek virgül veya noktaları temizle
            while (text.endsWith(",") || text.endsWith("\t")) {
                text = text.substring(0, text.length - 1).trim()
            }

            ManasContent(typeLabel, text, source)
        } catch (e: Exception) {
            getFallbackContent(
                when (type) {
                    "ayet" -> "Günün Ayeti"
                    "hadis" -> "Günün Hadisi"
                    "vecize" -> "Günün Vecizesi"
                    else -> "Günün Sözü"
                }
            )
        }
    }

    private fun getFallbackContent(label: String) = ManasContent(
        label,
        "Şüphesiz güçlükle beraber bir kolaylık vardır.",
        "İnşirah, 5"
    )
}
