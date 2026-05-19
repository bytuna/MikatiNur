package com.example.mkat_nur.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp

/**
 * Mîkat-ı Nur Senior Typography Engine.
 * Optimized for footnote detection and modern Jetpack Compose interactions.
 */
object RisaleTextEngine {

    private val ARABIC_COLOR_HEX = "#B71C1C"
    private val ARABIC_COLOR = Color(0xFFB71C1C)
    private val FOOTNOTE_COLOR = Color(0xFF2E7D32)
    private val LINK_COLOR = Color(0xFFB71C1C)
    
    // Başlıkları yakalamak için pattern (Genişletildi: Nükte, Vecih vb. eklendi)
    private val AUTO_HEADER_PATTERN = Regex("^(Birinci|İkinci|Üçüncü|Dördüncü|Beşinci|Altıncı|Yedinci|Sekizinci|Dokuzuncu|Onuncu|On Birinci|On İkinci|On Üçüncü|On Dördüncü|On Beşinci|On Altıncı|On Yedinci|On Sekizinci|On Dokuzuncu|Yirminci|Otuzuncu|BİRİNCİ|İKİNCİ|ÜÇÜNCÜ|DÖRDÜNCÜ|BEŞİNCİ|ALTINCI|YEDİNCİ|SEKİZİNCİ|DOKUZUNCU|ONUNCU) (Söz|Mektub|Lem'a|Şua|Nükte|Mesele|Zeyl|Makam|Sır|Kelime|Hakikat|Sual|Cevap|İşaret|Vecih|İhtar|Münacat).*", RegexOption.IGNORE_CASE)

    // Alt başlıkları yakalamak için pattern (Kırmızı yapılacaklar - Metin içindekiler)
    private val SUB_HEADER_PATTERN = Regex("(?i)(Birinci|İkinci|Üçüncü|Dördüncü|Beşinci|Altıncı|Yedinci|Sekizinci|Dokuzuncu|Onuncu) (Makam|Sır|Nükte|Mesele|Rükün|Esas|Lem'a|Dal|Işık|Şua|Mertebe|Kelime|Fıkra|Hakikat|Basamak|Sual|Cevap|İşaret|Remiz|Telvih|İma|Vecih|İhtar)")

    // Soru-Cevap ve Özel İsim vurguları (Siyah ve Kalın yapılacaklar)
    private val BLACK_BOLD_PATTERN = Regex("(?i)\\b(SUAL|CEVAP|ELCEVAP|Soru|Cevab|Said Nursi|Bediüzzaman|Üstad|Risale-i Nur|Müellif)\\b[:.]?")

    fun buildPremiumText(
        content: String,
        arabicFont: FontFamily,
        serifFont: FontFamily,
        fontSize: Float,
        onFootnoteClick: (String) -> Unit
    ): AnnotatedString {
        val (mainText, footnoteMap) = extractFootnotes(content)
        
        return buildAnnotatedString {
            val processedContent = mainText.replace("<br>", "\n").replace("<br/>", "\n")
            val paragraphs = processedContent.split("\n")
            
            paragraphs.forEach { paragraph ->
                val trimmed = paragraph.trim()
                if (trimmed == "ba" || trimmed.isEmpty()) return@forEach

                val isExplicitHeader = paragraph.contains("<h1>")
                val isAutoHeader = AUTO_HEADER_PATTERN.matches(trimmed)
                val isCenter = paragraph.contains("<center>")
                
                val alignment = when {
                    isExplicitHeader || isAutoHeader || isCenter -> TextAlign.Center
                    else -> TextAlign.Justify
                }

                withStyle(style = ParagraphStyle(textAlign = alignment)) {
                    val cleanParagraph = paragraph
                        .replace("<h1>", "").replace("</h1>", "")
                        .replace("<center>", "").replace("</center>", "")

                    processStyles(
                        text = cleanParagraph,
                        isHeader = isExplicitHeader || isAutoHeader,
                        isCenter = isCenter,
                        arabicFont = arabicFont,
                        serifFont = serifFont,
                        fontSize = fontSize,
                        footnoteMap = footnoteMap,
                        onFootnoteClick = onFootnoteClick
                    )
                }
            }
        }
    }

    /**
     * Extracts footnotes from the content.
     * Looks for the "ba" separator or matches markers at the end.
     */
    private fun extractFootnotes(content: String): Pair<String, Map<Int, String>> {
        val footnoteMap = mutableMapOf<Int, String>()
        var mainBody = content

        // Case 1: Standard "ba" separator
        if (content.contains("\nba\n") || content.contains("\nba ")) {
            val parts = content.split(Regex("\\nba(\\n|\\s)"), limit = 2)
            mainBody = parts[0]
            val footnoteSection = parts.getOrNull(1) ?: ""
            
            val lines = footnoteSection.split("\n").filter { it.trim().isNotEmpty() }
            lines.forEachIndexed { index, line ->
                footnoteMap[index + 1] = line.trim()
            }
        } else {
            // Case 2: Heuristic - look for lines at the end that look like footnotes
            val lines = content.split("\n")
            val markerRegex = Regex("\\[(\\d+)\\]")
            
            // Count total markers in the whole text
            val totalMarkers = markerRegex.findAll(content).map { it.groupValues[1].toInt() }.toList().distinct().size
            
            if (totalMarkers > 0) {
                val potentialFootnoteLines = mutableListOf<String>()
                var i = lines.size - 1
                while (i >= 0 && potentialFootnoteLines.size < totalMarkers) {
                    val line = lines[i].trim()
                    // Footnotes usually are short or start with a reference, or they are just the last few lines
                    if (line.isNotEmpty() && line != "ba") {
                        potentialFootnoteLines.add(0, line)
                    }
                    i--
                }
                
                if (potentialFootnoteLines.size == totalMarkers) {
                    potentialFootnoteLines.forEachIndexed { index, line ->
                        footnoteMap[index + 1] = line
                    }
                    mainBody = lines.take(i + 1).joinToString("\n")
                }
            }
        }

        return mainBody to footnoteMap
    }

    private fun AnnotatedString.Builder.processStyles(
        text: String,
        isHeader: Boolean,
        isCenter: Boolean,
        arabicFont: FontFamily,
        serifFont: FontFamily,
        fontSize: Float,
        footnoteMap: Map<Int, String>,
        onFootnoteClick: (String) -> Unit
    ) {
        // Renk etiketlerini (hem #B71C1C hem de diğerleri), kalınlık ve dipnot etiketlerini yakala
        val pattern = Regex("(<font color='([^']+)'>)|(</font>)|(\\{\\{)|(\\}\\})|(<b>)|(</b>)|(\\[\\d+\\])")
        val matches = pattern.findAll(text).toList()
        
        var lastIndex = 0
        var activeTagColor: Color? = null
        var isFootnote = false
        var isBold = false

        for (match in matches) {
            val plainText = text.substring(lastIndex, match.range.first)
            if (plainText.isNotEmpty()) {
                if (activeTagColor != null) {
                    // Eğer aktif bir renk etiketi varsa (kırmızı veya siyah fark etmez), o rengi uygula
                    // Arapça fontu hala otomatik algılanacak
                    appendForcedColorSpan(plainText, activeTagColor, isHeader, isCenter, isFootnote, isBold, arabicFont, serifFont, fontSize)
                } else {
                    // Etiket yoksa akıllı algılama yap (Özel isimler, Arapça vb.)
                    appendAutoDetectedArabicSpan(plainText, isHeader, isCenter, isFootnote, isBold, arabicFont, serifFont, fontSize)
                }
            }

            val fullMatch = match.value
            when {
                fullMatch.startsWith("<font") -> {
                    val colorHex = match.groups[2]?.value ?: ARABIC_COLOR_HEX
                    activeTagColor = try { Color(android.graphics.Color.parseColor(colorHex)) } catch(e: Exception) { ARABIC_COLOR }
                }
                fullMatch == "</font>" -> activeTagColor = null
                fullMatch == "{{" -> isFootnote = true
                fullMatch == "}}" -> isFootnote = false
                fullMatch == "<b>" -> isBold = true
                fullMatch == "</b>" -> isBold = false
                fullMatch.startsWith("[") && fullMatch.endsWith("]") -> {
                    val num = fullMatch.substring(1, fullMatch.length - 1).toIntOrNull()
                    val footnoteContent = num?.let { footnoteMap[it] }
                    
                    if (footnoteContent != null) {
                        val link = LinkAnnotation.Clickable(
                            tag = "FOOTNOTE",
                            styles = TextLinkStyles(
                                style = SpanStyle(
                                    color = LINK_COLOR,
                                    fontWeight = FontWeight.Black,
                                    baselineShift = BaselineShift.Superscript,
                                    fontSize = (fontSize * 0.9f).sp
                                )
                            ),
                            linkInteractionListener = { onFootnoteClick(footnoteContent) }
                        )
                        pushLink(link)
                        append(fullMatch)
                        pop()
                    } else {
                        append(fullMatch)
                    }
                }
            }
            lastIndex = match.range.last + 1
        }

        if (lastIndex < text.length) {
            val remainingText = text.substring(lastIndex)
            if (activeTagColor != null) {
                appendForcedColorSpan(remainingText, activeTagColor, isHeader, isCenter, isFootnote, isBold, arabicFont, serifFont, fontSize)
            } else {
                appendAutoDetectedArabicSpan(remainingText, isHeader, isCenter, isFootnote, isBold, arabicFont, serifFont, fontSize)
            }
        }
    }

    private fun AnnotatedString.Builder.appendForcedColorSpan(
        text: String,
        forcedColor: Color,
        isHeader: Boolean,
        isCenter: Boolean,
        isFootnote: Boolean,
        isBold: Boolean,
        arabicFont: FontFamily,
        serifFont: FontFamily,
        fontSize: Float
    ) {
        // Zorunlu renk olsa bile Arapça fontunu karakter bazlı korumalıyız
        var currentSegment = StringBuilder()
        var currentIsArabic = isArabicChar(text[0])

        for (char in text) {
            val isArabic = isArabicChar(char)
            if (isArabic == currentIsArabic) {
                currentSegment.append(char)
            } else {
                applyForcedStyle(currentSegment.toString(), forcedColor, currentIsArabic, isHeader, isCenter, isFootnote, isBold, arabicFont, serifFont, fontSize)
                currentSegment = StringBuilder().append(char)
                currentIsArabic = isArabic
            }
        }
        if (currentSegment.isNotEmpty()) {
            applyForcedStyle(currentSegment.toString(), forcedColor, currentIsArabic, isHeader, isCenter, isFootnote, isBold, arabicFont, serifFont, fontSize)
        }
    }

    private fun AnnotatedString.Builder.applyForcedStyle(
        text: String,
        color: Color,
        isArabic: Boolean,
        isHeader: Boolean,
        isCenter: Boolean,
        isFootnote: Boolean,
        isBold: Boolean,
        arabicFont: FontFamily,
        serifFont: FontFamily,
        fontSize: Float
    ) {
        val style = SpanStyle(
            color = color,
            fontFamily = if (isArabic) arabicFont else serifFont,
            fontSize = when {
                isArabic && isHeader -> (fontSize * 1.6f).sp
                isArabic -> (fontSize * 1.4f).sp
                isHeader -> (fontSize * 1.5f).sp
                else -> fontSize.sp
            },
            fontWeight = if (isHeader || isBold) FontWeight.Black else FontWeight.Normal,
            fontStyle = if (isCenter || isFootnote) FontStyle.Italic else FontStyle.Normal
        )
        withStyle(style) { append(text) }
    }

    private fun AnnotatedString.Builder.appendAutoDetectedArabicSpan(
        text: String,
        isHeader: Boolean,
        isCenter: Boolean,
        isFootnote: Boolean,
        isBold: Boolean,
        arabicFont: FontFamily,
        serifFont: FontFamily,
        fontSize: Float
    ) {
        if (text.isEmpty()) return
        
        // Eğer zaten bir BAŞLIK içindeysek, alt-vurgu (SUB_HEADER_PATTERN) aramamıza gerek yok.
        // Bu, 1. resimdeki boyut dengesizliğini (On Dördüncü Lem'a kısmının küçülmesini) önler.
        if (isHeader) {
            processArabicAndNormalText(text, isHeader, isCenter, isFootnote, isBold, arabicFont, serifFont, fontSize)
            return
        }
        
        // Önce özel vurguları (Sual, Cevap, İkinci Makam vb.) tespit edelim
        val combinedPattern = Regex("(${SUB_HEADER_PATTERN.pattern})|(${BLACK_BOLD_PATTERN.pattern})")
        val matches = combinedPattern.findAll(text).toList()
        
        if (matches.isEmpty()) {
            // Özel vurgu yoksa normal Arapça algılama yap
            processArabicAndNormalText(text, isHeader, isCenter, isFootnote, isBold, arabicFont, serifFont, fontSize)
            return
        }

        var lastIndex = 0
        for (match in matches) {
            // Eşleşmeden önceki kısmı işle
            val beforeText = text.substring(lastIndex, match.range.first)
            if (beforeText.isNotEmpty()) {
                processArabicAndNormalText(beforeText, isHeader, isCenter, isFootnote, isBold, arabicFont, serifFont, fontSize)
            }

            val matchText = match.value
            when {
                // İkinci Makam, Üçüncü Sır vb. -> Kırmızı
                SUB_HEADER_PATTERN.matches(matchText) -> {
                    withStyle(SpanStyle(color = ARABIC_COLOR, fontWeight = FontWeight.Bold, fontFamily = serifFont, fontSize = fontSize.sp)) {
                        append(matchText)
                    }
                }
                // SUAL, CEVAP, Said Nursi vb. -> Siyah Kalın
                BLACK_BOLD_PATTERN.matches(matchText) -> {
                    withStyle(SpanStyle(color = Color.Black, fontWeight = FontWeight.Black, fontFamily = serifFont, fontSize = fontSize.sp)) {
                        append(matchText)
                    }
                }
            }
            lastIndex = match.range.last + 1
        }

        // Kalan kısmı işle
        if (lastIndex < text.length) {
            processArabicAndNormalText(text.substring(lastIndex), isHeader, isCenter, isFootnote, isBold, arabicFont, serifFont, fontSize)
        }
    }

    private fun AnnotatedString.Builder.processArabicAndNormalText(
        text: String,
        isHeader: Boolean,
        isCenter: Boolean,
        isFootnote: Boolean,
        isBold: Boolean,
        arabicFont: FontFamily,
        serifFont: FontFamily,
        fontSize: Float
    ) {
        var currentSegment = StringBuilder()
        var currentIsArabic = isArabicChar(text[0])

        for (char in text) {
            val isArabic = isArabicChar(char)
            if (isArabic == currentIsArabic) {
                currentSegment.append(char)
            } else {
                appendStyledSpan(currentSegment.toString(), currentIsArabic, isHeader, isCenter, isFootnote, isBold, arabicFont, serifFont, fontSize)
                currentSegment = StringBuilder().append(char)
                currentIsArabic = isArabic
            }
        }
        if (currentSegment.isNotEmpty()) {
            appendStyledSpan(currentSegment.toString(), currentIsArabic, isHeader, isCenter, isFootnote, isBold, arabicFont, serifFont, fontSize)
        }
    }

    private fun isArabicChar(c: Char): Boolean {
        return c in '\u0600'..'\u06FF' || 
               c in '\u0750'..'\u077F' || 
               c in '\u08A0'..'\u08FF' || 
               c in '\uFB50'..'\uFDFF' || 
               c in '\uFE70'..'\uFEFF' ||
               c == '\u200F' // Right-to-left mark
    }

    private fun AnnotatedString.Builder.appendStyledSpan(
        text: String,
        isArabic: Boolean,
        isHeader: Boolean,
        isCenter: Boolean,
        isFootnote: Boolean,
        isBold: Boolean,
        arabicFont: FontFamily,
        serifFont: FontFamily,
        fontSize: Float
    ) {
        val style = when {
            isArabic -> SpanStyle(
                color = ARABIC_COLOR,
                fontFamily = arabicFont,
                fontSize = (if (isHeader) fontSize * 1.7f else fontSize * 1.5f).sp,
                baselineShift = BaselineShift(0.1f)
            )
            isHeader -> {
                // Ana başlıklar (Söz, Lem'a, Mektub, Şua) Kırmızı kalsın
                // Alt başlıklar (Makam, Sır, Hakikat vb.) Siyah ve Kalın olsun
                val isMainBookHeader = text.contains(Regex("Söz|Mektub|Lem'a|Şua|Zeyl|Nükte|Mesele", RegexOption.IGNORE_CASE))
                SpanStyle(
                    color = if (isMainBookHeader) ARABIC_COLOR else Color.Black,
                    fontFamily = serifFont,
                    fontSize = (fontSize * 1.6f).sp,
                    fontWeight = FontWeight.Black
                )
            }
            isCenter -> SpanStyle(
                fontFamily = serifFont,
                fontSize = (fontSize * 1.1f).sp,
                fontStyle = FontStyle.Normal,
                fontWeight = FontWeight.Bold,
                color = Color.DarkGray
            )
            isFootnote -> SpanStyle(
                color = FOOTNOTE_COLOR,
                fontFamily = serifFont,
                fontSize = (fontSize * 0.95f).sp,
                fontStyle = FontStyle.Italic
            )
            isBold -> SpanStyle(
                fontWeight = FontWeight.Black,
                fontFamily = serifFont,
                fontSize = fontSize.sp,
                color = Color.Black
            )
            else -> SpanStyle(
                fontFamily = serifFont,
                fontSize = fontSize.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0xFF212121) // Tam siyah yerine çok koyu gri (daha okunaklı)
            )
        }
        withStyle(style) { append(text) }
    }
}
