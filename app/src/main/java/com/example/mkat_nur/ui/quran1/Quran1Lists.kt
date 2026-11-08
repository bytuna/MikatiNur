package com.example.mkat_nur.ui.quran1

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mkat_nur.util.QuranUtils

private val P = Quran1Palette

@Composable
private fun Quran1ListRow(number: Int, title: String, subtitle: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = P.Paper,
        border = BorderStroke(1.dp, P.Gold.copy(alpha = 0.6f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(40.dp).border(1.5.dp, P.GoldDeep, CircleShape),
                contentAlignment = Alignment.Center
            ) { Text("$number", color = P.Emerald, fontWeight = FontWeight.Bold, fontSize = 13.sp) }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = P.Ink, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(subtitle, color = P.InkSoft, fontSize = 12.sp)
            }
        }
    }
}

/** "Son kalınan" kartı: DEVAM ET düğmesiyle kaldığın cüz/sayfaya döner. */
@Composable
private fun ContinueCard(title: String, detail: String, onContinue: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        color = P.Gold.copy(alpha = 0.15f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, P.Gold.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = P.Gold)
                Text(detail, fontSize = 12.sp, color = P.Paper, maxLines = 1)
            }
            Button(
                onClick = onContinue,
                colors = ButtonDefaults.buttonColors(containerColor = P.Gold),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                shape = RoundedCornerShape(8.dp)
            ) { Text("DEVAM ET", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = P.Ink) }
        }
    }
}

@Composable
fun Quran1JuzList(query: String, onOpen: (Int) -> Unit) {
    val lastJuz by Quran1HatimStore.lastJuz.collectAsState()
    val juzs = remember(query) { (1..30).filter { query.isBlank() || it.toString().contains(query.trim()) } }

    Column(modifier = Modifier.fillMaxSize()) {
        ContinueCard("Son Kalınan Cüz", "$lastJuz. Cüz") { onOpen(lastJuz) }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(juzs, key = { it }) { juz ->
                val range = QuranUtils.getJuzPageRange(juz)
                Quran1ListRow(juz, "$juz. Cüz", "Sayfa ${range.first} – ${range.second}") { onOpen(juz) }
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
fun Quran1PageList(query: String, onOpen: (Int) -> Unit) {
    val lastPage by Quran1HatimStore.lastPage.collectAsState()
    val pages = remember(query) { (1..604).filter { query.isBlank() || it.toString().contains(query.trim()) } }

    Column(modifier = Modifier.fillMaxSize()) {
        ContinueCard("Son Kalınan Sayfa", "$lastPage. Sayfa — ${QuranUtils.getJuzByPage(lastPage)}. Cüz") { onOpen(lastPage) }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(pages, key = { it }) { page ->
                Quran1ListRow(page, "$page. Sayfa", "${QuranUtils.getJuzByPage(page)}. Cüz") { onOpen(page) }
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}
