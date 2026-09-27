package com.example.mkat_nur.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight

data class TajweedSpan(
    val start: Int,
    val end: Int,
    val rule: TajweedRule
)

object QuranTajweedColorizer {
    private val qalqalahChars = setOf('ق', 'ط', 'ب', 'ج', 'د')

    fun analyzeTajweed(
        arabicText: String,
        activeRules: Set<TajweedRule> = TajweedRule.entries.toSet()
    ): List<TajweedSpan> {
        val spans = mutableListOf<TajweedSpan>()
        val len = arabicText.length
        var i = 0

        while (i < len) {
            val ch = arabicText[i]

            when {
                // 1. Koyu Kırmızı (0xFFB71C1C): Medd-i Lazım (Madda ٓ + Shadda ّ veya Sukun ْ)
                ch == 'ٓ' && i + 1 < len && (arabicText[i + 1] == 'ّ' || arabicText[i + 1] == 'ْ') -> {
                    if (activeRules.contains(TajweedRule.MADD_LAZIM)) {
                        val start = (i - 1).coerceAtLeast(0)
                        spans.add(TajweedSpan(start, (i + 2).coerceAtMost(len), TajweedRule.MADD_LAZIM))
                    }
                }

                // 2. Kırmızı (0xFFE53935): 4-5 Hareke Uzatmalar (Madda ٓ + Hemze)
                ch == 'ٓ' -> {
                    if (activeRules.contains(TajweedRule.MADD_MUTTASIL_MUNFASIL)) {
                        val start = (i - 1).coerceAtLeast(0)
                        spans.add(TajweedSpan(start, (i + 1).coerceAtMost(len), TajweedRule.MADD_MUTTASIL_MUNFASIL))
                    }
                }

                // 3. Sarı (0xFFFDD835): 2 Hareke Normal Uzatma (Dagger Alif ٰ)
                ch == 'ٰ' -> {
                    if (activeRules.contains(TajweedRule.MADD_TABII)) {
                        val start = (i - 1).coerceAtLeast(0)
                        spans.add(TajweedSpan(start, (i + 1).coerceAtMost(len), TajweedRule.MADD_TABII))
                    }
                }

                // 4. Mavi / Yeşil: Small Meem (ۢ veya ۨ) İklab
                ch == 'ۢ' || ch == 'ۨ' -> {
                    if (activeRules.contains(TajweedRule.GHUNNAH_IHFA)) {
                        val start = (i - 1).coerceAtLeast(0)
                        spans.add(TajweedSpan(start, (i + 1).coerceAtMost(len), TajweedRule.GHUNNAH_IHFA))
                    }
                }

                // 5. Yeşil (0xFF4CAF50): İhfa ve Ğunne (Tenvin ً ٌ ٍ)
                ch == 'ً' || ch == 'ٌ' || ch == 'ٍ' -> {
                    if (activeRules.contains(TajweedRule.GHUNNAH_IHFA)) {
                        spans.add(TajweedSpan(i, (i + 1).coerceAtMost(len), TajweedRule.GHUNNAH_IHFA))
                    }
                }

                // 6. Açık Mavi (0xFF29B6F6): Kalkale (ق, ط, ب, ج, د + Sukun ْ)
                ch in qalqalahChars && i + 1 < len && arabicText[i + 1] == 'ْ' -> {
                    if (activeRules.contains(TajweedRule.QALQALAH)) {
                        spans.add(TajweedSpan(i, (i + 2).coerceAtMost(len), TajweedRule.QALQALAH))
                    }
                }

                // 7. Koyu Mavi (0xFF1E88E5): Kalın Râ (ر + Fatha َ veya Damma ُ)
                ch == 'ر' && i + 1 < len && (arabicText[i + 1] == 'َ' || arabicText[i + 1] == 'ُ') -> {
                    if (activeRules.contains(TajweedRule.HEAVY_RA)) {
                        spans.add(TajweedSpan(i, (i + 2).coerceAtMost(len), TajweedRule.HEAVY_RA))
                    }
                }

                // 8. Gri (0xFF9E9E9E): Okunmayan Harfler (Hamzat al-Wasl ٱ veya Silent Alif ۟)
                ch == 'ٱ' || ch == '۟' -> {
                    if (activeRules.contains(TajweedRule.SILENT)) {
                        spans.add(TajweedSpan(i, (i + 1).coerceAtMost(len), TajweedRule.SILENT))
                    }
                }
            }
            i++
        }

        return spans
    }

    fun buildTajweedAnnotatedString(
        arabicText: String,
        spans: List<TajweedSpan>
    ): AnnotatedString {
        return buildAnnotatedString {
            append(arabicText)
            for (span in spans) {
                addStyle(
                    style = SpanStyle(
                        color = span.rule.color,
                        fontWeight = if (span.rule != TajweedRule.SILENT) FontWeight.Bold else FontWeight.Normal
                    ),
                    start = span.start,
                    end = span.end.coerceAtMost(arabicText.length)
                )
            }
        }
    }

    fun colorize(
        arabicText: String,
        activeRules: Set<TajweedRule> = TajweedRule.entries.toSet()
    ): AnnotatedString {
        val spans = analyzeTajweed(arabicText, activeRules)
        return buildTajweedAnnotatedString(arabicText, spans)
    }
}
