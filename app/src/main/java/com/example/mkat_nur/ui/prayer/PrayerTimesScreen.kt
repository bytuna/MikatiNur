package com.example.mkat_nur.ui.prayer

import android.content.Intent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.mkat_nur.model.*
import com.example.mkat_nur.util.AppConfig
import com.example.mkat_nur.util.ShareUtils
import com.example.mkat_nur.viewmodel.CountdownState
import com.example.mkat_nur.viewmodel.PrayerUiState
import com.example.mkat_nur.viewmodel.PrayerViewModel
import kotlinx.coroutines.delay
import java.util.*

fun getTurkishHijriText(monthEn: String): String {
    val input = monthEn.trim()
    return when {
        input.contains("Muharram", true) || input == "1" -> "Muharrem"
        input.contains("Safar", true) || input == "2" -> "Safer"
        input.contains("Rabi", true) && input.contains("awwal", true) || input == "3" -> "Rebiülevvel"
        input.contains("Rabi", true) && (input.contains("thani", true) || input.contains("akhir", true)) || input == "4" -> "Rebiülahir"
        input.contains("Jumada", true) && (input.contains("ula", true) || input.contains("1", true)) || input == "5" -> "Cemaziyelevvel"
        input.contains("Jumada", true) && (input.contains("akhira", true) || input.contains("2", true)) || input == "6" -> "Cemaziyelahir"
        input.contains("Rajab", true) || input == "7" -> "Recep"
        input.contains("Sha", true) && input.contains("ban", true) || input == "8" -> "Şaban"
        input.contains("Ramadan", true) || input == "9" -> "Ramazan"
        input.contains("Shawwal", true) || input == "10" -> "Şevval"
        input.contains("Qi", true) || input.contains("Qa", true) || input == "11" -> "Zilkade"
        input.contains("Hijjah", true) || input == "12" -> "Zilhicce"
        else -> input
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrayerTimesScreen(
    viewModel: PrayerViewModel = viewModel(),
    onMenuClick: () -> Unit,
    onPrayerClick: (String) -> Unit
) {
    val uiState by viewModel.offsetAppliedUiState.collectAsState()
    val countdownState by viewModel.countdownState.collectAsState()
    val selectedProvince by viewModel.selectedProvince.collectAsState()
    val isDarkModeState by viewModel.isDarkMode.collectAsState()
    val dailyContent by viewModel.dailyContent.collectAsState()
    val dailyContentType by viewModel.dailyContentType.collectAsState()
    val dataSource by viewModel.dataSource.collectAsState()
    val lastUpdateTimestamp by viewModel.lastUpdateTimestamp.collectAsState()

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
                    is PrayerUiState.Success -> MainContent(
                        state.data, 
                        countdownState, 
                        selectedProvince, 
                        dailyContent, 
                        dailyContentType, 
                        dataSource,
                        lastUpdateTimestamp = lastUpdateTimestamp,
                        viewModel = viewModel,
                        onRefreshLocation = { viewModel.refreshLocation() },
                        onPrayerClick = onPrayerClick
                    )
                    is PrayerUiState.Error -> Box(Modifier.fillMaxSize(), Alignment.Center) { Text("Hata: ${state.message}", color = Color.White) }
                }
            }
        }
    }

    if (showProvinceDialog) {
        ProvinceSelectionDialog(
            viewModel = viewModel,
            onDismiss = { showProvinceDialog = false }
        )
    }
}

@Composable
fun MainContent(
    data: PrayerData,
    countdownState: CountdownState?,
    province: Province,
    dailyContent: com.example.mkat_nur.model.DailyContent,
    dailyContentType: String,
    dataSource: String,
    lastUpdateTimestamp: Long,
    viewModel: PrayerViewModel,
    onRefreshLocation: () -> Unit,
    onPrayerClick: (String) -> Unit
) {
    var showKibleInfo by remember { mutableStateOf(false) }

    if (showKibleInfo) {
        AlertDialog(
            onDismissRequest = { showKibleInfo = false },
            confirmButton = {
                TextButton(onClick = { showKibleInfo = false }) {
                    Text("Tamam", color = Color(0xFFFFD700))
                }
            },
            title = { Text("Kıble Saati Nedir?", fontWeight = FontWeight.Bold) },
            text = {
                Text("Kıble Saati, Güneş'in tam kıble yönünde olduğu vakittir. Bu vakitte Güneş'e doğru dönen bir kimse, aynı zamanda kıbleye dönmüş olur. Pusulaya ihtiyaç duymadan en doğru şekilde kıbleyi tayin etmenizi sağlar.")
            },
            containerColor = Color(0xFF1B263B),
            titleContentColor = Color.White,
            textContentColor = Color.White.copy(alpha = 0.8f)
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { 
            HeaderSection(
                cityName = province.name, 
                data = data, 
                dataSource = dataSource,
                lastUpdateTimestamp = lastUpdateTimestamp,
                onRefresh = onRefreshLocation
            ) 
        }
        item { countdownState?.let { ModernCountdown(it) } }

        item { SlidingContentCard(dailyContent, viewModel) }

        item {
            val context = LocalContext.current
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "NAMAZ VAKİTLERİ",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    PrayerTimesGrid(data.timings, countdownState?.currentPrayer, onPrayerClick)
                }
            }
        }
        
        if (data.timings.kible.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { showKibleInfo = true },
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp), 
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Explore, null, tint = Color(0xFFFF9800))
                            Spacer(Modifier.width(12.dp))
                            Text("Kıble Saati: ", color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
                            Text(data.timings.kible, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Bilgi",
                            tint = Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
        
        item { Spacer(Modifier.height(20.dp)) }
    }
}

@Composable
fun SlidingContentCard(content: com.example.mkat_nur.model.DailyContent, viewModel: PrayerViewModel) {
    var index by remember { mutableIntStateOf(0) }
    val slidingDuration by viewModel.slidingDuration.collectAsState()
    val showAyet by viewModel.showAyet.collectAsState()
    val showHadis by viewModel.showHadis.collectAsState()
    val showVecize by viewModel.showVecize.collectAsState()
    val showEsma by viewModel.showEsma.collectAsState()

    val items = remember(content, showAyet, showHadis, showVecize, showEsma) {
        val list = mutableListOf<Triple<String, String, String>>()
        if (showAyet) list.add(Triple("Günün Ayeti", content.verse, content.verseSource))
        if (showHadis) list.add(Triple("Günün Hadisi", content.hadith, content.hadithSource))
        if (showVecize) list.add(Triple("Günün Vecizesi", content.quote, content.quoteSource))
        if (showEsma) list.add(Triple("Esmaü'l Hüsna", content.name, content.nameMeaning))
        list.filter { it.second.isNotEmpty() }
    }

    if (items.isEmpty()) return

    LaunchedEffect(items, slidingDuration) {
        while (true) {
            kotlinx.coroutines.delay((slidingDuration * 1000).toLong())
            if (items.isNotEmpty()) {
                index = (index + 1) % items.size
            }
        }
    }

    // Index sınır kontrolü
    val safeIndex = if (index < items.size) index else 0
    val current = items[safeIndex]
    
    val accentColor = when(current.first) {
        "Günün Ayeti" -> Color(0xFF81C784)
        "Günün Hadisi" -> Color(0xFF64B5F6)
        "Günün Vecizesi" -> Color(0xFFBA68C8)
        else -> Color(0xFFFFD54F)
    }

    Crossfade(targetState = current, animationSpec = tween(1000), label = "") { item ->
        InfoCard(
            title = item.first,
            content = item.second,
            source = item.third,
            icon = when(item.first) {
                "Günün Ayeti" -> Icons.AutoMirrored.Filled.MenuBook
                "Günün Hadisi" -> Icons.Default.AutoStories
                "Günün Vecizesi" -> Icons.Default.Favorite
                else -> Icons.Default.AutoAwesome
            },
            accentColor = accentColor,
            viewModel = viewModel
        )
    }
}

@Composable
fun HeaderSection(cityName: String, data: PrayerData, dataSource: String, lastUpdateTimestamp: Long, onRefresh: () -> Unit) {
    var tick by remember { mutableLongStateOf(System.currentTimeMillis()) }
    
    LaunchedEffect(lastUpdateTimestamp) {
        while(true) {
            delay(60000)
            tick = System.currentTimeMillis()
        }
    }

    val updateText = remember(lastUpdateTimestamp, tick) {
        if (lastUpdateTimestamp == 0L) ""
        else {
            val diff = System.currentTimeMillis() - lastUpdateTimestamp
            val mins = (diff / (1000 * 60)).toInt()
            when {
                mins < 1 -> "(Az önce)"
                mins < 60 -> "($mins dk önce)"
                mins < 1440 -> "(${mins / 60} sa önce)"
                else -> "(${mins / 1440} gün önce)"
            }
        }
    }

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f).clickable { onRefresh() }) {
            Text(text = cityName.uppercase(), fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color.White)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = data.date.readable, color = Color.White.copy(alpha = 0.7f), fontSize = 14.sp)
                Spacer(Modifier.width(8.dp))
                Box(Modifier.size(4.dp).background(Color.White.copy(alpha = 0.5f), CircleShape))
                Spacer(Modifier.width(8.dp))
                Text(text = "${data.date.hijri.day} ${getTurkishHijriText(data.date.hijri.month.en)}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            Text(
                text = "$dataSource $updateText",
                color = Color.White.copy(alpha = 0.4f),
                fontSize = 10.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        
        if (data.moonUrl.isNotEmpty()) {
            AsyncImage(
                model = data.moonUrl,
                contentDescription = "Ayın Şekli",
                modifier = Modifier.size(50.dp),
                contentScale = ContentScale.Fit
            )
        }
    }
}

@Composable
fun InfoCard(title: String, content: String, source: String, icon: ImageVector, accentColor: Color, viewModel: PrayerViewModel) {
    val context = LocalContext.current
    val isAiLoading by viewModel.isAiLoading.collectAsState()

    Card(
        modifier = Modifier.fillMaxWidth().height(220.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.12f)),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp).fillMaxHeight()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(icon, null, tint = accentColor, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(title, color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isAiLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.White.copy(alpha = 0.7f),
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.width(12.dp))
                    } else {
                        IconButton(
                            onClick = {
                                viewModel.shareWithAi(context, title, content, source)
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "AI ile Paylaş",
                                tint = Color(0xFFFFD700).copy(alpha = 0.8f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(Modifier.width(4.dp))
                    }

                    IconButton(
                        onClick = {
                            ShareUtils.shareInfoAsImage(context, title, content, source)
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.Share, "Paylaş", tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(16.dp))
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            
            // Metin Kısmı
            Box(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                Text(
                    text = if (content.startsWith("“")) content else "“$content”",
                    color = Color.White, 
                    fontSize = 17.sp, 
                    fontWeight = FontWeight.Medium,
                    lineHeight = 24.sp
                )
            }
            
            // Alt Kısım: Kaynak ve Uygulama İsmi
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = AppConfig.PROJECT_NAME,
                    color = Color.White.copy(alpha = 0.2f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Light
                )
                
                if (source.isNotBlank()) {
                    Text(
                        text = source, 
                        color = Color.White.copy(alpha = 0.5f), 
                        fontSize = 12.sp, 
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.End
                    )
                }
            }
        }
    }
}

@Composable
fun ModernCountdown(state: CountdownState) {
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.height(8.dp))

            if (state.isKerahat) {
                Text("KERAHAT VAKTİ", color = Color.Red, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
                Spacer(Modifier.height(4.dp))
            }
            val turkishLocale = Locale("tr", "TR")
            Text("${state.nextPrayer.uppercase(turkishLocale)} VAKTİNE", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text(
                text = String.format(Locale.getDefault(), "%02d:%02d:%02d", state.hours, state.minutes, state.seconds),
                fontSize = 44.sp, fontWeight = FontWeight.Black, color = Color.White
            )
            
            Spacer(Modifier.height(12.dp))

            // Işıldayan Kenarlı Mevcut Vakit Rozeti
            Surface(
                color = Color.White.copy(alpha = 0.1f),
                shape = RoundedCornerShape(50.dp),
                border = BorderStroke(
                    width = 1.5.dp,
                    color = Color(0xFFFFD700).copy(alpha = alpha) // Altın sarısı ışıldama
                )
            ) {
                Text(
                    text = "${state.currentPrayer} Vakti", 
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp), 
                    color = Color.White, 
                    fontSize = 15.sp, 
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun PrayerTimesGrid(timings: com.example.mkat_nur.model.Timings, currentPrayer: String?, onPrayerClick: (String) -> Unit) {
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
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items.take(4).forEach { (name, time) ->
                PrayerTimeItem(name, time, name == currentPrayer, onPrayerClick, Modifier.weight(1f))
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items.drop(4).forEach { (name, time) ->
                PrayerTimeItem(name, time, name == currentPrayer, onPrayerClick, Modifier.weight(1f))
            }
            Spacer(Modifier.weight(1f))
        }
    }
}

@Composable
fun PrayerTimeItem(name: String, time: String, isActive: Boolean, onPrayerClick: (String) -> Unit, modifier: Modifier) {
    val isTesbihatAvailable = name != "İmsak" && name != "Güneş"
    
    Box(
        modifier = modifier
            .background(if (isActive) Color.White.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
            .border(1.dp, if (isActive) Color.White.copy(alpha = 0.4f) else Color.Transparent, RoundedCornerShape(16.dp))
            .then(if (isTesbihatAvailable) Modifier.clickable { onPrayerClick(name) } else Modifier)
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
fun ProvinceSelectionDialog(viewModel: PrayerViewModel, onDismiss: () -> Unit) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedSehirId by remember { mutableStateOf<String?>(null) }
    var selectedSehirName by remember { mutableStateOf("") }

    val sehirler by viewModel.sehirler.collectAsState()
    val ilceler by viewModel.ilceler.collectAsState()

    val trLocale = remember { Locale.forLanguageTag("tr-TR") }
    
    val filteredSehirler = remember(sehirler, searchQuery) {
        sehirler.filter { it.sehirAdi.contains(searchQuery, ignoreCase = true) }
    }
    val filteredIlceler = remember(ilceler, searchQuery) {
        ilceler.filter { it.ilceAdi.contains(searchQuery, ignoreCase = true) }
    }

    val dialogBg = Brush.verticalGradient(listOf(Color(0xFF0D1B2A), Color(0xFF1B263B)))

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier.padding(28.dp),
        content = {
            Surface(modifier = Modifier.fillMaxWidth().wrapContentHeight(), shape = RoundedCornerShape(28.dp), color = Color.Transparent) {
                Box(modifier = Modifier.background(dialogBg).padding(24.dp)) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (selectedSehirId != null) {
                                IconButton(onClick = { selectedSehirId = null; searchQuery = "" }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                                }
                            }
                            Text(
                                if (selectedSehirId == null) "Şehir Seçin" else "$selectedSehirName - İlçe Seçin",
                                fontWeight = FontWeight.Black, fontSize = 18.sp, color = Color.White
                            )
                        }
                        
                        Spacer(Modifier.height(16.dp))
                        
                        OutlinedTextField(
                            value = searchQuery, onValueChange = { searchQuery = it }, modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Ara...", color = Color.White.copy(alpha = 0.5f)) },
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = Color.White, unfocusedBorderColor = Color.White.copy(alpha = 0.3f), cursorColor = Color.White),
                            shape = RoundedCornerShape(16.dp), singleLine = true
                        )
                        
                        Spacer(Modifier.height(16.dp))
                        
                        Surface(modifier = Modifier.height(400.dp), color = Color.White.copy(alpha = 0.1f), shape = RoundedCornerShape(16.dp)) {
                            LazyColumn {
                                if (selectedSehirId == null) {
                                    items(filteredSehirler.size) { index ->
                                        val s = filteredSehirler[index]
                                        TextButton(
                                            onClick = {
                                                selectedSehirId = s.sehirId
                                                selectedSehirName = s.sehirAdi
                                                searchQuery = ""
                                                viewModel.fetchIlceler(s.sehirId)
                                            },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(s.sehirAdi.uppercase(trLocale), color = Color.White, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Start, fontWeight = FontWeight.Bold)
                                        }
                                        if (index < filteredSehirler.size - 1) HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                                    }
                                } else {
                                    if (ilceler.isEmpty()) {
                                        item {
                                            Box(Modifier.fillMaxWidth().padding(20.dp), Alignment.Center) {
                                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                                            }
                                        }
                                    }
                                    items(filteredIlceler.size) { index ->
                                        val i = filteredIlceler[index]
                                        TextButton(
                                            onClick = {
                                                viewModel.onProvinceSelected(Province(i.ilceAdi, i.ilceId))
                                                onDismiss()
                                            },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(i.ilceAdi.uppercase(trLocale), color = Color(0xFFFF9800), modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Start, fontWeight = FontWeight.Bold)
                                        }
                                        if (index < filteredIlceler.size - 1) HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                                    }
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
