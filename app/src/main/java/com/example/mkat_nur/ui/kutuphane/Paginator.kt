package com.example.mkat_nur.ui.kutuphane

import android.graphics.Paint
import android.graphics.Typeface
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

/**
 * Düz metni, verilen genişlik/yükseklik ve yazı boyutuna göre sayfalara böler.
 *
 * Sonuç, her sayfanın metindeki BAŞLANGIÇ konumlarını (karakter indeksi) içeren bir dizidir.
 * Okuma konumu bu indeks olarak saklandığı için yazı boyutu değişse bile
 * kullanıcı kaldığı yerden devam eder.
 */
object Paginator {

    fun paint(textSizePx: Float): TextPaint =
        TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = textSizePx
            color = 0xFF2B1B0E.toInt()
            typeface = Typeface.SERIF
        }

    /** Hem sayfalama hem çizim aynı düzeni kullansın diye tek yerde tanımlı. */
    fun buildLayout(text: CharSequence, paint: TextPaint, width: Int): StaticLayout {
        val builder = StaticLayout.Builder.obtain(text, 0, text.length, paint, width)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(0f, 1.3f)
            .setIncludePad(false)
            .setBreakStrategy(Layout.BREAK_STRATEGY_SIMPLE)
            .setHyphenationFrequency(Layout.HYPHENATION_FREQUENCY_NONE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD)
        }
        return builder.build()
    }

    suspend fun paginate(text: String, widthPx: Int, heightPx: Int, paint: TextPaint): IntArray {
        val n = text.length
        if (n == 0) return intArrayOf(0)
        val starts = ArrayList<Int>()
        var pos = 0
        var guess = 2500 // bir sayfaya sığacak tahmini karakter sayısı (kendini ayarlar)

        while (pos < n) {
            currentCoroutineContext().ensureActive()
            // Sayfa başındaki boş satırları atla
            while (pos < n && text[pos] == '\n') pos++
            if (pos >= n) break
            starts.add(pos)

            var take = guess
            var consumed = 0
            while (true) {
                val end = minOf(n, pos + take)
                val segment = text.substring(pos, end)
                val layout = buildLayout(segment, paint, widthPx)
                var fit = 0
                while (fit < layout.lineCount && layout.getLineBottom(fit) <= heightPx) fit++
                if (fit == 0) fit = 1 // en az bir satır: sonsuz döngüye karşı güvence
                if (fit < layout.lineCount || end == n) {
                    consumed = layout.getLineEnd(fit - 1)
                    break
                }
                take *= 2 // parça sayfayı doldurmadı, daha fazla metin al
            }
            if (consumed <= 0) consumed = 1
            pos += consumed
            guess = (consumed * 1.4f).toInt().coerceAtLeast(500)
        }
        return starts.toIntArray()
    }

    /** Verilen karakter konumunu içeren sayfanın indeksi. */
    fun pageFor(starts: IntArray, offset: Int): Int {
        var lo = 0
        var hi = starts.size - 1
        var ans = 0
        while (lo <= hi) {
            val mid = (lo + hi) ushr 1
            if (starts[mid] <= offset) {
                ans = mid
                lo = mid + 1
            } else {
                hi = mid - 1
            }
        }
        return ans
    }
}
