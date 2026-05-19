package com.example.mkat_nur.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp

/**
 * Mîkat-ı Nur Premium Okuma Motoru.
 * Arapça ve Türkçe metinleri profesyonel tipografi kurallarına göre işler.
 */
object RisaleTextEngine {

    private val ARABIC_COLOR = Color(0xFFD32F2F)

    fun buildPremiumText(
        content: String,
        arabicFont: FontFamily,
        serifFont: FontFamily,
        fontSize: Float
    ): AnnotatedString {
        return buildAnnotatedString {
            // Basit tagleri (h1, center, font) regex ile ayır
            val pattern = Regex("(<center>)|(</center>)|(<h1>)|(</h1>)|(<font color='#D32F2F'>)|(</font>)")
            val matches = pattern.findAll(content).toList()
            
            var lastIndex = 0
            var isArabic = false
            var isHeader = false
            var isCenter = false

            for (match in matches) {
                val plainText = content.substring(lastIndex, match.range.first)
                if (plainText.isNotEmpty()) {
                    applyStyle(plainText, isArabic, isHeader, isCenter, arabicFont, serifFont, fontSize)
                }

                when (match.value) {
                    "<font color='#D32F2F'>" -> isArabic = true
                    "</font>" -> isArabic = false
                    "<h1>" -> isHeader = true
                    "</h1>" -> isHeader = false
                    "<center>" -> isCenter = true
                    "</center>" -> isCenter = false
                }
                lastIndex = match.range.last + 1
            }

            if (lastIndex < content.length) {
                applyStyle(content.substring(lastIndex), isArabic, isHeader, isCenter, arabicFont, serifFont, fontSize)
            }
        }
    }

    private fun AnnotatedString.Builder.applyStyle(
        text: String,
        isArabic: Boolean,
        isHeader: Boolean,
        isCenter: Boolean,
        arabicFont: FontFamily,
        serifFont: FontFamily,
        fontSize: Float
    ) {
        if (isArabic) {
            // Eğer metin legacy (bozuk) kodlama içeriyorsa düzelt, yoksa olduğu gibi (Unicode) kullan
            val processedText = if (text.contains(Regex("[lmnrk]"))) {
                ArabicNormalizer.fixLegacyArabic(text)
            } else {
                text.trim()
            }

            withStyle(style = SpanStyle(
                color = ARABIC_COLOR,
                fontFamily = arabicFont,
                fontSize = (fontSize * 1.45f).sp,
                fontWeight = FontWeight.Normal,
                baselineShift = BaselineShift(0.15f)
            )) {
                append(processedText)
            }
        } else {
            withStyle(style = SpanStyle(
                fontFamily = serifFont,
                fontSize = (if (isHeader) fontSize * 1.5f else fontSize).sp,
                fontWeight = if (isHeader) FontWeight.Black else FontWeight.Normal,
                color = Color.Unspecified 
            )) {
                // Ayetleri belirginleştirmek için özel bir işlem yapabiliriz
                // Ama şimdilik düz metin olarak ekliyoruz
                append(text)
            }
        }
    }
}
