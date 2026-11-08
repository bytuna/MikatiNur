package com.example.mkat_nur.ui.quran1

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import java.text.Normalizer
import java.util.Locale

private val P = Quran1Palette

/** Kur'an-ı Kerim 1: sure listesi, ayet okuma ve sesli tilavet ekranı. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Quran1Screen(
    onMenuClick: () -> Unit,
    viewModel: Quran1ViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val surahs by viewModel.surahList.collectAsState()
    val openSurah by viewModel.openSurahNumber.collectAsState()
    var showSettings by remember { mutableStateOf(false) }
    var showLegend by remember { mutableStateOf(false) }

    // Ekrandan ayrılırken sesi duraklat
    DisposableEffect(Unit) { onDispose { viewModel.pauseAudio() } }

    // Sure açıkken geri tuşu listeye döner
    BackHandler(enabled = openSurah != null) { viewModel.closeSurah() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(Quran1Palette.backgroundGradient))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = {
                    val title = if (openSurah == null) "Kur'an-ı Kerim 1"
                    else surahs.firstOrNull { it.number == openSurah }?.let { "${it.number}. ${it.turkishName}" } ?: "Kur'an-ı Kerim 1"
                    Text(title, color = P.Gold, fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    if (openSurah == null) {
                        IconButton(onClick = onMenuClick) { Icon(Icons.Default.Menu, "Menü", tint = P.Gold) }
                    } else {
                        IconButton(onClick = { viewModel.closeSurah() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Sure listesi", tint = P.Gold)
                        }
                    }
                },
                actions = {
                    if (openSurah != null) {
                        IconButton(onClick = { showLegend = true }) { Icon(Icons.Default.Palette, "Tecvid renkleri", tint = P.Gold) }
                    }
                    IconButton(onClick = { showSettings = true }) { Icon(Icons.Default.Tune, "Ayarlar", tint = P.Gold) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )

            when (val s = state) {
                is Quran1UiState.ListView -> SurahList(surahs) { viewModel.openSurah(it) }
                is Quran1UiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = P.Gold)
                }
                is Quran1UiState.Error -> Column(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.CloudOff, null, tint = P.Gold, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(12.dp))
                    Text(s.message, color = P.Paper, textAlign = TextAlign.Center, fontSize = 14.sp)
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { viewModel.retry() }, colors = ButtonDefaults.buttonColors(containerColor = P.Gold)) {
                        Text("Tekrar Dene", color = P.Ink, fontWeight = FontWeight.Bold)
                    }
                }
                is Quran1UiState.Success -> SurahReader(s, viewModel)
            }
        }
    }

    if (showSettings) {
        AlertDialog(
            onDismissRequest = { showSettings = false },
            containerColor = P.Paper,
            title = { Text("Kur'an-ı Kerim 1 Ayarları", color = P.Ink, fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Quran1SettingsSection(textColor = P.Ink, secondaryColor = P.InkSoft, accentColor = P.EmeraldSoft)
                }
            },
            confirmButton = { TextButton(onClick = { showSettings = false }) { Text("Tamam", color = P.Emerald, fontWeight = FontWeight.Bold) } }
        )
    }

    if (showLegend) {
        AlertDialog(
            onDismissRequest = { showLegend = false },
            containerColor = P.Paper,
            title = { Text("Tecvid Renk Rehberi", color = P.Ink, fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    TajweedParser.rules.forEach { rule ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(16.dp).background(rule.color, CircleShape))
                            Spacer(Modifier.width(10.dp))
                            Text(rule.label, color = P.Ink, fontSize = 13.sp)
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showLegend = false }) { Text("Tamam", color = P.Emerald, fontWeight = FontWeight.Bold) } }
        )
    }
}

// ====================================================================== Sure listesi

private fun normalize(s: String): String =
    Normalizer.normalize(s.lowercase(Locale("tr")), Normalizer.Form.NFD).replace(Regex("\\p{Mn}+"), "")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SurahList(surahs: List<Quran1SurahInfo>, onSelect: (Int) -> Unit) {
    var query by remember { mutableStateOf("") }
    val filtered = remember(surahs, query) {
        if (query.isBlank()) surahs
        else {
            val q = normalize(query.trim())
            surahs.filter { normalize(it.turkishName).contains(q) || it.number.toString() == q || it.arabicName.contains(q) }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            singleLine = true,
            placeholder = { Text("Sure ara (ad veya numara)", color = P.Paper.copy(alpha = 0.6f)) },
            leadingIcon = { Icon(Icons.Default.Search, null, tint = P.Gold) },
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = P.Gold,
                unfocusedBorderColor = P.Gold.copy(alpha = 0.4f),
                focusedTextColor = P.Paper,
                unfocusedTextColor = P.Paper,
                cursorColor = P.Gold
            ),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(filtered, key = { _, s -> s.number }) { _, surah ->
                Surface(
                    onClick = { onSelect(surah.number) },
                    shape = RoundedCornerShape(16.dp),
                    color = P.Paper,
                    border = BorderStroke(1.dp, P.Gold.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(40.dp).border(1.5.dp, P.GoldDeep, CircleShape),
                            contentAlignment = Alignment.Center
                        ) { Text("${surah.number}", color = P.Emerald, fontWeight = FontWeight.Bold, fontSize = 14.sp) }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(surah.turkishName, color = P.Ink, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("${surah.verseCount} ayet", color = P.InkSoft, fontSize = 12.sp)
                        }
                        Text(surah.arabicName, color = P.Emerald, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

// ====================================================================== Okuma ekranı

@Composable
private fun rememberMushafFont(fontKey: String): FontFamily {
    val context = LocalContext.current
    return remember(fontKey) {
        val option = Quran1Fonts.options.firstOrNull { it.key == fontKey } ?: Quran1Fonts.options.first()
        var id = context.resources.getIdentifier(option.resName, "font", context.packageName)
        if (id == 0) id = context.resources.getIdentifier(Quran1Fonts.options.first().resName, "font", context.packageName)
        if (id != 0) FontFamily(Font(id)) else FontFamily.Serif
    }
}

@Composable
private fun SurahReader(content: Quran1UiState.Success, viewModel: Quran1ViewModel) {
    val surah = content.surah
    val mealli by Quran1Settings.mealli.collectAsState()
    val tajweed by Quran1Settings.tajweed.collectAsState()
    val fontSize by Quran1Settings.fontSize.collectAsState()
    val fontKey by Quran1Settings.fontKey.collectAsState()
    val autoScroll by Quran1Settings.autoScroll.collectAsState()
    val playingIndex by viewModel.currentPlayingAyahIndex.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val isBuffering by viewModel.isBuffering.collectAsState()
    val audioError by viewModel.audioError.collectAsState()
    val fontFamily = rememberMushafFont(fontKey)

    val listState = rememberLazyListState()
    val hasAudio = surah.ayahs.isNotEmpty() && surah.ayahs.all { !it.audioUrl.isNullOrBlank() }

    // Çalan ayeti ekranın tam ortasında tut
    CenterOnPlayingAyah(listState, playingIndex, autoScroll && playingIndex >= 0, headerCount = 1)

    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f)) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item(key = "header") { SurahHeader(surah, content.fromCache) }

                itemsIndexed(surah.ayahs, key = { _, a -> a.globalNumber }) { index, ayah ->
                    AyahCard(
                        ayah = ayah,
                        isActive = index == playingIndex,
                        mealli = mealli,
                        tajweed = tajweed,
                        fontSize = fontSize,
                        fontFamily = fontFamily,
                        canPlay = hasAudio,
                        onPlay = { viewModel.playFrom(index) }
                    )
                }
                item(key = "footer") { Spacer(Modifier.height(16.dp)) }
            }
        }

        if (hasAudio) {
            AudioBar(
                total = surah.ayahs.size,
                ayahNumber = surah.ayahs.getOrNull(playingIndex)?.numberInSurah,
                isPlaying = isPlaying,
                isBuffering = isBuffering,
                autoScroll = autoScroll,
                error = audioError,
                onPrev = { viewModel.previousAyah() },
                onToggle = { viewModel.togglePlayPause() },
                onNext = { viewModel.nextAyah() },
                onToggleAutoScroll = { Quran1Settings.setAutoScroll(!autoScroll) }
            )
        }
    }
}

/**
 * Çalan ayet değişince LazyColumn'u kaydırır. Ayet ekranın ortasına gelsin diye
 * offset = -((viewportHeight - itemHeight) / 2) kullanılır.
 */
@Composable
private fun CenterOnPlayingAyah(listState: LazyListState, playingIndex: Int, enabled: Boolean, headerCount: Int) {
    LaunchedEffect(playingIndex, enabled) {
        if (!enabled) return@LaunchedEffect
        val target = playingIndex + headerCount

        fun measuredItemSize(): Int? =
            listState.layoutInfo.visibleItemsInfo.firstOrNull { it.index == target }?.size

        // Ayet henüz ölçülmediyse önce görünür yap, sonra gerçek yüksekliğiyle ortala
        if (measuredItemSize() == null) {
            listState.scrollToItem(target)
            delay(32)
        }
        val itemHeight = measuredItemSize() ?: return@LaunchedEffect
        val viewportHeight = listState.layoutInfo.viewportSize.height
        val offset = if (itemHeight >= viewportHeight) 0 else -((viewportHeight - itemHeight) / 2)
        listState.animateScrollToItem(target, offset)
    }
}

@Composable
private fun SurahHeader(surah: Quran1Surah, fromCache: Boolean) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = P.EmeraldDeep,
        border = BorderStroke(1.5.dp, P.Gold),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(vertical = 18.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(surah.arabicName, color = P.Gold, fontSize = 30.sp, fontWeight = FontWeight.Bold)
            Text(surah.turkishName, color = P.Paper, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Text("${surah.ayahs.size} ayet", color = P.Paper.copy(alpha = 0.7f), fontSize = 12.sp)
            if (fromCache) {
                Spacer(Modifier.height(4.dp))
                Text("Çevrimdışı kopya gösteriliyor", color = P.Gold.copy(alpha = 0.8f), fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun AyahCard(
    ayah: Quran1Ayah,
    isActive: Boolean,
    mealli: Boolean,
    tajweed: Boolean,
    fontSize: Float,
    fontFamily: FontFamily,
    canPlay: Boolean,
    onPlay: () -> Unit
) {
    val shape = RoundedCornerShape(18.dp)
    val borderColor = if (isActive) P.Gold else P.Gold.copy(alpha = 0.35f)

    Surface(
        shape = shape,
        color = if (isActive) Color(0xFFFFF4CF) else P.Paper,
        border = BorderStroke(if (isActive) 2.5.dp else 1.dp, borderColor),
        modifier = Modifier
            .fillMaxWidth()
            .then(if (isActive) Modifier.shadow(12.dp, shape, ambientColor = P.Gold, spotColor = P.Gold) else Modifier)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(34.dp).background(if (isActive) P.Gold else P.Emerald, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "${ayah.numberInSurah}",
                        color = if (isActive) P.Ink else P.Paper,
                        fontWeight = FontWeight.Bold, fontSize = 13.sp
                    )
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    listOfNotNull(ayah.juz?.let { "Cüz $it" }, ayah.page?.let { "Sayfa $it" }).joinToString(" · "),
                    color = P.InkSoft, fontSize = 11.sp, modifier = Modifier.weight(1f)
                )
                if (canPlay) {
                    IconButton(onClick = onPlay, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.PlayCircle, "Bu ayetten dinle", tint = P.Emerald, modifier = Modifier.size(28.dp))
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // Arapça metin (sağdan sola)
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                val arabicText = remember(ayah, tajweed) {
                    if (tajweed && !ayah.tajweed.isNullOrBlank()) TajweedParser.parse(ayah.tajweed, P.Ink)
                    else androidx.compose.ui.text.AnnotatedString(ayah.uthmani)
                }
                Text(
                    text = arabicText,
                    color = P.Ink,
                    fontFamily = fontFamily,
                    fontSize = fontSize.sp,
                    lineHeight = (fontSize * 1.95f).sp,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (mealli) {
                if (!ayah.transliteration.isNullOrBlank()) {
                    Spacer(Modifier.height(10.dp))
                    HorizontalDivider(color = P.Gold.copy(alpha = 0.4f))
                    Spacer(Modifier.height(8.dp))
                    Text(
                        ayah.transliteration, color = P.EmeraldSoft, fontSize = 14.sp,
                        fontStyle = FontStyle.Italic, lineHeight = 20.sp
                    )
                }
                if (!ayah.meal.isNullOrBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(ayah.meal, color = P.Ink, fontSize = 15.sp, lineHeight = 22.sp)
                }
            }
        }
    }
}

@Composable
private fun AudioBar(
    total: Int,
    ayahNumber: Int?,
    isPlaying: Boolean,
    isBuffering: Boolean,
    autoScroll: Boolean,
    error: String?,
    onPrev: () -> Unit,
    onToggle: () -> Unit,
    onNext: () -> Unit,
    onToggleAutoScroll: () -> Unit
) {
    Surface(
        color = P.EmeraldDeep,
        border = BorderStroke(1.dp, P.Gold.copy(alpha = 0.6f)),
        shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.navigationBarsPadding().padding(horizontal = 12.dp, vertical = 8.dp)) {
            if (error != null) {
                Text(error, color = Color(0xFFFFB4A9), fontSize = 11.sp, modifier = Modifier.padding(bottom = 4.dp))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        if (ayahNumber != null) "Ayet $ayahNumber / $total" else "Dinlemek için ▶",
                        color = P.Paper, fontSize = 13.sp, fontWeight = FontWeight.SemiBold
                    )
                    Text("Mişarî Raşid Alafasy", color = P.Gold.copy(alpha = 0.8f), fontSize = 10.5.sp)
                }
                IconButton(onClick = onPrev) { Icon(Icons.Default.SkipPrevious, "Önceki ayet", tint = P.Paper) }
                Box(
                    modifier = Modifier.size(48.dp).background(P.Gold, CircleShape).clickable(onClick = onToggle),
                    contentAlignment = Alignment.Center
                ) {
                    if (isBuffering && !isPlaying) {
                        CircularProgressIndicator(color = P.Ink, strokeWidth = 2.dp, modifier = Modifier.size(22.dp))
                    } else {
                        Icon(
                            if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            if (isPlaying) "Duraklat" else "Oynat",
                            tint = P.Ink, modifier = Modifier.size(28.dp)
                        )
                    }
                }
                IconButton(onClick = onNext) { Icon(Icons.Default.SkipNext, "Sonraki ayet", tint = P.Paper) }
                IconButton(onClick = onToggleAutoScroll) {
                    Icon(
                        Icons.Default.CenterFocusStrong,
                        if (autoScroll) "Otomatik kaydırma açık" else "Otomatik kaydırma kapalı",
                        tint = if (autoScroll) P.Gold else P.Paper.copy(alpha = 0.4f)
                    )
                }
            }
        }
    }
}
