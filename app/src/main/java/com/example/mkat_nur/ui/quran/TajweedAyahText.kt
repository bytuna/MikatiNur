package com.example.mkat_nur.ui.quran

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.example.mkat_nur.R
import com.example.mkat_nur.model.TajweedAnnotation
import com.example.mkat_nur.repository.TajweedRepository
import com.example.mkat_nur.util.TajweedColorMapper
import com.example.mkat_nur.util.TajweedFilterSettings
import com.example.mkat_nur.util.TajweedRule

// Int sayı değerini Arapça Hint rakamlarına (١, ٢, ٣, ٤, ٥, ٦, ٧, ٨, ٩, ٠) dönüştürür
fun Int.toArabicNumerals(): String {
    val arabicDigits = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
    return this.toString().map { ch ->
        if (ch in '0'..'9') arabicDigits[ch - '0'] else ch
    }.joinToString("")
}

@Composable
fun TajweedAyahText(
    surahId: Int,
    ayahId: Int,
    rawText: String,
    modifier: Modifier = Modifier,
    activeRules: Set<TajweedRule> = TajweedRule.entries.toSet(),
    filterSettings: TajweedFilterSettings = TajweedFilterSettings(),
    fontSize: TextUnit = 28.sp,
    fontFamily: FontFamily = FontFamily(
        Font(R.font.scheherazade_new_regular, FontWeight.Normal),
        Font(R.font.scheherazade_new_medium, FontWeight.Medium),
        Font(R.font.scheherazade_new_semibold, FontWeight.SemiBold),
        Font(R.font.scheherazade_new_bold, FontWeight.Bold)
    ),
    textColor: Color = Color(0xFF2C1810)
) {
    val context = LocalContext.current
    var annotations by remember(surahId, ayahId) { mutableStateOf<List<TajweedAnnotation>>(emptyList()) }

    LaunchedEffect(surahId, ayahId) {
        annotations = TajweedRepository.getAnnotations(context, surahId, ayahId)
    }

    val annotatedText = remember(rawText, annotations, activeRules, filterSettings, textColor, ayahId) {
        buildAnnotatedString {
            val rawTextLen = rawText.length
            val arabicDigits = ayahId.toArabicNumerals()
            val ornamentStr = " \u06DD$arabicDigits"

            // 1. Ham Arapça Ayet Metni ve Mühür (\u06DD + Rakamlar) TEK BİR BİTİŞİK DİZİ Olarak Eklenir (OpenType Ligatür Bütünlüğü)
            append(rawText)
            append(ornamentStr)

            val fullTextLen = length

            // 2. Ham ayet metnine varsayılan ana rengi ata
            addStyle(SpanStyle(color = textColor), 0, rawTextLen)

            // 3. Tecvid Annotasyonlarını Yalnızca Ayet Metni İçinde (0..rawTextLen) Uygula
            val sortedAnnotations = annotations.sortedBy { ann ->
                if (TajweedColorMapper.getRuleType(ann.rule) == TajweedRule.SILENT) 0 else 1
            }

            for (ann in sortedAnnotations) {
                val color = TajweedColorMapper.getTajweedColor(ann.rule, activeRules, filterSettings)
                val ruleType = TajweedColorMapper.getRuleType(ann.rule)

                if (color != Color.Unspecified && ann.start in 0 until rawTextLen) {
                    val endPos = ann.end.coerceAtMost(rawTextLen)
                    addStyle(
                        style = SpanStyle(
                            color = color,
                            fontWeight = if (ruleType != TajweedRule.SILENT) FontWeight.Bold else FontWeight.Normal
                        ),
                        start = ann.start,
                        end = endPos
                    )
                }
            }

            // 4. Mühür ve Rakamlar TEK SPAN İçinde Kırmızı Renkle Stillendirilir (OpenType Fontun Rakamları Gülün İçine Ortalaması İçin)
            addStyle(
                style = SpanStyle(color = Color(0xFFB71C1C), fontWeight = FontWeight.Bold),
                start = rawTextLen,
                end = fullTextLen
            )
        }
    }

    Text(
        text = annotatedText,
        fontSize = fontSize,
        fontFamily = fontFamily,
        textAlign = TextAlign.Right,
        lineHeight = (fontSize.value * 1.6f).sp,
        softWrap = true,
        modifier = modifier.fillMaxWidth()
    )
}
