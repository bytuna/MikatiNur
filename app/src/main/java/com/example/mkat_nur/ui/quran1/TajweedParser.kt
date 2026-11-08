package com.example.mkat_nur.ui.quran1

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle

/**
 * AlQuran Cloud "quran-tajweed" metnini renkli AnnotatedString'e çevirir.
 *
 * Sözdizimi:  [kural:parametre[metin]   örn.  [h:1[ٱ]للَّهِ   veya  [q[ْ]
 * Kural harfleri alquran.cloud/tajweed-guide sayfasındaki listeye göredir.
 */
object TajweedParser {

    data class TajweedRule(val code: Char, val label: String, val color: Color)

    private val GREY = Color(0xFFAAAAAA)

    val rules: List<TajweedRule> = listOf(
        TajweedRule('h', "Hemze-i Vasl", GREY),
        TajweedRule('s', "Okunmayan harf", GREY),
        TajweedRule('l', "Lâm-ı Şemsiyye", GREY),
        TajweedRule('n', "Med (Tabiî)", Color(0xFF537FFF)),
        TajweedRule('p', "Med (Arız)", Color(0xFF4050FF)),
        TajweedRule('m', "Med (Lâzım)", Color(0xFF000EBC)),
        TajweedRule('o', "Med (Muttasıl/Munfasıl)", Color(0xFF2144C1)),
        TajweedRule('q', "Kalkale", Color(0xFFDD0008)),
        TajweedRule('g', "Gunne", Color(0xFFFF7E1E)),
        TajweedRule('f', "İhfa", Color(0xFF9400A8)),
        TajweedRule('c', "İhfa-i Şefevî", Color(0xFFD500B7)),
        TajweedRule('i', "İklab", Color(0xFF26BFFD)),
        TajweedRule('w', "İdgam-ı Şefevî", Color(0xFF169777)),
        TajweedRule('a', "İdgam (Gunneli)", Color(0xFF169777)),
        TajweedRule('u', "İdgam (Gunnesiz)", Color(0xFF169777)),
        TajweedRule('d', "İdgam-ı Mütecaniseyn", Color(0xFFA1A1A1)),
        TajweedRule('b', "İdgam-ı Mütekaribeyn", Color(0xFFA1A1A1))
    )

    private val colorByCode: Map<Char, Color> = rules.associate { it.code to it.color }

    // [h:1[ٱ]  veya  [q[ْ]
    private val pattern = Regex("""\[([a-z])(?::\d+)?\[([^\]]*)]""")

    fun parse(raw: String, baseColor: Color): AnnotatedString = buildAnnotatedString {
        var last = 0
        for (m in pattern.findAll(raw)) {
            if (m.range.first > last) {
                withStyle(SpanStyle(color = baseColor)) { append(raw.substring(last, m.range.first)) }
            }
            val code = m.groupValues[1][0]
            val text = m.groupValues[2]
            val color = colorByCode[code] ?: baseColor
            withStyle(SpanStyle(color = color)) { append(text) }
            last = m.range.last + 1
        }
        if (last < raw.length) {
            withStyle(SpanStyle(color = baseColor)) { append(raw.substring(last)) }
        }
    }

    /** İşaretleri kaldırıp düz metin döndürür (kopyalama/paylaşma için). */
    fun strip(raw: String): String = pattern.replace(raw) { it.groupValues[2] }
}
