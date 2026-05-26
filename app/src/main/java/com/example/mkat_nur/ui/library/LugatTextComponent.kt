package com.example.mkat_nur.ui.library

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.example.mkat_nur.R
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import java.util.regex.Pattern

@Composable
fun LugatTextComponent(
    kitapMetni: String,
    lugatMap: Map<String, String>,
    fontScale: Float,
    modifier: Modifier = Modifier,
    balloonBgColor: Color = Color(0xFFFFF9C4),
    textStyle: TextStyle = TextStyle(
        fontSize = 18.sp,
        lineHeight = 28.sp,
        color = Color.Black,
        textAlign = TextAlign.Justify,
        textDirection = TextDirection.ContentOrLtr
    )
) {
    var selectedWord by remember { mutableStateOf<String?>(null) }
    var wordMeaning by remember { mutableStateOf<String?>(null) }
    var showPopup by remember { mutableStateOf(false) }
    var popupOffset by remember { mutableStateOf(IntOffset(0, 0)) }
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

    val currentTextStyle = remember(textStyle, fontScale) {
        textStyle.copy(fontSize = textStyle.fontSize * fontScale, lineHeight = textStyle.lineHeight * fontScale)
    }

    val annotatedString = remember(kitapMetni, lugatMap) {
        processTextStable(kitapMetni, lugatMap)
    }

    Box(
        modifier = modifier.pointerInput(annotatedString) {
            detectTapGestures(
                onTap = { offset ->
                    textLayoutResult?.let { layout ->
                        val position = layout.getOffsetForPosition(offset)
                        annotatedString.getStringAnnotations(tag = "LUGAT", start = position, end = position)
                            .firstOrNull()?.let { annotation ->
                                val parts = annotation.item.split("|")
                                if (parts.size >= 2) {
                                    selectedWord = parts[0]
                                    wordMeaning = parts[1]
                                    popupOffset = IntOffset(offset.x.toInt(), offset.y.toInt() - 150)
                                    showPopup = true
                                }
                            }
                    }
                }
            )
        }
    ) {
        Text(
            text = annotatedString,
            style = currentTextStyle,
            onTextLayout = { textLayoutResult = it },
            modifier = Modifier.fillMaxWidth()
        )

        if (showPopup && selectedWord != null && wordMeaning != null) {
            Popup(
                offset = popupOffset,
                onDismissRequest = { showPopup = false },
                properties = PopupProperties(focusable = true)
            ) {
                LugatBalloonUI(word = selectedWord!!, meaning = wordMeaning!!, bgColor = balloonBgColor, onDismiss = { showPopup = false })
            }
        }
    }
}

private fun processTextStable(text: String, lugatMap: Map<String, String>): AnnotatedString {
    val arabicFont = FontFamily(Font(R.font.uthman_taha))
    val risaleRed = Color(0xFFB71C1C)
    val cleanedText = text.replace(Regex("\\[\\d+\\]"), "")

    return buildAnnotatedString {
        append(cleanedText)
        
        val besmeleRegex = "بِسْمِ\\s+اللّٰهِ\\s+الرَّحْمٰنِ\\s+الرَّح۪يمِ|بِسْمِ\\s+اللهِ\\s+الرَّحْمٰنِ\\s+الرَّحِيمِ"
        val besmeleMatcher = Pattern.compile(besmeleRegex).matcher(cleanedText)
        val besmeleRanges = mutableListOf<IntRange>()
        while (besmeleMatcher.find()) {
            val start = besmeleMatcher.start()
            val end = besmeleMatcher.end()
            besmeleRanges.add(start until end)
            addStyle(SpanStyle(fontFamily = arabicFont, color = risaleRed, fontSize = 1.6.em, fontWeight = FontWeight.Bold), start, end)
            addStyle(ParagraphStyle(textAlign = TextAlign.Center, textDirection = TextDirection.Rtl), start, end)
        }

        val arabicChars = "[\\u0600-\\u06FF\\u0750-\\u077F\\u0870-\\u089F\\u08A0-\\u08FF\\uFB50-\\uFDFF\\uFE70-\\uFEFF\u06DD\u06DE\u06DF\u06E0\u06E1\u06E2\u06E3\u06E4\u06E5\u06E6\u06E7\u06E8\u06E9\u06EA\u06EB\u06EC\u06ED\\*]+"
        val arabicMatcher = Pattern.compile(arabicChars).matcher(cleanedText)
        while (arabicMatcher.find()) {
            val start = arabicMatcher.start()
            val end = arabicMatcher.end()
            if (besmeleRanges.none { it.contains(start) }) {
                addStyle(SpanStyle(fontFamily = arabicFont, color = risaleRed, fontSize = 1.25.em), start, end)
            }
        }

        val arabicBlockMatcher = Pattern.compile("$arabicChars+(?:\\s+$arabicChars+)*").matcher(cleanedText)
        while (arabicBlockMatcher.find()) {
            val start = arabicBlockMatcher.start()
            val end = arabicBlockMatcher.end()
            if (besmeleRanges.none { it.contains(start) }) {
                addStyle(ParagraphStyle(textAlign = TextAlign.Right, textDirection = TextDirection.Rtl), start, end)
            }
        }

        if (lugatMap.isEmpty()) return@buildAnnotatedString
        val stopWords = setOf("ve", "ki", "ise", "için", "gibi", "olan", "bir", "her", "şey", "bizim", "fakat", "beştir")
        val sortedKeys = lugatMap.keys
            .filter { it.length > 3 && !it.any { c -> c in '\u0600'..'\u06FF' } && !stopWords.contains(it.lowercase()) }
            .sortedByDescending { it.length }
        
        val patternString = sortedKeys.joinToString("|") { Pattern.quote(it) }
        val pattern = Pattern.compile("(?i)(?<![\\wçğışöüÇĞİŞÖÜ-])$patternString(?![\\wçğışöüÇĞİŞÖÜ-])")
        val matcher = pattern.matcher(cleanedText)

        while (matcher.find()) {
            val start = matcher.start()
            val end = matcher.end()
            val matched = matcher.group()
            val key = sortedKeys.find { it.equals(matched, ignoreCase = true) } ?: continue
            val meaning = lugatMap[key] ?: ""

            matched.split(" ").forEach { sub ->
                if (sub.isNotEmpty()) {
                    val subStart = cleanedText.indexOf(sub, start)
                    if (subStart != -1 && subStart < end) {
                        addStyle(SpanStyle(textDecoration = TextDecoration.Underline), subStart, subStart + sub.length)
                    }
                }
            }
            addStringAnnotation("LUGAT", "$key|$meaning", start, end)
        }
    }
}

@Composable
private fun LugatBalloonUI(word: String, meaning: String, bgColor: Color, onDismiss: () -> Unit) {
    Card(modifier = Modifier.padding(8.dp).widthIn(max = 280.dp).shadow(12.dp, RoundedCornerShape(16.dp)).clickable { onDismiss() }, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = bgColor), elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = word, style = TextStyle(fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3E2723)))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = meaning, style = TextStyle(fontSize = 14.sp, lineHeight = 20.sp, color = Color(0xFF212121)))
        }
    }
}
