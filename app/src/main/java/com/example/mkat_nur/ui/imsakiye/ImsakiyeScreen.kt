package com.example.mkat_nur.ui.imsakiye

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mkat_nur.model.PrayerData
import com.example.mkat_nur.viewmodel.PrayerViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImsakiyeScreen(
    viewModel: PrayerViewModel,
    onMenuClick: () -> Unit
) {
    val allVakitler by viewModel.allVakitler.collectAsState()
    val highlightColor by viewModel.highlightColor.collectAsState()
    val listState = rememberLazyListState()
    val todayStr = remember { SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date()) }

    // Bugünün satırına otomatik kaydırma
    LaunchedEffect(allVakitler) {
        val todayIndex = allVakitler.indexOfFirst { it.date.readable == todayStr }
        if (todayIndex != -1) {
            listState.animateScrollToItem(todayIndex)
        }
    }

    val bgColors = listOf(Color(0xFF0D1B2A), Color(0xFF1B263B))

    Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(bgColors))) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("AYLIK İMSAKİYE", fontWeight = FontWeight.Black, color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = onMenuClick) { Icon(Icons.Default.Menu, null, tint = Color.White) }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { padding ->
            Column(modifier = Modifier.padding(padding).fillMaxSize()) {
                // Tablo Başlığı
                ImsakiyeHeader()

                if (allVakitler.isEmpty()) {
                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                        CircularProgressIndicator(color = Color.White)
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(allVakitler) { data ->
                            ImsakiyeRow(
                                data = data,
                                isToday = data.date.readable == todayStr,
                                highlightColor = Color(highlightColor)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ImsakiyeHeader() {
    Surface(
        color = Color.White.copy(alpha = 0.1f),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HeaderText("Tarih", Modifier.weight(1.5f))
            HeaderText("İms", Modifier.weight(1f))
            HeaderText("Gün", Modifier.weight(1f))
            HeaderText("Öğl", Modifier.weight(1f))
            HeaderText("İki", Modifier.weight(1f))
            HeaderText("Akş", Modifier.weight(1f))
            HeaderText("Yat", Modifier.weight(1f))
        }
    }
}

@Composable
fun ImsakiyeRow(data: PrayerData, isToday: Boolean, highlightColor: Color) {
    val bgColor = if (isToday) highlightColor.copy(alpha = 0.3f) else Color.Transparent
    val textColor = if (isToday) Color.White else Color.White.copy(alpha = 0.8f)
    val fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal

    Surface(
        color = bgColor,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 1.dp),
        shape = RoundedCornerShape(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = data.date.readable.substringBefore(".202"), // Yılı kısaltalım
                modifier = Modifier.weight(1.5f),
                color = textColor,
                fontSize = 11.sp,
                fontWeight = fontWeight,
                textAlign = TextAlign.Center
            )
            CellText(data.timings.fajr, Modifier.weight(1f), textColor, fontWeight)
            CellText(data.timings.sunrise, Modifier.weight(1f), textColor, fontWeight)
            CellText(data.timings.dhuhr, Modifier.weight(1f), textColor, fontWeight)
            CellText(data.timings.asr, Modifier.weight(1f), textColor, fontWeight)
            CellText(data.timings.maghrib, Modifier.weight(1f), textColor, fontWeight)
            CellText(data.timings.isha, Modifier.weight(1f), textColor, fontWeight)
        }
    }
}

@Composable
fun HeaderText(text: String, modifier: Modifier) {
    Text(
        text = text,
        modifier = modifier,
        color = Color.White,
        fontSize = 12.sp,
        fontWeight = FontWeight.Black,
        textAlign = TextAlign.Center
    )
}

@Composable
fun CellText(text: String, modifier: Modifier, color: Color, weight: FontWeight) {
    Text(
        text = text.substringBefore(" "),
        modifier = modifier,
        color = color,
        fontSize = 12.sp,
        fontWeight = weight,
        textAlign = TextAlign.Center
    )
}
