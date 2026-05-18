package com.example.mkat_nur.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL
import java.net.URLEncoder

object AiImageService {

    enum class ShareStyle(val displayName: String, val promptTemplate: String) {
        MINIMALIST(
            "Minimalist ve Spiritüel",
            "A minimalist and deeply spiritual digital art, representing the concept of \"[THEME]\". Soft divine light rays piercing through serene cosmic clouds, calm atmosphere, elegant and abstract background. High-end graphic design, pastel and gold color palette, corporate clean look, shot on 35mm lens, photorealistic texture, no text, 4k resolution"
        ),
        NATURE(
            "Epik Doğa Manzarası",
            "An epic cinematic landscape photography illustrating \"[THEME]\". Majestic mountains, vast starry night sky, a glowing river of light, breathtaking nature, divine and awe-inspiring atmosphere. Volumetric lighting, highly detailed, Unreal Engine 5 render, trending on ArtStation, no text, ultra-wide angle"
        ),
        ABSTRACT(
            "İslami Geometrik/Soyut",
            "Abstract Islamic art background, modern interpretation of geometric patterns and arabesque motifs reflecting \"[THEME]\". Subtle glowing lines, deep turquoise and emerald green tones with gold accents, elegant texture, depth of field, premium mobile wallpaper style, clean and peaceful, no typography"
        )
    }

    private val generativeModel = Firebase.ai.generativeModel(
        modelName = "gemini-flash-latest"
    )

    /**
     * Gemini kullanarak içeriğe uygun bir görsel promptu oluşturur ve ardından Pollinations.ai üzerinden görseli çeker.
     */
    suspend fun generateAiBackground(content: String, style: ShareStyle = ShareStyle.MINIMALIST): Bitmap? = withContext(Dispatchers.IO) {
        try {
            // 1. Gemini ile İçerik Temasını Belirleme (Promptu stile göre özelleştirmek için)
            val themeSystem = "Sana verilen dini metnin (ayet/hadis/dua) ana temasını ve ruhunu yansıtan en fazla 5-6 kelimelik İngilizce bir özet/tema yaz. " +
                    "Sadece temayı döndür, başka açıklama yapma."
            
            val themeResponse = generativeModel.generateContent(
                content {
                    text("$themeSystem\n\nİçerik: $content")
                }
            )

            val theme = themeResponse.text?.trim()?.removeSurrounding("\"") ?: "spiritual peace and light"
            
            // Seçilen stile göre promptu oluştur
            val visualPrompt = style.promptTemplate.replace("[THEME]", theme)
            
            // 2. Pollinations.ai ile Görseli Oluşturma/Çekme
            val encodedPrompt = URLEncoder.encode(visualPrompt, "UTF-8")
            val imageUrl = "https://image.pollinations.ai/prompt/$encodedPrompt?width=1080&height=1350&nologo=true&seed=${(0..100000).random()}"
            
            val connection = URL(imageUrl).openConnection()
            connection.connectTimeout = 30000
            connection.readTimeout = 30000
            
            val inputStream = connection.getInputStream()
            return@withContext BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
