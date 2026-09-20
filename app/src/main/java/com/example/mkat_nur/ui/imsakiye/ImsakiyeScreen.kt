package com.example.mkat_nur.ui.imsakiye

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.example.mkat_nur.ui.theme.LocalAppThemeColors
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
    val listState = rememberLazyListState()
    val todayStr = remember { SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date()) }

    // Bugünün satırına otomatik kaydırma
    LaunchedEffect(allVakitler) {
        val todayIndex = allVakitler.indexOfFirst { it.date.readable == todayStr }
        if (todayIndex != -1) {
            listState.animateScrollToItem(todayIndex)
        }
    }

    val themeColors = LocalAppThemeColors.current
    val bgColors = themeColors.gradientColors

    Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(bgColors))) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("AYLIK İMSAKİYE", fontWeight = FontWeight.Black, color = themeColors.textPrimary) },
                    navigationIcon = {
                        IconButton(onClick = onMenuClick) { Icon(Icons.Default.Menu, null, tint = themeColors.textPrimary) }
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
                        CircularProgressIndicator(color = themeColors.primary)
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
                                isToday = data.date.readable == todayStr
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
    val themeColors = LocalAppThemeColors.current
    Surface(
        color = themeColors.surface,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, themeColors.cardBorder)
    ) {
        Row(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
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
fun ImsakiyeRow(data: PrayerData, isToday: Boolean) {
    val themeColors = LocalAppThemeColors.current
    val bgColor = if (isToday) themeColors.accent.copy(alpha = 0.20f) else themeColors.surface
    val borderColor = if (isToday) themeColors.accent else themeColors.cardBorder.copy(alpha = 0.5f)
    val textColor = if (isToday) themeColors.accent else themeColors.textPrimary
    val fontWeight = if (isToday) FontWeight.ExtraBold else FontWeight.Medium

    Surface(
        color = bgColor,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 2.dp),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(if (isToday) 1.5.dp else 0.5.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = data.date.readable.substringBefore(".202"), // Yılı kısaltalım
                modifier = Modifier.weight(1.5f),
                color = textColor,
                fontSize = 11.5.sp,
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
    val themeColors = LocalAppThemeColors.current
    Text(
        text = text,
        modifier = modifier,
        color = themeColors.textPrimary,
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
