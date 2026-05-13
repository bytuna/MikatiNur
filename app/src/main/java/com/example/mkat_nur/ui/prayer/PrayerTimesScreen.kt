package com.example.mkat_nur.ui.prayer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mkat_nur.model.PrayerData
import com.example.mkat_nur.model.Province
import com.example.mkat_nur.viewmodel.CountdownState
import com.example.mkat_nur.viewmodel.PrayerUiState
import com.example.mkat_nur.viewmodel.PrayerViewModel
import java.util.*

fun getTurkishHijriText(monthEn: String): String {
    val input = monthEn.trim()
    return when {
        input.contains("Muharram", true) -> "Muharrem"
        input.contains("Safar", true) -> "Safer"
        input.contains("Rabi", true) && input.contains("awwal", true) -> "Rebiülevvel"
        input.contains("Rabi", true) && (input.contains("thani", true) || input.contains("akhir", true)) -> "Rebiülahir"
        input.contains("Jumada", true) && (input.contains("ula", true) || input.contains("1", true)) -> "Cemaziyelevvel"
        input.contains("Jumada", true) && (input.contains("akhira", true) || input.contains("2", true)) -> "Cemaziyelahir"
        input.contains("Rajab", true) -> "Recep"
        input.contains("Sha", true) && input.contains("ban", true) -> "Şaban"
        input.contains("Ramadan", true) -> "Ramazan"
        input.contains("Shawwal", true) -> "Şevval"
        input.contains("Qi", true) || input.contains("Qa", true) -> "Zilkade"
        input.contains("Hijjah", true) -> "Zilhicce"
        else -> input
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrayerTimesScreen(
    viewModel: PrayerViewModel = viewModel(),
    onMenuClick: () -> Unit
) {
    val uiState by viewModel.offsetAppliedUiState.collectAsState()
    val countdownState by viewModel.countdownState.collectAsState()
    val selectedProvince by viewModel.selectedProvince.collectAsState()
    val isDarkModeState by viewModel.isDarkMode.collectAsState()
    val dailyContent by viewModel.dailyContent.collectAsState()
    val dailyContentType by viewModel.dailyContentType.collectAsState()

    val isInDarkMode = isDarkModeState ?: isSystemInDarkTheme()
    var showProvinceDialog by remember { mutableStateOf(false) }

    val bgColors = if (isInDarkMode) {
        listOf(Color(0xFF121212), Color(0xFF1E1E1E))
    } else {
        when (countdownState?.currentPrayer) {
            "İmsak", "Yatsı" -> listOf(Color(0xFF0D1B2A), Color(0xFF1B263B))
            "Akşam" -> listOf(Color(0xFF370617), Color(0xFF6A040F))
            else -> listOf(Color(0xFF023E8A), Color(0xFF0077B6))
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(bgColors))) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Mîkat-ı Nur", fontWeight = FontWeight.ExtraBold, color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = onMenuClick) { Icon(Icons.Default.Menu, null, tint = Color.White) }
                    },
                    actions = {
                        IconButton(onClick = {
                            val nextMode = when(isDarkModeState) {
                                null -> true
                                true -> false
                                false -> null
                            }
                            viewModel.toggleDarkMode(nextMode)
                        }) {
                            val icon = when(isDarkModeState) {
                                null -> Icons.Default.BrightnessAuto
                                true -> Icons.Default.DarkMode
                                false -> Icons.Default.LightMode
                            }
                            Icon(icon, null, tint = Color.White)
                        }
                        IconButton(onClick = { showProvinceDialog = true }) {
                            Icon(Icons.Default.LocationOn, null, tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { padding ->
            Box(modifier = Modifier.padding(padding)) {
                when (val state = uiState) {
                    is PrayerUiState.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator(color = Color.White) }
                    is PrayerUiState.Success -> MainContent(state.data, countdownState, selectedProvince, dailyContent, dailyContentType)
                    is PrayerUiState.Error -> Box(Modifier.fillMaxSize(), Alignment.Center) { Text("Hata: ${state.message}", color = Color.White) }
                }
            }
        }
    }

    if (showProvinceDialog) {
        ProvinceSelectionDialog(
            onDismiss = { showProvinceDialog = false },
            onProvinceSelected = { viewModel.onProvinceSelected(it); showProvinceDialog = false }
        )
    }
}

@Composable
fun MainContent(
    data: PrayerData,
    countdownState: CountdownState?,
    province: Province,
    dailyContent: com.example.mkat_nur.model.DailyContent,
    dailyContentType: String
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { HeaderSection(province.name, data.date) }
        item { countdownState?.let { ModernCountdown(it) } }

        item {
            InfoCard(
                title = "Günün $dailyContentType",
                content = dailyContent.verse,
                source = dailyContent.verseSource,
                icon = Icons.Default.MenuBook,
                accentColor = Color(0xFF81C784)
            )
        }

        item {
            InfoCard(
                title = "Esmaü'l Hüsna",
                content = dailyContent.name,
                source = dailyContent.nameMeaning,
                icon = Icons.Default.AutoAwesome,
                accentColor = Color(0xFFFFD54F)
            )
        }

        item { PrayerTimesGrid(data.timings, countdownState?.currentPrayer) }
        item { Spacer(Modifier.height(20.dp)) }
    }
}

@Composable
fun HeaderSection(cityName: String, date: com.example.mkat_nur.model.DateInfo) {
    Column {
        Text(text = cityName.uppercase(), fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color.White)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = date.readable, color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
            Spacer(Modifier.width(8.dp))
            Box(Modifier.size(4.dp).background(Color.White.copy(alpha = 0.5f), CircleShape))
            Spacer(Modifier.width(8.dp))
            Text(text = "${date.hijri.day} ${getTurkishHijriText(date.hijri.month.en)}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}

@Composable
fun InfoCard(title: String, content: String, source: String, icon: ImageVector, accentColor: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.12f)),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = accentColor, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(title, color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(10.dp))
            Text(text = "“$content”", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Medium)
            Text(text = source, color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp, modifier = Modifier.align(Alignment.End))
        }
    }
}

@Composable
fun ModernCountdown(state: CountdownState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("${state.nextPrayer.uppercase()} VAKTİNE", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text(
                text = String.format("%02d:%02d:%02d", state.hours, state.minutes, state.seconds),
                fontSize = 44.sp, fontWeight = FontWeight.Black, color = Color.White
            )
            Surface(color = Color.White.copy(alpha = 0.15f), shape = RoundedCornerShape(50.dp)) {
                // HATALI SATIR BURADA DÜZELTİLDİ: Artık kerahatStatus yerine sadece mevcut vakit yazıyor
                Text("${state.currentPrayer} Vakti", Modifier.padding(horizontal = 12.dp, vertical = 4.dp), color = Color.White, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun PrayerTimesGrid(timings: com.example.mkat_nur.model.Timings, currentPrayer: String?) {
    val items = listOf(
        "İmsak" to timings.fajr,
        "Sabah" to timings.sabah,
        "Güneş" to timings.sunrise,
        "Öğle" to timings.dhuhr,
        "İkindi" to timings.asr,
        "Akşam" to timings.maghrib,
        "Yatsı" to timings.isha
    )
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // İlk 4 vakit (İmsak, Sabah, Güneş, Öğle)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items.take(4).forEach { (name, time) ->
                PrayerTimeItem(name, time, name == currentPrayer, Modifier.weight(1f))
            }
        }
        // Son 3 vakit (İkindi, Akşam, Yatsı)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items.drop(4).forEach { (name, time) ->
                PrayerTimeItem(name, time, name == currentPrayer, Modifier.weight(1f))
            }
            // Hizalama için boş kutu (İsteğe bağlı, 4-3 yapısı için)
            Spacer(Modifier.weight(1f))
        }
    }
}

@Composable
fun PrayerTimeItem(name: String, time: String, isActive: Boolean, modifier: Modifier) {
    Box(
        modifier = modifier
            .background(if (isActive) Color.White.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
            .border(1.dp, if (isActive) Color.White.copy(alpha = 0.4f) else Color.Transparent, RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Column {
            Text(name, color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp)
            Text(time.substringBefore(" "), color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProvinceSelectionDialog(onDismiss: () -> Unit, onProvinceSelected: (Province) -> Unit) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredProvinces = PrayerViewModel.provinces.filter { it.name.contains(searchQuery, ignoreCase = true) }
    val dialogBg = Brush.verticalGradient(listOf(Color(0xFF0D1B2A), Color(0xFF1B263B)))
    AlertDialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier.padding(28.dp),
        content = {
            Surface(modifier = Modifier.fillMaxWidth().wrapContentHeight(), shape = RoundedCornerShape(28.dp), color = Color.Transparent) {
                Box(modifier = Modifier.background(dialogBg).padding(24.dp)) {
                    Column {
                        Text("Şehir Seçin", fontWeight = FontWeight.Black, fontSize = 22.sp, color = Color.White)
                        Spacer(Modifier.height(16.dp))
                        OutlinedTextField(
                            value = searchQuery, onValueChange = { searchQuery = it }, modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Şehir ara...", color = Color.White.copy(alpha = 0.5f)) },
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = Color.White, unfocusedBorderColor = Color.White.copy(alpha = 0.3f), cursorColor = Color.White),
                            shape = RoundedCornerShape(16.dp), singleLine = true
                        )
                        Spacer(Modifier.height(16.dp))
                        Surface(modifier = Modifier.height(350.dp), color = Color.White.copy(alpha = 0.1f), shape = RoundedCornerShape(16.dp)) {
                            LazyColumn {
                                items(filteredProvinces.size) { index ->
                                    val p = filteredProvinces[index]
                                    TextButton(onClick = { onProvinceSelected(p) }, modifier = Modifier.fillMaxWidth()) {
                                        Text(p.name.uppercase(), color = Color.White, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Start, fontWeight = FontWeight.Bold)
                                    }
                                    if (index < filteredProvinces.size - 1) HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                                }
                            }
                        }
                        TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) { Text("KAPAT", color = Color.White, fontWeight = FontWeight.Bold) }
                    }
                }
            }
        }
    )
}