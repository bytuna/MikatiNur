package com.example.mkat_nur.ui.library

import android.text.Html
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import com.example.mkat_nur.R
import java.util.regex.Pattern

object HtmlTextHelper {

    private val ARABIC_FONT = FontFamily(Font(R.font.uthman_taha))

    /**
     * HTML içeriğini AnnotatedString'e dönüştürür ve lügat kelimelerini tıklanabilir hale getirir.
     */
    fun parseHtmlWithDictionary(htmlContent: String?): AnnotatedString {
        if (htmlContent == null) return AnnotatedString("")
        val cleanHtml = htmlContent.replace("\n", "").replace("\r", "")
        
        // Basit bir regex ile <span anlam='...'> kelime </span> yapılarını buluyoruz.
        // Örnek: <span anlam='sonsuz'>ebedî</span>
        val pattern = Pattern.compile("<span anlam=['\"]([^'\"]*)['\"]>(.*?)</span>", Pattern.CASE_INSENSITIVE)
        val matcher = pattern.matcher(cleanHtml)
        
        return buildAnnotatedString {
            var lastIndex = 0
            while (matcher.find()) {
                // Eşleşme öncesindeki düz metni ekle (HTML etiketlerinden temizleyerek)
                val beforeMatch = cleanHtml.substring(lastIndex, matcher.start())
                append(Html.fromHtml(beforeMatch, Html.FROM_HTML_MODE_COMPACT).toString())
                
                val meaning = matcher.group(1) ?: ""
                val word = matcher.group(2) ?: ""
                
                // Lügat kelimesini özel stille ekle
                pushStringAnnotation(tag = "DICTIONARY", annotation = meaning)
                withStyle(
                    style = SpanStyle(
                        color = Color(0xFFB71C1C),
                        textDecoration = TextDecoration.Underline
                    )
                ) {
                    append(Html.fromHtml(word, Html.FROM_HTML_MODE_COMPACT).toString())
                }
                pop()
                
                lastIndex = matcher.end()
            }
            
            // Kalan metni ekle
            if (lastIndex < cleanHtml.length) {
                val remaining = cleanHtml.substring(lastIndex)
                append(Html.fromHtml(remaining, Html.FROM_HTML_MODE_COMPACT).toString())
            }
        }
    }
}
