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
 * Optimized for local JSON data with Markdown and HTML support.
 */
object RisaleTextEngine {

    private val ARABIC_COLOR = Color(0xFFB71C1C)
    private val FOOTNOTE_COLOR = Color(0xFF2E7D32)
    private val LINK_COLOR = Color(0xFFB71C1C)

    fun buildRisaleText(
        content: String,
        hasiyeler: Map<String, String>,
        arabicFont: FontFamily,
        fontSize: Float,
        onFootnoteClick: (String) -> Unit
    ): AnnotatedString {
        return buildAnnotatedString {
            val paragraphs = content.split("\n\n")
            
            paragraphs.forEach { paragraph ->
                val trimmed = paragraph.trim()
                if (trimmed.isEmpty()) return@forEach

                // Markdown Header Detection
                val isH1 = trimmed.startsWith("# ")
                val isH2 = trimmed.startsWith("## ")
                val isH3 = trimmed.startsWith("### ")
                
                val alignment = when {
                    isH1 || isH2 || isH3 -> TextAlign.Center
                    else -> TextAlign.Justify
                }

                withStyle(style = ParagraphStyle(textAlign = alignment)) {
                    val cleanText = trimmed
                        .removePrefix("# ").removePrefix("## ").removePrefix("### ")
                    
                    processRichText(
                        text = cleanText,
                        isHeader = isH1 || isH2 || isH3,
                        headerLevel = if (isH1) 1 else if (isH2) 2 else 3,
                        arabicFont = arabicFont,
                        fontSize = fontSize,
                        hasiyeler = hasiyeler,
                        onFootnoteClick = onFootnoteClick
                    )
                }
                append("\n\n")
            }
        }
    }

    private fun AnnotatedString.Builder.processRichText(
        text: String,
        isHeader: Boolean,
        headerLevel: Int,
        arabicFont: FontFamily,
        fontSize: Float,
        hasiyeler: Map<String, String>,
        onFootnoteClick: (String) -> Unit
    ) {
        // Handle <br/> tags first
        val preparedText = text.replace("<br/>", "\n").replace("<br>", "\n")

        // More flexible Regex for Arabic tags, Bold, and Footnotes
        val pattern = Regex("(<(p|span)[^>]*class=[\"']arabic[\"'][^>]*>)(.*?)(</\\2>)|(\\*\\*)(.*?)(\\*\\*)|(\\[\\^.*?\\])", RegexOption.DOT_MATCHES_ALL)
        
        var lastIndex = 0
        val matches = pattern.findAll(preparedText)

        for (match in matches) {
            // Append text before match
            val beforeText = preparedText.substring(lastIndex, match.range.first)
            if (beforeText.isNotEmpty()) {
                appendNormalText(beforeText, isHeader, headerLevel, fontSize)
            }

            val fullMatch = match.value
            when {
                // Arabic Paragraph or Span (Groups 1-4)
                match.groups[1] != null -> {
                    val arabicContent = match.groups[3]?.value ?: ""
                    // Clean internal HTML from Arabic content
                    val cleanArabic = arabicContent.replace(Regex("<[^>]*>"), "")
                    withStyle(SpanStyle(
                        fontFamily = arabicFont,
                        fontSize = (fontSize * 1.5f).sp,
                        color = ARABIC_COLOR,
                        baselineShift = BaselineShift(0.1f)
                    )) {
                        append(cleanArabic.trim())
                    }
                }
                // Bold (Groups 5-7)
                fullMatch.startsWith("**") -> {
                    val boldContent = match.groups[6]?.value ?: ""
                    withStyle(SpanStyle(fontWeight = FontWeight.Black)) {
                        append(boldContent)
                    }
                }
                // Footnote
                fullMatch.startsWith("[^") -> {
                    val footnoteKey = fullMatch
                    val footnoteContent = hasiyeler[footnoteKey]
                    
                    if (footnoteContent != null) {
                        val link = LinkAnnotation.Clickable(
                            tag = "FOOTNOTE",
                            styles = TextLinkStyles(
                                style = SpanStyle(
                                    color = LINK_COLOR,
                                    fontWeight = FontWeight.Bold,
                                    baselineShift = BaselineShift.Superscript,
                                    fontSize = (fontSize * 0.8f).sp
                                )
                            ),
                            linkInteractionListener = { onFootnoteClick(footnoteContent) }
                        )
                        pushLink(link)
                        append(fullMatch.replace("[^", "").replace("]", ""))
                        pop()
                    } else {
                        append(fullMatch)
                    }
                }
            }
            lastIndex = match.range.last + 1
        }

        // Append remaining text
        if (lastIndex < text.length) {
            appendNormalText(text.substring(lastIndex), isHeader, headerLevel, fontSize)
        }
    }

    private fun AnnotatedString.Builder.appendNormalText(
        text: String,
        isHeader: Boolean,
        headerLevel: Int,
        fontSize: Float
    ) {
        // Clean any remaining HTML tags in normal text
        val cleanText = text.replace(Regex("<[^>]*>"), "")
        if (cleanText.isEmpty()) return

        val style = if (isHeader) {
            SpanStyle(
                fontWeight = FontWeight.Black,
                fontSize = when(headerLevel) {
                    1 -> (fontSize * 1.5f).sp
                    2 -> (fontSize * 1.3f).sp
                    else -> (fontSize * 1.1f).sp
                },
                color = if (headerLevel == 1) ARABIC_COLOR else Color.Black
            )
        } else {
            SpanStyle(
                fontSize = fontSize.sp,
                color = Color(0xFF212121)
            )
        }
        withStyle(style) {
            append(text)
        }
    }
}
