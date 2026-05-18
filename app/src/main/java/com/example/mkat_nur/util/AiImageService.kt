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

    private val generativeModel = Firebase.ai.generativeModel(
        modelName = "gemini-flash-latest"
    )

    /**
     * Gemini kullanarak içeriğe uygun bir görsel promptu oluşturur ve ardından Pollinations.ai üzerinden görseli çeker.
     */
    suspend fun generateAiBackground(content: String): Bitmap? = withContext(Dispatchers.IO) {
        try {
            // 1. Gemini ile Prompt Oluşturma
            val promptSystem = "Sen bir görsel tasarım uzmanısın. Sana verilen dini metnin (ayet/hadis/dua) ruhuna uygun, " +
                    "huzurlu, profesyonel, sanatsal ve etkileyici bir arka plan resmi için İngilizce bir prompt yaz. " +
                    "Prompt sadece görsel detayları içermeli, metin içermemeli. Stil: Cinematic, highly detailed, 4k, serene. " +
                    "Sadece promptu döndür, başka açıklama yapma."
            
            val response = generativeModel.generateContent(
                content {
                    text("$promptSystem\n\nİçerik: $content")
                }
            )

            val visualPrompt = response.text?.trim() ?: "A serene Islamic art background, peaceful atmosphere, high quality"
            
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
