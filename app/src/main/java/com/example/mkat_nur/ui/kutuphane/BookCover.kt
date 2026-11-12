package com.example.mkat_nur.ui.kutuphane

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private data class CoverState(val bitmap: ImageBitmap?, val done: Boolean)

/**
 * assets/covers altındaki kapak resmini gösterir.
 * Resim yoksa kitap adıyla otomatik, altın çerçeveli bir kapak çizer.
 */
@Composable
fun BookCover(book: Book, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val widthPx = with(LocalDensity.current) { 120.dp.roundToPx() }
    val state by produceState(CoverState(null, false), book.coverPath) {
        val bmp = withContext(Dispatchers.IO) { Repository.loadCover(context, book.coverPath, widthPx) }
        value = CoverState(bmp, true)
    }
    Box(modifier) {
        val bmp = state.bitmap
        when {
            bmp != null -> Image(
                bitmap = bmp,
                contentDescription = book.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
            state.done -> GeneratedCover(book.title)
            else -> Box(Modifier.fillMaxSize().background(Color(0x33000000)))
        }
    }
}

@Composable
private fun GeneratedCover(title: String) {
    val palette = listOf(0xFF7A1418, 0xFF0F3D22, 0xFF4A2A12, 0xFF5A1A3A, 0xFF1B2F5B)
    val bg = Color(palette[(title.hashCode() and 0x7FFFFFFF) % palette.size])
    val gold = Color(0xFFE2BC6A)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bg, RoundedCornerShape(2.dp))
            .drawBehind {
                val o = 6.dp.toPx()
                drawRect(
                    gold,
                    topLeft = Offset(o, o),
                    size = Size(size.width - 2 * o, size.height - 2 * o),
                    style = Stroke(1.5.dp.toPx()),
                )
                val o2 = o + 3.dp.toPx()
                drawRect(
                    gold.copy(alpha = 0.6f),
                    topLeft = Offset(o2, o2),
                    size = Size(size.width - 2 * o2, size.height - 2 * o2),
                    style = Stroke(1.dp.toPx()),
                )
            }
            .padding(14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = title,
            color = gold,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            lineHeight = 15.sp,
            textAlign = TextAlign.Center,
            maxLines = 5,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
