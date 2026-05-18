package com.example.mkat_nur.ui.quran

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.filled.Pause
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mkat_nur.R
import com.example.mkat_nur.model.Surah
import com.example.mkat_nur.model.Verse
import com.example.mkat_nur.viewmodel.QuranUiState
import com.example.mkat_nur.viewmodel.QuranViewModel
import com.example.mkat_nur.viewmodel.SurahDetailUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranScreen(
    viewModel: QuranViewModel = viewModel(),
    onMenuClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val detailUiState by viewModel.detailUiState.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val isPaused by viewModel.isPaused.collectAsState()
    val playbackSpeed by viewModel.playbackSpeed.collectAsState()
    val currentPlayingVerse by viewModel.currentPlayingVerse.collectAsState()
    val currentPlayingSurahId by viewModel.currentPlayingSurahId.collectAsState()
    val selectedReciter by viewModel.selectedReciter.collectAsState()
    val selectedFont by viewModel.selectedFont.collectAsState()

    val view = LocalView.current
    DisposableEffect(isPlaying) {
        if (isPlaying) {
            view.keepScreenOn = true
        }
        onDispose {
            view.keepScreenOn = false
        }
    }

    var selectedSurah by remember { mutableStateOf<Surah?>(null) }
    var selectedJuz by remember { mutableStateOf<Int?>(null) }
    var selectedPage by remember { mutableStateOf<Int?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(0) }
    var showSettings by remember { mutableStateOf(false) }

    val surahListState = rememberLazyListState()
    val juzListState = rememberLazyListState()
    val pageListState = rememberLazyListState()
    val verseListState = rememberLazyListState()

    val bgColors = listOf(Color(0xFF002B36), Color(0xFF073642)) // Koyu petrol/mavi tema

    if (showSettings) {
        SettingsDialog(
            currentReciter = selectedReciter,
            currentFont = selectedFont,
            onReciterChange = { viewModel.setReciter(it) },
            onFontChange = { viewModel.setFont(it) },
            onDismiss = { showSettings = false }
        )
    }

    Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(bgColors))) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Column {
                    CenterAlignedTopAppBar(
                        title = {
                            val title = when {
                                selectedSurah != null -> selectedSurah!!.name?.uppercase() ?: ""
                                selectedJuz != null -> "${selectedJuz}. CÜZ"
                                selectedPage != null -> "SAYFA $selectedPage"
                                else -> "KUR'AN-I KERİM"
                            }
                            Text(
                                text = title,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFFFD700),
                                letterSpacing = 2.sp
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = {
                                when {
                                    selectedSurah != null -> selectedSurah = null
                                    selectedJuz != null -> selectedJuz = null
                                    selectedPage != null -> selectedPage = null
                                    else -> onMenuClick()
                                }
                            }) {
                                Icon(
                                    imageVector = if (selectedSurah == null && selectedJuz == null && selectedPage == null) Icons.Default.Menu else Icons.Default.ArrowBack,
                                    contentDescription = null,
                                    tint = Color.White
                                )
                            }
                        },
                        actions = {
                            IconButton(onClick = { showSettings = true }) {
                                Icon(Icons.Default.Settings, contentDescription = "Ayarlar", tint = Color.White)
                            }
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                    )
                    
                    if (selectedSurah == null && selectedJuz == null && selectedPage == null) {
                        TabRow(
                            selectedTabIndex = selectedTab,
                            containerColor = Color.Transparent,
                            contentColor = Color(0xFFFFD700),
                            indicator = { tabPositions ->
                                TabRowDefaults.SecondaryIndicator(
                                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                    color = Color(0xFFFFD700)
                                )
                            },
                            divider = {}
                        ) {
                            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                                Text("SURELER", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold, color = if(selectedTab == 0) Color(0xFFFFD700) else Color.White.copy(0.6f))
                            }
                            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                                Text("CÜZLER", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold, color = if(selectedTab == 1) Color(0xFFFFD700) else Color.White.copy(0.6f))
                            }
                            Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }) {
                                Text("SAYFALAR", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold, color = if(selectedTab == 2) Color(0xFFFFD700) else Color.White.copy(0.6f))
                            }
                        }

                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            placeholder = { Text("Sure ara...", color = Color.White.copy(alpha = 0.5f)) },
                            leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.White.copy(alpha = 0.5f)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFFFD700),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                    }
                }
            }
        ) { padding ->
            Box(modifier = Modifier.padding(padding)) {
                if (selectedSurah == null && selectedJuz == null && selectedPage == null) {
                    when (selectedTab) {
                        0 -> { // Sureler
                            when (val state = uiState) {
                                is QuranUiState.Loading -> LoadingBox()
                                is QuranUiState.Success -> {
                                    val filteredSurahs = state.surahs.filter { 
                                        (it.name?.contains(searchQuery, ignoreCase = true) ?: false) || 
                                        it.id.toString() == searchQuery 
                                    }
                                    SurahList(filteredSurahs, surahListState) { surah ->
                                        selectedSurah = surah
                                        viewModel.stopAudio()
                                        viewModel.fetchSurahDetail(surah.id)
                                    }
                                }
                                is QuranUiState.Error -> ErrorBox(state.message)
                            }
                        }
                        1 -> { // Cüzler
                            JuzList(searchQuery, juzListState) { juz ->
                                selectedJuz = juz
                                viewModel.fetchJuzDetail(juz)
                            }
                        }
                        2 -> { // Sayfalar
                            PageList(searchQuery, pageListState) { page ->
                                selectedPage = page
                                viewModel.fetchPageDetail(page)
                            }
                        }
                    }
                } else {
                    when (val state = detailUiState) {
                        is SurahDetailUiState.Loading -> LoadingBox()
                        is SurahDetailUiState.Success -> {
                            val detailTitle = when {
                                selectedSurah != null -> selectedSurah!!.name ?: ""
                                selectedJuz != null -> "${selectedJuz}. Cüz"
                                selectedPage != null -> "Sayfa ${selectedPage}"
                                else -> ""
                            }
                            val detailSubtitle = when {
                                selectedSurah != null -> "— İNİŞ SIRASI: ${selectedSurah!!.revelationOrder ?: "-"} • ${selectedSurah!!.verseCount} AYET —"
                                selectedJuz != null -> "— BU CÜZDEKİ TÜM AYETLER —"
                                selectedPage != null -> "— BU SAYFADAKİ TÜM AYETLER —"
                                else -> ""
                            }
                            VerseList(
                                state.verses, 
                                detailTitle, 
                                detailSubtitle, 
                                selectedSurah?.id ?: 0,
                                isPlaying,
                                isPaused,
                                playbackSpeed,
                                currentPlayingVerse,
                                currentPlayingSurahId,
                                selectedFont,
                                listState = verseListState,
                                onPlayClick = { verse -> viewModel.playVerse(selectedSurah?.id ?: verse.surahId, verse.verseNumber, state.verses) },
                                onStopClick = { viewModel.stopAudio() },
                                onPauseClick = { viewModel.togglePauseResume() },
                                onSpeedClick = { viewModel.setPlaybackSpeed(it) }
                            )
                        }
                        is SurahDetailUiState.Error -> ErrorBox(state.message)
                        else -> {}
                    }
                }
            }
        }
    }
}

@Composable
fun LoadingBox() {
    Box(Modifier.fillMaxSize(), Alignment.Center) {
        CircularProgressIndicator(color = Color(0xFFFFD700))
    }
}

@Composable
fun ErrorBox(message: String) {
    Box(Modifier.fillMaxSize(), Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Bir sorun oluştu", color = Color.White, fontWeight = FontWeight.Bold)
            Text(message, color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun SurahList(surahs: List<Surah>, state: androidx.compose.foundation.lazy.LazyListState, onSurahClick: (Surah) -> Unit) {
    LazyColumn(
        state = state,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp),
    ) {
        items(surahs) { surah ->
            SurahItem(surah, onSurahClick)
            HorizontalDivider(color = Color.White.copy(alpha = 0.05f), thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
        }
    }
}

@Composable
fun SurahItem(surah: Surah, onClick: (Surah) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(surah) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(40.dp)) {
            // Basit bir çerçeve ikonu yerine yıldız kullandık
            Icon(
                imageVector = Icons.Default.PlayArrow, // Geçici simge
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = Color(0xFFFFD700).copy(alpha = 0.3f)
            )
            Text(surah.id.toString(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
        
        Spacer(Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = surah.name ?: "",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "İniş Sırası: ${surah.revelationOrder ?: "-"}",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(" • ", color = Color.White.copy(alpha = 0.3f))
                Text(
                    text = "${surah.verseCount} AYET",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        if (surah.arabicName != null) {
            Text(
                text = surah.arabicName,
                color = Color(0xFFFFD700),
                fontSize = 24.sp,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Right
            )
        }
    }
}

@Composable
fun JuzList(searchQuery: String, state: androidx.compose.foundation.lazy.LazyListState, onJuzClick: (Int) -> Unit) {
    val juzs = (1..30).filter { it.toString().contains(searchQuery) }
    LazyColumn(
        state = state,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp),
    ) {
        items(juzs) { juz ->
            SimpleListItem(number = juz, title = "${juz}. Cüz", subtitle = "Cüz") { onJuzClick(juz) }
            HorizontalDivider(color = Color.White.copy(alpha = 0.05f), thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
        }
    }
}

@Composable
fun PageList(searchQuery: String, state: androidx.compose.foundation.lazy.LazyListState, onPageClick: (Int) -> Unit) {
    val pages = (1..604).filter { it.toString().contains(searchQuery) }
    LazyColumn(
        state = state,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp),
    ) {
        items(pages) { page ->
            SimpleListItem(number = page, title = "${page}. Sayfa", subtitle = "Sayfa") { onPageClick(page) }
            HorizontalDivider(color = Color.White.copy(alpha = 0.05f), thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
        }
    }
}

@Composable
fun SimpleListItem(number: Int, title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(40.dp)) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = Color(0xFFFFD700).copy(alpha = 0.3f)
            )
            Text(number.toString(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
        Spacer(Modifier.width(16.dp))
        Column {
            Text(text = title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(text = subtitle, color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun VerseList(
    verses: List<Verse>, 
    title: String, 
    subtitle: String, 
    surahId: Int,
    isPlaying: Boolean,
    isPaused: Boolean,
    playbackSpeed: Float,
    currentPlayingVerse: Int?,
    currentPlayingSurahId: Int?,
    selectedFont: String,
    listState: androidx.compose.foundation.lazy.LazyListState = rememberLazyListState(),
    onPlayClick: (Verse) -> Unit,
    onStopClick: () -> Unit,
    onPauseClick: () -> Unit,
    onSpeedClick: (Float) -> Unit
) {
    // Otomatik Kaydırma
    LaunchedEffect(currentPlayingVerse, currentPlayingSurahId) {
        if (currentPlayingVerse != null && currentPlayingSurahId != null) {
            val index = verses.indexOfFirst { it.verseNumber == currentPlayingVerse && it.surahId == currentPlayingSurahId }
            if (index != -1) {
                // Header (index 0) olduğu için +1 ekliyoruz
                listState.animateScrollToItem(index + 1)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Surah Info Header
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFD700).copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.2f))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = title, color = Color(0xFFFFD700), fontSize = 24.sp, fontWeight = FontWeight.Black)
                        Text(text = subtitle, color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
                        
                        Spacer(Modifier.height(20.dp))
                        
                        if (surahId != 0 && surahId != 1 && surahId != 9) {
                            Text(text = "بِسْمِ اللّٰهِ الرَّحْمٰنِ الرَّح۪يمِ", color = Color.White, fontSize = 28.sp, textAlign = TextAlign.Center)
                        }
                        
                        Spacer(Modifier.height(16.dp))
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Button(
                                onClick = { 
                                    if (isPlaying) onPauseClick()
                                    else if (isPaused) onPauseClick()
                                    else verses.firstOrNull()?.let { onPlayClick(it) } 
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, 
                                    contentDescription = null, 
                                    tint = Color(0xFF002B36)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = if (isPlaying) "DURAKLAT" else if (isPaused) "DEVAM ET" else "DİNLE", 
                                    color = Color(0xFF002B36), 
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
            
            items(verses) { verse ->
                VerseItem(
                    verse = verse,
                    isCurrentPlaying = currentPlayingVerse == verse.verseNumber && currentPlayingSurahId == verse.surahId,
                    selectedFont = selectedFont,
                    onPlayClick = { onPlayClick(verse) },
                    onStopClick = onStopClick
                )
            }
        }

        // Gezer Buton ve Hız Seçici (Floating Action Button Area)
        if (isPlaying || isPaused) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 32.dp, end = 16.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    
                    // Hız Seçici Panel
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF073642).copy(alpha = 0.9f)),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Row(modifier = Modifier.padding(4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            val speeds = listOf(0.75f, 1.0f, 1.25f, 1.5f)
                            speeds.forEach { speed ->
                                FilterChip(
                                    selected = playbackSpeed == speed,
                                    onClick = { onSpeedClick(speed) },
                                    label = { Text("${speed}x", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFFFFD700),
                                        selectedLabelColor = Color(0xFF002B36),
                                        containerColor = Color.Transparent,
                                        labelColor = Color.White
                                    ),
                                    border = null,
                                    shape = CircleShape
                                )
                            }
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Durdur Butonu (Küçük)
                        SmallFloatingActionButton(
                            onClick = { onStopClick() },
                            containerColor = Color.Red.copy(alpha = 0.8f),
                            contentColor = Color.White,
                            shape = CircleShape
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Durdur")
                        }
                        
                        // Oynat/Duraklat Butonu (Büyük)
                        FloatingActionButton(
                            onClick = { onPauseClick() },
                            containerColor = Color(0xFFFFD700),
                            contentColor = Color(0xFF002B36),
                            shape = CircleShape
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Duraklat" else "Devam Et"
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VerseItem(
    verse: Verse,
    isCurrentPlaying: Boolean,
    selectedFont: String,
    onPlayClick: () -> Unit,
    onStopClick: () -> Unit
) {
    val arabicFontFamily = when(selectedFont) {
        "SansSerif" -> FontFamily.SansSerif
        "Serif" -> FontFamily.Serif
        "Monospace" -> FontFamily.Monospace
        "Cursive" -> FontFamily.Cursive
        "Uthman Taha" -> FontFamily(Font(R.font.uthman_taha))
        "Amiri" -> FontFamily.Serif // Amiri fontu eklenebilir
        else -> FontFamily.Default
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isCurrentPlaying) Color(0xFFFFD700).copy(alpha = 0.1f) else Color(0xFFFCF5E5), 
                RoundedCornerShape(16.dp)
            )
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ayet Numarası (Süslü)
            Surface(
                modifier = Modifier.size(34.dp),
                shape = CircleShape,
                color = Color(0xFF073642).copy(alpha = 0.05f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF073642).copy(alpha = 0.2f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = verse.verseNumber.toString(),
                        color = Color(0xFF073642),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
            
            // Ayet İşlemleri (Paylaş, Dinle, Kopyala vb.)
            Row {
                IconButton(onClick = { 
                    if (isCurrentPlaying) onStopClick() else onPlayClick() 
                }) {
                    Icon(
                        imageVector = if (isCurrentPlaying) Icons.Default.Close else Icons.Default.PlayArrow,
                        contentDescription = null, 
                        tint = if (isCurrentPlaying) Color(0xFFFFD700) else Color(0xFF073642).copy(alpha = 0.5f)
                    )
                }
            }
        }
        
        Spacer(Modifier.height(16.dp))
        
        // Arapça Metin (Okunaklı ve Büyük)
        Text(
            text = verse.arabicText ?: "",
            color = Color(0xFF000000), // Siyah/Koyu renk Arapça için daha iyi
            fontSize = 28.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = arabicFontFamily,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Right,
            lineHeight = 48.sp
        )
        
        val translationText = verse.translation
        if (translationText != null) {
            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = Color(0xFF073642).copy(alpha = 0.1f))
            Spacer(Modifier.height(12.dp))
            // Meal (Türkçe)
            Text(
                text = translationText,
                color = Color(0xFF1B263B),
                fontSize = 16.sp,
                lineHeight = 26.sp,
                textAlign = TextAlign.Justify,
                fontWeight = FontWeight.Normal
            )
        }
    }
}

@Composable
fun SettingsDialog(
    currentReciter: String,
    currentFont: String,
    onReciterChange: (String) -> Unit,
    onFontChange: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val reciters = listOf(
        "Mishary Rashid Alafasy" to "Alafasy_128kbps",
        "Abdurrahman As-Sudais" to "Abdurrahmaan_As-Sudais_192kbps",
        "Abdulbasit Abdussamed" to "Abdul_Basit_Murattal_64kbps",
        "Maher Al-Muaiqly" to "Maher_AlMuaiqly_64kbps",
        "Saad Al-Ghamdi" to "Ghamadi_40kbps",
        "Yasir el-Dawsari" to "Yasser_Ad-Dussary_128kbps",
        "Nasser Al-Qatami" to "Nasser_Alqatami_128kbps",
        "Salah Al-Budair" to "Salah_Al_Budair_128kbps"
    )
    
    val fonts = listOf("Default", "Uthman Taha", "SansSerif", "Serif", "Monospace", "Cursive", "Amiri")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Görünüm ve Okuyucu Ayarları", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState()) // Tüm pencereyi kaydırılabilir yapar
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Okuyucu Seçin:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Column {
                    reciters.forEach { (name, key) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onReciterChange(key) }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = currentReciter == key, 
                                onClick = { onReciterChange(key) },
                                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFFFD700), unselectedColor = Color.White.copy(0.6f))
                            )
                            Text(name, modifier = Modifier.padding(start = 8.dp), color = Color.White)
                        }
                    }
                }
                
                HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
                
                Text("Arapça Yazı Tipi:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()) // Yazı tiplerini sağa-sola kaydırır
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    fonts.forEach { font ->
                        FilterChip(
                            selected = currentFont == font,
                            onClick = { onFontChange(font) },
                            label = { Text(font, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFFFD700),
                                selectedLabelColor = Color(0xFF002B36),
                                containerColor = Color.Transparent,
                                labelColor = Color.White
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = currentFont == font,
                                borderColor = Color.White.copy(alpha = 0.3f),
                                selectedBorderColor = Color(0xFFFFD700),
                                borderWidth = 1.dp
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { 
                Text("KAPAT", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold) 
            }
        },
        containerColor = Color(0xFF073642),
        titleContentColor = Color(0xFFFFD700),
        textContentColor = Color.White
    )
}
