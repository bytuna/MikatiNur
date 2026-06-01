package com.example.mkat_nur.ui.risale

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.example.mkat_nur.util.RisaleTextEngine

@Composable
fun RisaleReaderComponent(
    content: String,
    hasiyeler: Map<String, String>,
    fontSize: Float,
    backgroundColor: Color,
    textColor: Color,
    arabicFont: FontFamily,
    scrollState: ScrollState = rememberScrollState(),
    scrollTarget: String? = null,
    onScrollTargetComplete: () -> Unit = {},
    onFootnoteClick: (String) -> Unit,
    onWordLongClick: (String) -> Unit
) {
    val annotatedText = RisaleTextEngine.buildRisaleText(
        content = content,
        hasiyeler = hasiyeler,
        arabicFont = arabicFont,
        fontSize = fontSize,
        onFootnoteClick = onFootnoteClick
    )

    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

    // Hedef başlığa kaydır
    LaunchedEffect(scrollTarget, textLayoutResult) {
        if (scrollTarget != null && textLayoutResult != null) {
            val index = annotatedText.text.indexOf(scrollTarget, ignoreCase = true)
            if (index != -1) {
                val line = textLayoutResult!!.getLineForOffset(index)
                val top = textLayoutResult!!.getLineTop(line)
                scrollState.animateScrollTo(top.toInt())
            }
            onScrollTargetComplete()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(16.dp)
    ) {
        SelectionContainer {
            Text(
                text = annotatedText,
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onLongPress = { offset ->
                                textLayoutResult?.let { layout ->
                                    val position = layout.getOffsetForPosition(offset)
                                    val wordRange = getWordBounds(annotatedText.text, position)
                                    if (wordRange.first < wordRange.last) {
                                        val word = annotatedText.text.substring(wordRange.first, wordRange.last).trim()
                                        onWordLongClick(word)
                                    }
                                }
                            }
                        )
                    },
                onTextLayout = { textLayoutResult = it }
            )
        }
    }
}

private fun getWordBounds(text: String, index: Int): IntRange {
    if (text.isEmpty() || index < 0 || index >= text.length) return 0..0
    var start = index
    while (start > 0 && !text[start - 1].isWhitespace() && text[start - 1] !in listOf('.', ',', '!', '?', ';', '(', ')', '"', '\n', ':', '[', ']')) {
        start--
    }
    var end = index
    while (end < text.length && !text[end].isWhitespace() && text[end] !in listOf('.', ',', '!', '?', ';', '(', ')', '"', '\n', ':', '[', ']')) {
        end++
    }
    return IntRange(start, end)
}
