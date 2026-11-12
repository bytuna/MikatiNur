package com.example.mkat_nur.ui.kutuphane

import android.graphics.Bitmap
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.asImageBitmap
import kotlin.math.PI
import kotlin.math.sin

/**
 * Ahşap ve hamur kâğıt dokuları kodla üretilir; ekstra resim dosyası gerekmez.
 * Küçük bir "karo" bitmap'i oluşturulur ve tekrarlanarak (tile) boyanır.
 */
object WoodTextures {

    /** Açık renk ahşap: raf başlığı ve raf tahtası. */
    val light: Brush by lazy { woodBitmap(0xFFE9BE82.toInt(), contrast = 0.55f, seed = 7).toBrush() }

    /** Koyu renk ahşap: rafın arka duvarı. */
    val dark: Brush by lazy { woodBitmap(0xFF93592D.toInt(), contrast = 0.75f, seed = 21).toBrush() }

    /** Hamur kâğıt: okuyucu sayfaları. */
    val paper: Brush by lazy { paperBitmap(0xFFF1E2BA.toInt(), seed = 5).toBrush() }

    private fun Bitmap.toBrush(): Brush =
        ShaderBrush(ImageShader(asImageBitmap(), TileMode.Repeated, TileMode.Repeated))

    private fun woodBitmap(base: Int, contrast: Float, seed: Int, w: Int = 512, h: Int = 256): Bitmap {
        val rnd = java.util.Random(seed.toLong())
        val twoPi = (2 * PI).toFloat()
        val rowNoise = FloatArray(h) { rnd.nextFloat() * 2f - 1f }
        val smooth = FloatArray(h) { i ->
            (rowNoise[(i - 1 + h) % h] + rowNoise[i] * 2f + rowNoise[(i + 1) % h]) / 4f
        }
        val fine = FloatArray(h) { rnd.nextFloat() * 2f - 1f }
        val p1 = rnd.nextFloat() * twoPi
        val p2 = rnd.nextFloat() * twoPi
        val p3 = rnd.nextFloat() * twoPi
        val br = (base shr 16) and 0xFF
        val bg = (base shr 8) and 0xFF
        val bb = base and 0xFF
        val pixels = IntArray(w * h)
        for (y in 0 until h) {
            for (x in 0 until w) {
                val fx = x.toFloat() / w
                // Damarların dalgalanması (x yönünde periyodik -> yatayda dikişsiz)
                val warp = 5f * sin(twoPi * 2 * fx + p1) + 2.5f * sin(twoPi * 5 * fx + p2)
                val yy = y + warp
                val fy = yy / h
                val ring = sin(twoPi * (9 * fy + 0.15f * sin(twoPi * fx + p3)))
                val ring2 = sin(twoPi * (23 * fy))
                val idx = ((yy.toInt() % h) + h) % h
                val d = contrast * (0.10f * ring + 0.05f * ring2 + 0.10f * smooth[idx] + 0.04f * fine[idx]) +
                    (rnd.nextFloat() - 0.5f) * 0.04f
                val r = (br * (1f + d)).toInt().coerceIn(0, 255)
                val g = (bg * (1f + d)).toInt().coerceIn(0, 255)
                val b = (bb * (1f + d)).toInt().coerceIn(0, 255)
                pixels[y * w + x] = (0xFF shl 24) or (r shl 16) or (g shl 8) or b
            }
        }
        return Bitmap.createBitmap(pixels, w, h, Bitmap.Config.ARGB_8888)
    }

    private fun paperBitmap(base: Int, seed: Int, w: Int = 256, h: Int = 256): Bitmap {
        val rnd = java.util.Random(seed.toLong())
        val twoPi = (2 * PI).toFloat()
        val br = (base shr 16) and 0xFF
        val bg = (base shr 8) and 0xFF
        val bb = base and 0xFF
        val pixels = IntArray(w * h)
        for (y in 0 until h) {
            for (x in 0 until w) {
                val blotch = 0.010f * sin(twoPi * 2 * x / w) * sin(twoPi * 3 * y / h)
                val d = blotch + (rnd.nextFloat() - 0.5f) * 0.028f
                val r = (br * (1f + d)).toInt().coerceIn(0, 255)
                val g = (bg * (1f + d)).toInt().coerceIn(0, 255)
                val b = (bb * (1f + d)).toInt().coerceIn(0, 255)
                pixels[y * w + x] = (0xFF shl 24) or (r shl 16) or (g shl 8) or b
            }
        }
        return Bitmap.createBitmap(pixels, w, h, Bitmap.Config.ARGB_8888)
    }
}
