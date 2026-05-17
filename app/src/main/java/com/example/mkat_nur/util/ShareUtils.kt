package com.example.mkat_nur.util

import android.content.Context
import android.content.Intent
import android.graphics.*
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

object ShareUtils {

    enum class CardTheme(val primaryColor: String, val accentColor: String, val bgStart: String, val bgEnd: String) {
        DEEP_BLUE("#0D1B2A", "#FFD700", "#1B263B", "#0D1B2A"),
        EMERALD("#004D40", "#A5D6A7", "#00695C", "#00241B"),
        SUNSET("#370617", "#FFBA08", "#6A040F", "#370617"),
        ROYAL("#240046", "#E0AAFF", "#3C096C", "#10002B")
    }

    fun shareInfoAsImage(context: Context, title: String, content: String, source: String) {
        val theme = CardTheme.values().random()
        val width = 1080
        val height = 1350
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        drawAtmosphericBackground(canvas, width, height, theme)

        val paint = Paint().apply { isAntiAlias = true }
        
        // App Name at top center
        paint.color = Color.WHITE
        paint.alpha = 180
        paint.textSize = 40f
        paint.isFakeBoldText = true
        paint.letterSpacing = 0.2f
        canvas.drawText(AppConfig.PROJECT_NAME.uppercase(), (width / 2f) - (paint.measureText(AppConfig.PROJECT_NAME.uppercase()) / 2f), 120f, paint)

        // Title (Ayet/Hadis) with accent
        paint.color = Color.parseColor(theme.accentColor)
        paint.alpha = 255
        paint.textSize = 70f
        paint.isFakeBoldText = true
        paint.letterSpacing = 0.05f
        canvas.drawText(title, (width / 2f) - (paint.measureText(title) / 2f), 280f, paint)

        // Decorative line
        paint.strokeWidth = 4f
        canvas.drawLine(width/2f - 100f, 320f, width/2f + 100f, 320f, paint)

        // Content
        paint.color = Color.WHITE
        paint.textSize = 52f
        paint.isFakeBoldText = false
        paint.letterSpacing = 0f
        val padding = 120f
        val textWidth = width - (padding * 2)
        val lines = wrapText("“$content”", paint, textWidth)
        
        var y = 480f
        for (line in lines) {
            canvas.drawText(line, (width / 2f) - (paint.measureText(line) / 2f), y, paint)
            y += 85f
        }

        // Source
        if (source.isNotBlank()) {
            paint.color = Color.parseColor(theme.accentColor)
            paint.textSize = 42f
            paint.alpha = 200
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText("- $source -", width / 2f, y + 100f, paint)
        }

        saveAndShare(context, bitmap, "mikat_nur_paylasim.png")
    }

    private fun drawAtmosphericBackground(canvas: Canvas, width: Int, height: Int, theme: CardTheme) {
        // Gradient Background
        val gradient = LinearGradient(0f, 0f, 0f, height.toFloat(),
            intArrayOf(Color.parseColor(theme.bgStart), Color.parseColor(theme.bgEnd)),
            null, Shader.TileMode.CLAMP)
        val paint = Paint().apply { shader = gradient }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

        // Atmospheric Circles (Glowing effect)
        val circlePaint = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor(theme.accentColor)
            alpha = 15
        }
        canvas.drawCircle(0f, 0f, 500f, circlePaint)
        canvas.drawCircle(width.toFloat(), height.toFloat(), 400f, circlePaint)
        canvas.drawCircle(width / 2f, height / 2f, 300f, circlePaint)

        // Border
        paint.shader = null
        paint.color = Color.parseColor(theme.accentColor)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 15f
        paint.alpha = 80
        canvas.drawRect(50f, 50f, width - 50f, height - 50f, paint)
    }

    private fun saveAndShare(context: Context, bitmap: Bitmap, fileName: String) {
        try {
            val cachePath = File(context.cacheDir, "images")
            cachePath.mkdirs()
            val file = File(cachePath, fileName)
            val stream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.close()
            
            val contentUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Mîkat-ı Nur ile Paylaş"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun wrapText(text: String, paint: Paint, maxWidth: Float): List<String> {
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = StringBuilder()
        
        for (word in words) {
            val testLine = if (currentLine.isEmpty()) word else "${currentLine} $word"
            if (paint.measureText(testLine) <= maxWidth) {
                currentLine.append(if (currentLine.isEmpty()) "" else " ").append(word)
            } else {
                lines.add(currentLine.toString())
                currentLine = StringBuilder(word)
            }
        }
        if (currentLine.isNotEmpty()) lines.add(currentLine.toString())
        return lines
    }
}