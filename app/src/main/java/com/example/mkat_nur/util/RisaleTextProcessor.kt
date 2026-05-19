package com.example.mkat_nur.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp

object RisaleTextProcessor {

    private val ARABIC_COLOR = Color(0xFFD32F2F)

    fun parseRisaleContent(
        text: String,
        arabicFont: FontFamily,
        serifFont: FontFamily,
        baseFontSize: Float
    ): AnnotatedString {
        return buildAnnotatedString {
            val pattern = Regex("(<center>)|(</center>)|(<h1>)|(</h1>)|(<font color='#D32F2F'>)|(</font>)")
            val matches = pattern.findAll(text).toList()
            
            var lastIndex = 0
            var activeArabic = false
            var activeHeader = false
            var activeCenter = false

            for (match in matches) {
                val plainText = text.substring(lastIndex, match.range.first)
                if (plainText.isNotEmpty()) {
                    appendStyledSection(plainText, activeArabic, activeHeader, activeCenter, arabicFont, serifFont, baseFontSize)
                }

                when (match.value) {
                    "<font color='#D32F2F'>" -> activeArabic = true
                    "</font>" -> activeArabic = false
                    "<h1>" -> activeHeader = true
                    "</h1>" -> activeHeader = false
                    "<center>" -> activeCenter = true
                    "</center>" -> activeCenter = false
                }
                lastIndex = match.range.last + 1
            }

            if (lastIndex < text.length) {
                appendStyledSection(text.substring(lastIndex), activeArabic, activeHeader, activeCenter, arabicFont, serifFont, baseFontSize)
            }
        }
    }

    private fun AnnotatedString.Builder.appendStyledSection(
        text: String,
        isArabic: Boolean,
        isHeader: Boolean,
        isCenter: Boolean,
        arabicFont: FontFamily,
        serifFont: FontFamily,
        baseFontSize: Float
    ) {
        if (isArabic) {
            val processed = if (text.contains(Regex("[lmn]"))) {
                 ArabicNormalizer.fixLegacyArabic(text)
            } else {
                text
            }
            
            withStyle(style = SpanStyle(
                color = ARABIC_COLOR,
                fontFamily = arabicFont,
                fontSize = (baseFontSize * 1.5f).sp,
                fontWeight = FontWeight.Normal,
                baselineShift = BaselineShift(0.1f)
            )) {
                append(processed)
            }
        } else {
            withStyle(style = SpanStyle(
                fontFamily = serifFont,
                fontSize = (if (isHeader) baseFontSize * 1.5f else baseFontSize).sp,
                fontWeight = if (isHeader) FontWeight.Black else FontWeight.Normal
            )) {
                append(text)
            }
        }
    }
}
