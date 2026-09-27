package com.example.mkat_nur.ui.quran

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.mkat_nur.R
import com.example.mkat_nur.model.QuranViewDisplayMode
import com.example.mkat_nur.model.Surah
import com.example.mkat_nur.model.Verse
import com.example.mkat_nur.util.QuranTajweedColorizer
import com.example.mkat_nur.util.QuranTransliterationHelper
import com.example.mkat_nur.util.QuranUtils
import com.example.mkat_nur.util.TajweedRule
import com.example.mkat_nur.viewmodel.QuranMode
import com.example.mkat_nur.viewmodel.QuranUiState
import com.example.mkat_nur.viewmodel.QuranViewModel
import com.example.mkat_nur.viewmodel.SurahDetailUiState
import com.example.mkat_nur.viewmodel.SurahSortMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranScreen(
    viewModel: QuranViewModel = viewModel(),
    onMenuClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val detailUiState by viewModel.detailUiState.collectAsState()
    val pagesMapState by viewModel.pagesMap.collectAsState()
    val vmActivePage by viewModel.activePage.collectAsState()

    val isPlaying by viewModel.isPlaying.collectAsState()
    val isPaused by viewModel.isPaused.collectAsState()
    val playbackSpeed by viewModel.playbackSpeed.collectAsState()
    val currentPlayingVerse by viewModel.currentPlayingVerse.collectAsState()
    val currentPlayingSurahId by viewModel.currentPlayingSurahId.collectAsState()
    val selectedReciter by viewModel.selectedReciter.collectAsState()
    val selectedFont by viewModel.selectedFont.collectAsState()
    val quranMode by viewModel.quranMode.collectAsState()
    val arabicFontSize by viewModel.arabicFontSize.collectAsState()

    // Ezan Vakti Pro State'leri
    val showTransliteration by viewModel.showTransliteration.collectAsState()
    val selectedMealSource by viewModel.selectedMealSource.collectAsState()
    val selectedMealId by viewModel.selectedMealId.collectAsState()
    val downloadedMeals by viewModel.downloadedMeals.collectAsState()
    val isDownloadingMeal by viewModel.isDownloadingMeal.collectAsState()
    val surahSortMode by viewModel.surahSortMode.collectAsState()
    val displayMode by viewModel.displayMode.collectAsState()
    val audioHighlightMode by viewModel.audioHighlightMode.collectAsState()
    val tajweedEnabled by viewModel.tajweedEnabled.collectAsState()
    val tajweedActiveRules by viewModel.tajweedActiveRules.collectAsState()
    val bookmarks by viewModel.bookmarks.collectAsState()
    val isRepeatEnabled by viewModel.isRepeatEnabled.collectAsState()

    // YALNIZCA CÜZLER SEKMESİNE ÖZEL SON KALINAN KONUM
    val lastJuzTabReadJuz by viewModel.lastJuzTabReadJuz.collectAsState()
    val lastJuzTabReadPage by viewModel.lastJuzTabReadPage.collectAsState()

    var selectedSurah by remember { mutableStateOf<Surah?>(null) }
    var selectedJuz by remember { mutableStateOf<Int?>(null) }
    var selectedPage by remember { mutableStateOf<Int?>(null) }

    val isDetailActive = selectedSurah != null || selectedJuz != null || selectedPage != null

    // Kur'an okunurken ve detay/oynatıcı aktifken ekranın kapanmasını önle
    val view = LocalView.current
    DisposableEffect(isDetailActive, isPlaying) {
        view.keepScreenOn = isDetailActive || isPlaying
        onDispose {
            view.keepScreenOn = false
        }
    }

    var activePageNumber by remember { mutableIntStateOf(1) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(0) }
    var targetScrollVerseIndex by remember { mutableStateOf<Int?>(null) }

    // Okuma otomatik sonraki sayfaya geçtiğinde activePageNumber senkronize et
    LaunchedEffect(vmActivePage) {
        if (activePageNumber != vmActivePage) {
            activePageNumber = vmActivePage
        }
    }

    // Dialog & Drawer Kontrolleri
    var showSettings by remember { mutableStateOf(false) }
    var showQuickJumpDialog by remember { mutableStateOf(false) }
    var showSideMenu by remember { mutableStateOf(false) }
    var showMeallerDialog by remember { mutableStateOf(false) }
    var showHafizlarDialog by remember { mutableStateOf(false) }
    var showSayfaGorunumuDialog by remember { mutableStateOf(false) }
    var showSecimTakipDialog by remember { mutableStateOf(false) }
    var showTecvidDialog by remember { mutableStateOf(false) }
    var showBookmarksDialog by remember { mutableStateOf(false) }
    var mushafModeNotice by remember { mutableStateOf<String?>(null) }

    val isAnyDialogOpen = showSideMenu || showSettings || showQuickJumpDialog || showMeallerDialog || showHafizlarDialog || showSayfaGorunumuDialog || showSecimTakipDialog || showTecvidDialog || showBookmarksDialog || mushafModeNotice != null

    // Geri Tuşu İşleyicisi (Tek adım geri gitme)
    BackHandler(enabled = isDetailActive || isAnyDialogOpen) {
        when {
            mushafModeNotice != null -> mushafModeNotice = null
            showSideMenu -> showSideMenu = false
            showSettings -> showSettings = false
            showQuickJumpDialog -> showQuickJumpDialog = false
            showMeallerDialog -> showMeallerDialog = false
            showHafizlarDialog -> showHafizlarDialog = false
            showSayfaGorunumuDialog -> showSayfaGorunumuDialog = false
            showSecimTakipDialog -> showSecimTakipDialog = false
            showTecvidDialog -> showTecvidDialog = false
            showBookmarksDialog -> showBookmarksDialog = false
            isDetailActive -> {
                selectedSurah = null
                selectedJuz = null
                selectedPage = null
                viewModel.stopAudio()
            }
        }
    }

    val surahListState = rememberLazyListState()
    val juzListState = rememberLazyListState()
    val pageListState = rememberLazyListState()

    val bgColors = listOf(Color(0xFF002B36), Color(0xFF073642))

    if (mushafModeNotice != null) {
        AlertDialog(
            onDismissRequest = { mushafModeNotice = null },
            title = { Text("Sayfa Görünümü", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF2C1810)) },
            text = { Text(mushafModeNotice!!, fontSize = 14.sp, color = Color(0xFF333333)) },
            confirmButton = {
                Button(onClick = { mushafModeNotice = null }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5A2B))) {
                    Text("ANLADIM", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color(0xFFFCF8EC),
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (showQuickJumpDialog) {
        QuickJumpDialog(
            surahs = (uiState as? QuranUiState.Success)?.surahs ?: emptyList(),
            sortMode = surahSortMode,
            onSortModeChange = { viewModel.setSurahSortMode(it) },
            onSelect = { juz, surahId, verseNum ->
                showQuickJumpDialog = false
                if (surahId != null) {
                    val surah = (uiState as? QuranUiState.Success)?.surahs?.find { it.id == surahId }
                    selectedSurah = surah
                    selectedJuz = null
                    selectedPage = null
                    val startPage = QuranUtils.getStartPageForSurah(surahId)
                    activePageNumber = startPage
                    targetScrollVerseIndex = (verseNum ?: 1) - 1
                    viewModel.fetchPageDetail(startPage)
                } else if (juz != null) {
                    selectedJuz = juz
                    selectedSurah = null
                    selectedPage = null
                    val startPage = QuranUtils.getJuzPageRange(juz).first
                    activePageNumber = startPage
                    viewModel.saveJuzTabLastReadPosition(juz, startPage)
                    viewModel.fetchPageDetail(startPage)
                }
            },
            onDismiss = { showQuickJumpDialog = false }
        )
    }

    if (showSideMenu) {
        SideMenuSheet(
            onItemClick = { item ->
                showSideMenu = false
                when (item) {
                    "Sureler" -> { selectedSurah = null; selectedJuz = null; selectedPage = null }
                    "Yer İmleri" -> showBookmarksDialog = true
                    "Mealler" -> showMeallerDialog = true
                    "Hafızlar" -> showHafizlarDialog = true
                    "Sayfa Görünümü" -> showSayfaGorunumuDialog = true
                    "Seçim ve Takip Ayarı" -> showSecimTakipDialog = true
                    "Metin Ayarları" -> showSettings = true
                    "Tecvid Renklendirme" -> showTecvidDialog = true
                }
            },
            onDismiss = { showSideMenu = false }
        )
    }

    if (showMeallerDialog) {
        MeallerDialog(
            showTransliteration = showTransliteration,
            selectedMealId = selectedMealId,
            downloadedMeals = downloadedMeals,
            isDownloadingMeal = isDownloadingMeal,
            onTransliterationChange = { viewModel.setShowTransliteration(it) },
            onMealSelect = { id, title -> 
                viewModel.setSelectedMeal(id, title)
                viewModel.fetchPageDetail(activePageNumber)
            },
            onMealDownload = { id -> viewModel.downloadMeal(id) },
            onDismiss = { showMeallerDialog = false }
        )
    }

    if (showHafizlarDialog) {
        HafizlarDialog(
            currentReciter = selectedReciter,
            onReciterChange = { viewModel.setReciter(it) },
            onDismiss = { showHafizlarDialog = false }
        )
    }

    if (showSayfaGorunumuDialog) {
        SayfaGorunumuDialog(
            currentMode = displayMode,
            onModeSelect = { mode ->
                viewModel.setDisplayMode(mode)
                if (mode == QuranViewDisplayMode.KURAN_SAYFASI) {
                    mushafModeNotice = "Mushaf (Kur'an Sayfası) görünümüne geçildi. Ayetler orijinal sayfa düzeni ve hat yazısıyla peş peşe sergilenir."
                }
            },
            onDismiss = { showSayfaGorunumuDialog = false }
        )
    }

    if (showSecimTakipDialog) {
        SecimTakipDialog(
            currentMode = audioHighlightMode,
            onModeSelect = { viewModel.setAudioHighlightMode(it) },
            onDismiss = { showSecimTakipDialog = false }
        )
    }

    if (showTecvidDialog) {
        TecvidDialog(
            isEnabled = tajweedEnabled,
            activeRules = tajweedActiveRules,
            onEnabledChange = { viewModel.setTajweedEnabled(it) },
            onToggleRule = { viewModel.toggleTajweedRule(it) },
            onDismiss = { showTecvidDialog = false }
        )
    }

    if (showBookmarksDialog) {
        BookmarksDialog(
            bookmarks = bookmarks,
            onSelectBookmark = { verseKey ->
                showBookmarksDialog = false
                val parts = verseKey.split(":")
                val surahId = parts.firstOrNull()?.toIntOrNull() ?: 1
                val vNum = parts.getOrNull(1)?.toIntOrNull() ?: 1
                selectedSurah = (uiState as? QuranUiState.Success)?.surahs?.find { it.id == surahId }
                val startPage = QuranUtils.getStartPageForSurah(surahId)
                activePageNumber = startPage
                targetScrollVerseIndex = vNum - 1
                viewModel.fetchPageDetail(startPage)
            },
            onDismiss = { showBookmarksDialog = false }
        )
    }

    if (showSettings) {
        SettingsDialog(
            currentReciter = selectedReciter,
            currentFont = selectedFont,
            currentMode = quranMode,
            currentFontSize = arabicFontSize,
            onReciterChange = { viewModel.setReciter(it) },
            onFontChange = { viewModel.setFont(it) },
            onModeChange = { viewModel.setQuranMode(it) },
            onFontSizeChange = { viewModel.setArabicFontSize(it) },
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
                            val surahsList = (uiState as? QuranUiState.Success)?.surahs ?: emptyList()
                            val pageVersesKey = "page_${activePageNumber}_trans_${selectedMealId}"
                            val activeVerses = pagesMapState[pageVersesKey] ?: (detailUiState as? SurahDetailUiState.Success)?.verses ?: emptyList()
                            val currentSurahId = currentPlayingSurahId ?: activeVerses.firstOrNull()?.surahId ?: selectedSurah?.id ?: 1
                            val currentSurahObj = surahsList.find { it.id == currentSurahId } ?: selectedSurah
                            val displaySurahName = currentSurahObj?.name ?: "Fâtiha"
                            val totalVersesCount = currentSurahObj?.verseCount ?: activeVerses.size
                            val activeVerseNum = currentPlayingVerse ?: activeVerses.firstOrNull()?.verseNumber ?: 1
                            val displayJuzNum = QuranUtils.getJuzByPage(activePageNumber)

                            val titleText = if (selectedSurah == null && selectedJuz == null && selectedPage == null) {
                                "KUR'AN-I KERİM"
                            } else {
                                "Cüz: $displayJuzNum Sayfa: $activePageNumber • $displaySurahName ($activeVerseNum / $totalVersesCount Ayet)"
                            }
                            Text(
                                text = titleText,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFD700),
                                fontSize = 12.sp,
                                maxLines = 1
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = {
                                if (selectedSurah == null && selectedJuz == null && selectedPage == null) {
                                    showQuickJumpDialog = true
                                } else {
                                    selectedSurah = null
                                    selectedJuz = null
                                    selectedPage = null
                                    viewModel.stopAudio()
                                }
                            }) {
                                Icon(
                                    imageVector = if (selectedSurah == null && selectedJuz == null && selectedPage == null) Icons.Default.Search else Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Hızlı Erişim",
                                    tint = Color.White
                                )
                            }
                        },
                        actions = {
                            IconButton(onClick = { showSideMenu = true }) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = "Menü",
                                    tint = Color.White
                                )
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
                                Text("SURELER", modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp), fontWeight = FontWeight.Bold, fontSize = 11.sp, color = if(selectedTab == 0) Color(0xFFFFD700) else Color.White.copy(0.6f), maxLines = 1)
                            }
                            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                                Text("CÜZLER", modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp), fontWeight = FontWeight.Bold, fontSize = 11.sp, color = if(selectedTab == 1) Color(0xFFFFD700) else Color.White.copy(0.6f), maxLines = 1)
                            }
                            Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }) {
                                Text("SAYFALAR", modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp), fontWeight = FontWeight.Bold, fontSize = 11.sp, color = if(selectedTab == 2) Color(0xFFFFD700) else Color.White.copy(0.6f), maxLines = 1)
                            }
                            Tab(selected = selectedTab == 3, onClick = { selectedTab = 3 }) {
                                Text("HATİM", modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp), fontWeight = FontWeight.Bold, fontSize = 11.sp, color = if(selectedTab == 3) Color(0xFFFFD700) else Color.White.copy(0.6f), maxLines = 1)
                            }
                        }

                        if (selectedTab != 3) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                                placeholder = { Text("Sure veya ayet ara...", color = Color.White.copy(alpha = 0.5f), fontSize = 13.sp) },
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
            }
        ) { padding ->
            Box(modifier = Modifier.padding(padding)) {
                if (selectedSurah == null && selectedJuz == null && selectedPage == null) {
                    when (selectedTab) {
                        0 -> {
                            when (val state = uiState) {
                                is QuranUiState.Loading -> LoadingBox()
                                is QuranUiState.Success -> {
                                    val normalizedQuery = QuranUtils.normalizeForSearch(searchQuery)
                                    val filteredSurahs = state.surahs.filter { 
                                        val normName = QuranUtils.normalizeForSearch(it.name)
                                        val normArabic = QuranUtils.normalizeForSearch(it.arabicName)
                                        normName.contains(normalizedQuery) || 
                                        normArabic.contains(normalizedQuery) ||
                                        it.id.toString() == searchQuery.trim() 
                                    }
                                    val sortedSurahs = when (surahSortMode) {
                                        SurahSortMode.KURAN -> filteredSurahs.sortedBy { it.id }
                                        SurahSortMode.NUZUL -> filteredSurahs.sortedBy { it.revelationOrder ?: it.id }
                                        SurahSortMode.ALFABETIK -> filteredSurahs.sortedBy { it.name ?: "" }
                                    }
                                    SurahList(sortedSurahs, surahListState) { surah ->
                                        selectedSurah = surah
                                        selectedJuz = null
                                        val startPage = QuranUtils.getStartPageForSurah(surah.id)
                                        activePageNumber = startPage
                                        viewModel.stopAudio()
                                        viewModel.fetchPageDetail(startPage)
                                    }
                                }
                                is QuranUiState.Error -> ErrorBox(state.message)
                            }
                        }
                        1 -> {
                            Column(modifier = Modifier.fillMaxSize()) {
                                // YALNIZCA CÜZLER SEKMESİNDEN YAPILAN OKUMALARIN YERİNİ HATIRLATAN KART
                                Surface(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                                    color = Color(0xFFFFD700).copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.4f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text("Son Kalınan Cüz", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFFFFD700))
                                            Text("$lastJuzTabReadJuz. Cüz — Sayfa $lastJuzTabReadPage", fontSize = 12.sp, color = Color.White)
                                        }
                                        Button(
                                            onClick = {
                                                selectedJuz = lastJuzTabReadJuz
                                                selectedSurah = null
                                                activePageNumber = lastJuzTabReadPage
                                                viewModel.fetchPageDetail(lastJuzTabReadPage)
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("DEVAM ET", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF2C1810))
                                        }
                                    }
                                }

                                JuzList(searchQuery, juzListState) { juz ->
                                    selectedJuz = juz
                                    selectedSurah = null
                                    val startPage = QuranUtils.getJuzPageRange(juz).first
                                    activePageNumber = startPage
                                    viewModel.saveJuzTabLastReadPosition(juz, startPage)
                                    viewModel.fetchPageDetail(startPage)
                                }
                            }
                        }
                        2 -> {
                            PageList(searchQuery, pageListState) { page ->
                                selectedPage = page
                                selectedSurah = null
                                selectedJuz = null
                                activePageNumber = page
                                viewModel.fetchPageDetail(page)
                            }
                        }
                        3 -> {
                            // Hatim Alanı (Başlangıç Cüzü Seçimi, Cüz Dağıtımı & Hatim Duası)
                            HatimTab(
                                viewModel = viewModel,
                                onOpenJuz = { juz ->
                                    selectedJuz = null
                                    selectedSurah = null
                                    selectedPage = juz
                                    val startPage = QuranUtils.getJuzPageRange(juz).first
                                    activePageNumber = startPage
                                    viewModel.fetchPageDetail(startPage)
                                }
                            )
                        }
                    }
                } else {
                    // Sayfa Bazlı Okuma Ekranı (604 Sayfa - Soldan Sağa Büyüyen Kur'an Sayfa Düzeni)
                    val surahsList = (uiState as? QuranUiState.Success)?.surahs ?: emptyList()
                    val pagePagerState = rememberPagerState(
                        initialPage = (activePageNumber - 1).coerceIn(0, 603),
                        pageCount = { 604 }
                    )

                    // Seçimler dışarıdan değiştiğinde (Hızlı Erişim / Arama / Sure seçimi) Pager'ı kaydır
                    LaunchedEffect(activePageNumber) {
                        val targetIndex = (activePageNumber - 1).coerceIn(0, 603)
                        if (pagePagerState.currentPage != targetIndex && !pagePagerState.isScrollInProgress) {
                            pagePagerState.scrollToPage(targetIndex)
                        }
                    }

                    // Kullanıcı Pager'ı elle kaydırdığında sayfa numarasını ve veriyi yükle (Döngüyü keser)
                    LaunchedEffect(pagePagerState.currentPage) {
                        val newPage = pagePagerState.currentPage + 1
                        if (activePageNumber != newPage) {
                            activePageNumber = newPage
                            
                            // Yalnızca kullanıcı Cüzler sekmesinden Cüz Okuma modundaysa konumu güncelle
                            if (selectedJuz != null) {
                                val currentJuz = QuranUtils.getJuzByPage(newPage)
                                viewModel.saveJuzTabLastReadPosition(currentJuz, newPage)
                            }

                            viewModel.fetchPageDetail(newPage)
                        }
                    }

                    HorizontalPager(
                        state = pagePagerState,
                        reverseLayout = true, // Kur'an Kitap Düzeni: Soldan sağa çekince sayfa büyür (1->2->3)
                        modifier = Modifier.fillMaxSize()
                    ) { pageIdx ->
                        val pageNum = pageIdx + 1
                        val key = "page_${pageNum}_trans_$selectedMealId"
                        val cachedPageVerses = pagesMapState[key]

                        if (cachedPageVerses != null && cachedPageVerses.isNotEmpty()) {
                            val currentSurahId = currentPlayingSurahId ?: cachedPageVerses.firstOrNull()?.surahId ?: selectedSurah?.id ?: 1
                            val currentSurah = surahsList.find { it.id == currentSurahId } ?: selectedSurah
                            val currentSurahName = currentSurah?.name ?: "Fâtiha"
                            val currentSurahArabic = currentSurah?.arabicName ?: "الفاتحة"

                            if (displayMode == QuranViewDisplayMode.KURAN_SAYFASI) {
                                // Mushaf (Kuran Sayfası) Görünümü (Oynatıcı Barı, Vurgu & Ortalanmış Otomatik Kaydırma)
                                MushafView(
                                    verses = cachedPageVerses,
                                    surah = currentSurah,
                                    surahsList = surahsList,
                                    selectedSurahId = selectedSurah?.id,
                                    targetScrollVerseIndex = targetScrollVerseIndex,
                                    selectedFont = selectedFont,
                                    arabicFontSize = arabicFontSize,
                                    isPlaying = isPlaying,
                                    isPaused = isPaused,
                                    playbackSpeed = playbackSpeed,
                                    currentPlayingVerse = currentPlayingVerse,
                                    currentPlayingSurahId = currentPlayingSurahId,
                                    selectedMealSource = selectedMealSource,
                                    bookmarks = bookmarks,
                                    isRepeatEnabled = isRepeatEnabled,
                                    tajweedEnabled = tajweedEnabled,
                                    tajweedActiveRules = tajweedActiveRules,
                                    onPlayClick = { verse -> viewModel.playVerse(verse.surahId, verse.verseNumber, cachedPageVerses) },
                                    onStopClick = { viewModel.stopAudio() },
                                    onPauseClick = { viewModel.togglePauseResume() },
                                    onSpeedClick = { viewModel.setPlaybackSpeed(it) },
                                    onRepeatToggle = { viewModel.toggleRepeat() },
                                    onBookmarkToggle = { verseKey -> viewModel.toggleBookmark(verseKey) }
                                )
                            } else {
                                // Metin Görünümü (Yalnızca o sayfadaki ayetler)
                                val detailTitle = "$currentSurahName — Sayfa $pageNum"
                                val detailSubtitle = "— CÜZ: ${QuranUtils.getJuzByPage(pageNum)} • SAYFA: $pageNum —"
                                
                                EzanVaktiVerseList(
                                    verses = cachedPageVerses,
                                    surahNameArabic = currentSurahArabic,
                                    detailTitle = detailTitle,
                                    detailSubtitle = detailSubtitle,
                                    surahId = currentSurahId,
                                    surahsList = surahsList,
                                    selectedSurahId = selectedSurah?.id,
                                    targetScrollVerseIndex = targetScrollVerseIndex,
                                    isPlaying = isPlaying,
                                    isPaused = isPaused,
                                    playbackSpeed = playbackSpeed,
                                    currentPlayingVerse = currentPlayingVerse,
                                    currentPlayingSurahId = currentPlayingSurahId,
                                    selectedFont = selectedFont,
                                    quranMode = quranMode,
                                    arabicFontSize = arabicFontSize,
                                    showTransliteration = showTransliteration,
                                    selectedMealSource = selectedMealSource,
                                    bookmarks = bookmarks,
                                    isRepeatEnabled = isRepeatEnabled,
                                    tajweedEnabled = tajweedEnabled,
                                    tajweedActiveRules = tajweedActiveRules,
                                    onPlayClick = { verse -> viewModel.playVerse(verse.surahId, verse.verseNumber, cachedPageVerses) },
                                    onStopClick = { viewModel.stopAudio() },
                                    onPauseClick = { viewModel.togglePauseResume() },
                                    onSpeedClick = { viewModel.setPlaybackSpeed(it) },
                                    onRepeatToggle = { viewModel.toggleRepeat() },
                                    onBookmarkToggle = { verseKey -> viewModel.toggleBookmark(verseKey) }
                                )
                            }
                        } else {
                            when (val state = detailUiState) {
                                is SurahDetailUiState.Loading -> LoadingBox()
                                is SurahDetailUiState.Success -> {
                                    val pageVerses = state.verses
                                    val currentSurahId = currentPlayingSurahId ?: pageVerses.firstOrNull()?.surahId ?: selectedSurah?.id ?: 1
                                    val currentSurah = surahsList.find { it.id == currentSurahId } ?: selectedSurah
                                    val currentSurahName = currentSurah?.name ?: "Fâtiha"
                                    val currentSurahArabic = currentSurah?.arabicName ?: "الفاتحة"

                                    if (displayMode == QuranViewDisplayMode.KURAN_SAYFASI) {
                                        MushafView(
                                            verses = pageVerses,
                                            surah = currentSurah,
                                            surahsList = surahsList,
                                            selectedSurahId = selectedSurah?.id,
                                            targetScrollVerseIndex = targetScrollVerseIndex,
                                            selectedFont = selectedFont,
                                            arabicFontSize = arabicFontSize,
                                            isPlaying = isPlaying,
                                            isPaused = isPaused,
                                            playbackSpeed = playbackSpeed,
                                            currentPlayingVerse = currentPlayingVerse,
                                            currentPlayingSurahId = currentPlayingSurahId,
                                            selectedMealSource = selectedMealSource,
                                            bookmarks = bookmarks,
                                            isRepeatEnabled = isRepeatEnabled,
                                            tajweedEnabled = tajweedEnabled,
                                            tajweedActiveRules = tajweedActiveRules,
                                            onPlayClick = { verse -> viewModel.playVerse(verse.surahId, verse.verseNumber, pageVerses) },
                                            onStopClick = { viewModel.stopAudio() },
                                            onPauseClick = { viewModel.togglePauseResume() },
                                            onSpeedClick = { viewModel.setPlaybackSpeed(it) },
                                            onRepeatToggle = { viewModel.toggleRepeat() },
                                            onBookmarkToggle = { verseKey -> viewModel.toggleBookmark(verseKey) }
                                        )
                                    } else {
                                        EzanVaktiVerseList(
                                            verses = pageVerses,
                                            surahNameArabic = currentSurahArabic,
                                            detailTitle = "$currentSurahName — Sayfa $pageNum",
                                            detailSubtitle = "— CÜZ: ${QuranUtils.getJuzByPage(pageNum)} • SAYFA: $pageNum —",
                                            surahId = currentSurahId,
                                            surahsList = surahsList,
                                            selectedSurahId = selectedSurah?.id,
                                            targetScrollVerseIndex = targetScrollVerseIndex,
                                            isPlaying = isPlaying,
                                            isPaused = isPaused,
                                            playbackSpeed = playbackSpeed,
                                            currentPlayingVerse = currentPlayingVerse,
                                            currentPlayingSurahId = currentPlayingSurahId,
                                            selectedFont = selectedFont,
                                            quranMode = quranMode,
                                            arabicFontSize = arabicFontSize,
                                            showTransliteration = showTransliteration,
                                            selectedMealSource = selectedMealSource,
                                            bookmarks = bookmarks,
                                            isRepeatEnabled = isRepeatEnabled,
                                            tajweedEnabled = tajweedEnabled,
                                            tajweedActiveRules = tajweedActiveRules,
                                            onPlayClick = { verse -> viewModel.playVerse(verse.surahId, verse.verseNumber, pageVerses) },
                                            onStopClick = { viewModel.stopAudio() },
                                            onPauseClick = { viewModel.togglePauseResume() },
                                            onSpeedClick = { viewModel.setPlaybackSpeed(it) },
                                            onRepeatToggle = { viewModel.toggleRepeat() },
                                            onBookmarkToggle = { verseKey -> viewModel.toggleBookmark(verseKey) }
                                        )
                                    }
                                }
                                is SurahDetailUiState.Error -> ErrorBox(state.message)
                                else -> LoadingBox()
                            }
                        }
                    }
                }
            }
        }
    }
}

// Hatim Sekmesi (1..30 Numaratör Grid, Onaylı Silme, İsim Dağıtımı & Hatim Duası)
@Composable
fun HatimTab(
    viewModel: QuranViewModel,
    onOpenJuz: (Int) -> Unit
) {
    val context = LocalContext.current
    val hatimStartJuz by viewModel.hatimStartJuz.collectAsState()
    val hatimCompletedJuzs by viewModel.hatimCompletedJuzs.collectAsState()
    val hatimAssignments by viewModel.hatimAssignments.collectAsState()
    val isHatimActive by viewModel.isHatimActive.collectAsState()

    var selectedStartJuz by remember { mutableIntStateOf(hatimStartJuz) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showDuaDialog by remember { mutableStateOf(false) }
    var editingJuzAssignment by remember { mutableStateOf<Int?>(null) }
    var personNameInput by remember { mutableStateOf("") }

    val sequence = remember(selectedStartJuz) { viewModel.getHatimSequence(selectedStartJuz) }

    val completedCount = hatimCompletedJuzs.size
    val progressPercent = ((completedCount / 30f) * 100).toInt()

    // 30 Cüz de tamamlandığında otomatik Hatim Duasını göster
    LaunchedEffect(completedCount) {
        if (completedCount == 30) {
            showDuaDialog = true
        }
    }

    if (showDuaDialog) {
        HatimDuasiDialog(onDismiss = { showDuaDialog = false })
    }

    if (editingJuzAssignment != null) {
        val jNum = editingJuzAssignment!!
        AlertDialog(
            onDismissRequest = { editingJuzAssignment = null },
            title = { Text("$jNum. Cüz Okuyucusu / Kişi", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF2C1810)) },
            text = {
                Column {
                    Text("Bu cüzü okuyacak kişinin ismini yazın veya temizleyin:", fontSize = 12.sp, color = Color.Gray)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = personNameInput,
                        onValueChange = { personNameInput = it },
                        placeholder = { Text("Örn: Ahmet Bey / Ayşe Hanım") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.assignHatimJuz(jNum, personNameInput)
                        editingJuzAssignment = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5A2B))
                ) {
                    Text("KAYDET", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { editingJuzAssignment = null }) {
                    Text("İPTAL", color = Color.Gray)
                }
            },
            containerColor = Color(0xFFFCF8EC),
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Hatimi Sil", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Red) },
            text = { Text("Mevcut hatiminizi ve kaydettiğiniz tüm ilerlemeyi tamamen silmek istediğinize emin misiniz? Bu işlem geri alınamaz.", fontSize = 13.sp, color = Color(0xFF2C1810)) },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmDialog = false
                        viewModel.resetHatim()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("HATİMİ SİL", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("İPTAL", color = Color.Gray)
                }
            },
            containerColor = Color(0xFFFCF8EC),
            shape = RoundedCornerShape(16.dp)
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (!isHatimActive) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFCF8EC)),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.5.dp, Color(0xFF8B5A2B))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Yeni Hatim Başlat", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF2C1810))
                        Spacer(Modifier.height(8.dp))
                        Text("Hatime başlayacağınız Cüz'ü seçin. Seçtiğiniz Cüz'den başlayarak 30 Cüz döngüsel sırayla tamamlanır.", fontSize = 12.sp, color = Color.Gray)
                        Spacer(Modifier.height(16.dp))

                        Text("Başlangıç Cüzü: $selectedStartJuz. Cüz", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF8B5A2B))
                        Slider(
                            value = selectedStartJuz.toFloat(),
                            onValueChange = { selectedStartJuz = it.toInt().coerceIn(1, 30) },
                            valueRange = 1f..30f,
                            steps = 28,
                            colors = SliderDefaults.colors(thumbColor = Color(0xFF8B5A2B), activeTrackColor = Color(0xFF8B5A2B))
                        )

                        Spacer(Modifier.height(8.dp))
                        Text("Veya Numaratörden Seçin:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF8B5A2B))
                        Spacer(Modifier.height(8.dp))

                        @OptIn(ExperimentalLayoutApi::class)
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            (1..30).forEach { num ->
                                val isSelected = selectedStartJuz == num
                                Surface(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clickable { selectedStartJuz = num },
                                    shape = CircleShape,
                                    color = if (isSelected) Color(0xFF8B5A2B) else Color(0xFFFFD700).copy(alpha = 0.2f),
                                    border = BorderStroke(1.dp, if (isSelected) Color(0xFF8B5A2B) else Color(0xFFFFD700).copy(alpha = 0.5f))
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(text = num.toString(), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isSelected) Color.White else Color(0xFF2C1810))
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))
                        val previewText = sequence.take(5).joinToString(" ➔ ") + " ... ➔ " + sequence.takeLast(3).joinToString(" ➔ ")
                        Text("Sıralama Düzeni: $previewText", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color(0xFF2C1810))
                        Spacer(Modifier.height(16.dp))

                        Button(
                            onClick = { viewModel.startHatim(selectedStartJuz) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5A2B)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("HATİMİ BAŞLAT", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        } else {
            val activeSequence = viewModel.getHatimSequence(hatimStartJuz)
            val nextJuzToRead = activeSequence.firstOrNull { !hatimCompletedJuzs.contains(it) } ?: activeSequence.first()

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFCF8EC)),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.5.dp, Color(0xFF8B5A2B))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text("Aktif Hatim İlerlemesi", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF2C1810))
                                Text("Başlangıç: $hatimStartJuz. Cüz", fontSize = 11.sp, color = Color.Gray)
                            }
                            Text("$completedCount / 30 Cüz (%$progressPercent)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF8B5A2B))
                        }
                        Spacer(Modifier.height(12.dp))
                        LinearProgressIndicator(
                            progress = completedCount / 30f,
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                            color = Color(0xFF8B5A2B),
                            trackColor = Color.LightGray.copy(alpha = 0.4f)
                        )
                        Spacer(Modifier.height(16.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { onOpenJuz(nextJuzToRead) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5A2B)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Sıradaki: $nextJuzToRead. Cüzü Oku", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                            }

                            OutlinedButton(
                                onClick = { showDeleteConfirmDialog = true },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                                border = BorderStroke(1.dp, Color.Red),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Sil", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        // Hatim Listesini Paylaş Butonu (WhatsApp / Gruba Paylaşım)
                        Button(
                            onClick = {
                                val shareText = viewModel.getHatimShareText()
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, shareText)
                                }
                                context.startActivity(Intent.createChooser(intent, "Hatim Listesini Paylaş"))
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("HATİM LİSTESİNİ PAYLAŞ (WHATSAPP)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                        }

                        if (completedCount == 30) {
                            Spacer(Modifier.height(8.dp))
                            Button(
                                onClick = { showDuaDialog = true },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFF2C1810))
                                Spacer(Modifier.width(8.dp))
                                Text("HATİM DUASINI OKU", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF2C1810))
                            }
                        }
                    }
                }
            }

            item {
                Text("Cüz Dağıtımı ve Tamamlama Durumu", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFFFFD700))
            }

            itemsIndexed(activeSequence) { idx, juzNum ->
                val isCompleted = hatimCompletedJuzs.contains(juzNum)
                val assignedPerson = hatimAssignments[juzNum]
                val range = QuranUtils.getJuzPageRange(juzNum)

                Card(
                    modifier = Modifier.fillMaxWidth().clickable { onOpenJuz(juzNum) },
                    colors = CardDefaults.cardColors(containerColor = if (isCompleted) Color(0xFFE8F5E9) else Color(0xFFFCF8EC)),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, if (isCompleted) Color(0xFF4CAF50) else Color.LightGray)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(32.dp),
                            shape = CircleShape,
                            color = if (isCompleted) Color(0xFF4CAF50) else Color(0xFF8B5A2B)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("#${idx + 1}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text("$juzNum. Cüz", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF2C1810))
                            Text("Sayfa ${range.first} - ${range.second}", fontSize = 11.sp, color = Color.Gray)
                            val personDisplay = if (!assignedPerson.isNullOrBlank()) "👤 $assignedPerson" else "🟩 [Boşta - Dokun & Kişi Ata]"
                            Text(
                                text = personDisplay,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (!assignedPerson.isNullOrBlank()) Color(0xFF1565C0) else Color(0xFF2E7D32),
                                modifier = Modifier.clickable {
                                    editingJuzAssignment = juzNum
                                    personNameInput = assignedPerson ?: ""
                                }
                            )
                        }

                        Checkbox(
                            checked = isCompleted,
                            onCheckedChange = { viewModel.toggleHatimJuzCompleted(juzNum) },
                            colors = CheckboxDefaults.colors(checkedColor = Color(0xFF4CAF50))
                        )

                        TextButton(onClick = { onOpenJuz(juzNum) }) {
                            Text("OKU", fontWeight = FontWeight.Bold, color = Color(0xFF8B5A2B))
                        }
                    }
                }
            }
        }
    }
}

// Hatim Duası Diyaloğu (Arapça & Türkçe Anlamı)
@Composable
fun HatimDuasiDialog(
    onDismiss: () -> Unit
) {
    var selectedLanguage by remember { mutableIntStateOf(0) } // 0: Arapça, 1: Türkçe

    val arabicDua = """
        الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ ، وَالْعَاقِبَةُ لِلْمُتَّقِينَ ، وَلاَ عُدْوَانَ إِلاَّ عَلَى الظَّالِمِينَ .
        وَالصَّلاَةُ وَالسَّلاَمُ عَلَى رَسُولِنَا مُحَمَّدٍ وَآلِهِ وَصَحْبِهِ أَجْمَعِينَ .
        
        اللَّهُمَّ رَبَّنَا يَا رَبَّنَا تَقَبَّلْ مِنَّا إِنَّكَ أَنْتَ السَّمِيعُ الْعَلِيمُ ، وَتُبْ عَلَيْنَا يَا مَوْلاَنَا إِنَّكَ أَنْتَ التَّوَّابُ الرَّحِيمُ .
        وَاهْدِنَا وَوَفِّقْنَا إِلَى الْحَقِّ وَإِلَى طَرِيقٍ مُسْتَقِيمٍ بِبَرَكَةِ الْقُرْآنِ الْعَظِيمِ .
        
        اللَّهُمَّ ارْحَمْنَا بِالْقُرْآنِ وَاجْعَلْهُ لَنَا إِمَاماً وَنُوراً وَهُدًى وَرَحْمَةً .
        اللَّهُمَّ ذَكِّرْنَا مِنْهُ مَا نَسِينَا وَعَلِّمْنَا مِنْهُ مَا جَهِلْنَا وَارْزُقْنَا تِلاَوَتَهُ آنَاءَ اللَّيْلِ وَأَطْرَافَ النَّهَارِ وَاجْعَلْهُ لَنَا حُجَّةً يَا رَبَّ الْعَالَمِينَ .
    """.trimIndent()

    val turkishDua = """
        Hamd alemlerin Rabbi olan Allah'a mahsustur. Güzel akıbet takva sahiplerinindir. Salat ve selam Peygamberimiz Hz. Muhammed (s.a.v.)'e, onun âline ve tüm ashabına olsun.
        
        Ey Rabbimiz! Bizden kabul buyur; şüphesiz sen her şeyi işiten ve bilensin. Ey Mevlamız! Tövbelerimizi kabul eyle; şüphesiz sen tövbeleri çokça kabul eden ve merhamet edensin.
        
        Yüce Kur'an'ın bereketiyle bizi hakka ve dosdoğru yola ilet. Allah'ım! Kur'an ile bize merhamet eyle; onu bize önder, nur, hidayet ve rahmet kıl.
        
        Allah'ım! Unuttuklarımızı Kur'an ile bize hatırlat, belmediklerimizi bellet. Gece saatlerinde ve gündüz vakitlerinde onu okumayı nasip eyle. Ey Alemlerin Rabbi! Kur'an'ı lehimize hüccet kıl. Âmin!
    """.trimIndent()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text("❖ HATİM DUASI ❖", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color(0xFF8B5A2B))
                Spacer(Modifier.height(8.dp))
                TabRow(selectedTabIndex = selectedLanguage, containerColor = Color.Transparent, contentColor = Color(0xFF8B5A2B)) {
                    Tab(selected = selectedLanguage == 0, onClick = { selectedLanguage = 0 }) {
                        Text("Arapça", modifier = Modifier.padding(8.dp), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Tab(selected = selectedLanguage == 1, onClick = { selectedLanguage = 1 }) {
                        Text("Türkçe Anlamı", modifier = Modifier.padding(8.dp), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                if (selectedLanguage == 0) {
                    Text(
                        text = arabicDua,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2C1810),
                        textAlign = TextAlign.Right,
                        lineHeight = 30.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Text(
                        text = turkishDua,
                        fontSize = 14.sp,
                        color = Color(0xFF333333),
                        lineHeight = 22.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5A2B)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("ÂMİN (KAPAT)", fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        containerColor = Color(0xFFFCF8EC),
        shape = RoundedCornerShape(16.dp)
    )
}

// Bismillah Başlık Dekorasyonu (Sayfadaki yeni sure başlangıçlarında)
@Composable
fun BismillahHeaderDecoration(
    fontFamily: FontFamily = FontFamily(Font(R.font.uthman_taha))
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 16.dp)
            .background(Color(0xFFFCF8EC), RoundedCornerShape(8.dp))
            .border(1.dp, Color(0xFF8B5A2B).copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = fontFamily,
            color = Color(0xFF8B5A2B),
            textAlign = TextAlign.Center
        )
    }
}

// Mushaf / Kuran Sayfası Görünümü (Ekran Ortasında Odaklanan Ayet & Doğru Sure Adı)
@Composable
fun MushafView(
    verses: List<Verse>,
    surah: Surah?,
    surahsList: List<Surah>,
    selectedSurahId: Int?,
    targetScrollVerseIndex: Int?,
    selectedFont: String,
    arabicFontSize: Float,
    isPlaying: Boolean,
    isPaused: Boolean,
    playbackSpeed: Float,
    currentPlayingVerse: Int?,
    currentPlayingSurahId: Int?,
    selectedMealSource: String,
    bookmarks: Set<String>,
    isRepeatEnabled: Boolean,
    tajweedEnabled: Boolean,
    tajweedActiveRules: Set<TajweedRule>,
    onPlayClick: (Verse) -> Unit,
    onStopClick: () -> Unit,
    onPauseClick: () -> Unit,
    onSpeedClick: (Float) -> Unit,
    onRepeatToggle: () -> Unit,
    onBookmarkToggle: (String) -> Unit
) {
    var showImageMushaf by remember { mutableStateOf(false) }
    val pageNumber = verses.firstOrNull()?.pageNumber ?: 1
    val surahName = surah?.arabicName ?: "الفاتحة"
    val surahTitle = surah?.name ?: "Fâtiha"

    val mushafListState = rememberLazyListState()

    var lastHandledVerseTarget by remember { mutableStateOf<Pair<Int?, Int?>>(null to null) }

    // Sadece O an dinlenen ayet değiştiğinde veya Hızlı Erişim/Arama yapıldığında 1 kez kaydır (Kilitlenmeyi ve çakışmayı önler)
    LaunchedEffect(currentPlayingVerse, currentPlayingSurahId, targetScrollVerseIndex) {
        val currentTarget = currentPlayingVerse to currentPlayingSurahId
        if (currentPlayingVerse != null && currentPlayingSurahId != null && currentTarget != lastHandledVerseTarget) {
            lastHandledVerseTarget = currentTarget
            val idx = verses.indexOfFirst { it.surahId == currentPlayingSurahId && it.verseNumber == currentPlayingVerse }
            if (idx >= 0) {
                mushafListState.animateScrollToItem(idx, scrollOffset = -250)
            }
        } else if (targetScrollVerseIndex != null && targetScrollVerseIndex in verses.indices) {
            mushafListState.animateScrollToItem(targetScrollVerseIndex, scrollOffset = -50)
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFFAF6EE))) {
        Column(modifier = Modifier.fillMaxSize().padding(bottom = 90.dp)) {
            // Üst Bar: Mod Geçişi (Hat / Resim)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFE8E3D0))
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Sayfa $pageNumber • $surahName",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2C1810),
                    fontSize = 13.sp
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = !showImageMushaf,
                        onClick = { showImageMushaf = false },
                        label = { Text("Hat Metin", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF8B5A2B), selectedLabelColor = Color.White)
                    )
                    FilterChip(
                        selected = showImageMushaf,
                        onClick = { showImageMushaf = true },
                        label = { Text("Mushaf Resmi", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF8B5A2B), selectedLabelColor = Color.White)
                    )
                }
            }

            if (showImageMushaf) {
                // Orijinal Mushaf Sayfa Resmi
                MushafImagePage(pageNumber = pageNumber)
            } else {
                // Hat Metin Mushaf Görünümü (Ayet ayet listeleme, Sure Bitiş/Başlangıç Başlıkları ve O an okunan ayetin ORTALANMASI)
                val arabicFontFamily = when {
                    selectedFont.contains("Me Quran") || selectedFont.contains("Diyanet") -> FontFamily(Font(R.font.me_quran))
                    selectedFont.contains("Scheherazade") || selectedFont.contains("Şehrazat") -> FontFamily(
                        Font(R.font.scheherazade_new_regular, FontWeight.Normal),
                        Font(R.font.scheherazade_new_medium, FontWeight.Medium),
                        Font(R.font.scheherazade_new_semibold, FontWeight.SemiBold),
                        Font(R.font.scheherazade_new_bold, FontWeight.Bold)
                    )
                    else -> FontFamily(Font(R.font.uthman_taha))
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                        .background(Color(0xFFFFFDF7), RoundedCornerShape(12.dp))
                        .border(2.dp, Color(0xFF8B5A2B).copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    LazyColumn(
                        state = mushafListState,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        itemsIndexed(verses) { _, verse ->
                            val isPlayingThis = currentPlayingVerse == verse.verseNumber && currentPlayingSurahId == verse.surahId

                            if (verse.verseNumber == 1) {
                                val sObj = surahsList.find { it.id == verse.surahId }
                                SurahHeaderDecoration(sObj?.arabicName ?: surahName, fontFamily = arabicFontFamily)
                                if (verse.surahId != 9 && verse.surahId != 1) {
                                    BismillahHeaderDecoration(fontFamily = arabicFontFamily)
                                }
                                Spacer(Modifier.height(8.dp))
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isPlayingThis) Color(0xFFFFF9C4) else Color.Transparent)
                                    .clickable { onPlayClick(verse) }
                                    .padding(vertical = 8.dp, horizontal = 6.dp)
                            ) {
                                TajweedAyahText(
                                    surahId = verse.surahId,
                                    ayahId = verse.verseNumber,
                                    rawText = verse.arabicText ?: "",
                                    activeRules = if (tajweedEnabled) tajweedActiveRules else emptySet(),
                                    fontSize = (arabicFontSize * 1.1f).sp,
                                    fontFamily = arabicFontFamily,
                                    textColor = if (isPlayingThis) Color(0xFF8B5A2B) else Color(0xFF2C1810),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f), thickness = 0.5.dp)
                        }
                    }
                }
            }
        }

        // Alt Oynatıcı Barı (Mushaf Görünümünde de Tam Aktif)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            color = Color(0xFFE8E3D0),
            shadowElevation = 10.dp,
            border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                if (isPlaying || isPaused) {
                    val statusText = if (currentPlayingVerse != null) {
                        "$surahTitle — $currentPlayingVerse. Ayet Okunuyor"
                    } else "Çalma Duraklatıldı"
                    Text(
                        text = statusText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF8B5A2B),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 2.dp),
                        textAlign = TextAlign.Center
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Hız Butonu
                    TextButton(onClick = {
                        val nextSpeed = when (playbackSpeed) {
                            1.0f -> 1.25f
                            1.25f -> 1.5f
                            1.5f -> 0.75f
                            else -> 1.0f
                        }
                        onSpeedClick(nextSpeed)
                    }) {
                        Text("${playbackSpeed}x", fontWeight = FontWeight.Bold, color = Color(0xFF2C1810))
                    }

                    // Oynatma Kontrolleri
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        IconButton(onClick = onStopClick) {
                            Icon(Icons.Default.Stop, contentDescription = "Durdur", tint = Color(0xFF2C1810))
                        }

                        FloatingActionButton(
                            onClick = {
                                if (isPlaying || isPaused) {
                                    onPauseClick()
                                } else if (verses.isNotEmpty()) {
                                    onPlayClick(verses.first())
                                }
                            },
                            containerColor = Color(0xFF8B5A2B),
                            contentColor = Color.White,
                            modifier = Modifier.size(48.dp),
                            shape = CircleShape
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Duraklat" else "Oynat"
                            )
                        }

                        // Tekrar Modu
                        IconButton(onClick = onRepeatToggle) {
                            Icon(
                                imageVector = Icons.Default.Repeat,
                                contentDescription = "Tekrar",
                                tint = if (isRepeatEnabled) Color(0xFFFF9800) else Color(0xFF2C1810)
                            )
                        }
                    }

                    // Yer İmi Butonu
                    val firstVerseKey = "${surah?.id ?: 1}:${verses.firstOrNull()?.verseNumber ?: 1}"
                    val isFirstBookmarked = bookmarks.contains(firstVerseKey)
                    IconButton(onClick = { onBookmarkToggle(firstVerseKey) }) {
                        Icon(
                            imageVector = if (isFirstBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Yer İmi",
                            tint = if (isFirstBookmarked) Color(0xFFFF9800) else Color(0xFF2C1810)
                        )
                    }
                }
            }
        }
    }
}

// Güvenilir Akıllı Mushaf Resim Yükleyicisi (Yedekli CDN Desteği)
@Composable
fun MushafImagePage(pageNumber: Int) {
    val paddedPage = pageNumber.toString().padStart(3, '0')
    val primaryUrl = "https://android.quran.com/data/width_1024/page$paddedPage.png"
    val fallbackUrl = "https://raw.githubusercontent.com/TheQuran/quran-images/master/images/pages/page_$paddedPage.png"
    
    var currentUrl by remember(pageNumber) { mutableStateOf(primaryUrl) }
    var isLoading by remember(pageNumber) { mutableStateOf(true) }
    var isError by remember(pageNumber) { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFCF8EC))
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = Color(0xFF8B5A2B))
                Spacer(Modifier.height(8.dp))
                Text("Mushaf Sayfası $pageNumber Yükleniyor...", fontSize = 12.sp, color = Color(0xFF8B5A2B))
            }
        }

        val context = LocalContext.current
        val imageRequest = remember(currentUrl) {
            ImageRequest.Builder(context)
                .data(currentUrl)
                .crossfade(true)
                .build()
        }

        AsyncImage(
            model = imageRequest,
            contentDescription = "Mushaf Sayfa $pageNumber",
            contentScale = ContentScale.Fit,
            onLoading = { isLoading = true; isError = false },
            onSuccess = { isLoading = false; isError = false },
            onError = {
                if (currentUrl == primaryUrl) {
                    currentUrl = fallbackUrl
                } else {
                    isLoading = false
                    isError = true
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        if (isError) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = Color.Red, modifier = Modifier.size(36.dp))
                Spacer(Modifier.height(8.dp))
                Text("Sayfa resmi yüklenemedi. Lütfen internet bağlantınızı kontrol ediniz.", fontSize = 12.sp, color = Color.Red, textAlign = TextAlign.Center)
                Spacer(Modifier.height(8.dp))
                Button(onClick = { currentUrl = primaryUrl; isLoading = true; isError = false }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5A2B))) {
                    Text("Tekrar Deneyin", color = Color.White)
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
    Box(Modifier.fillMaxSize().padding(16.dp), Alignment.Center) {
        Text(text = message, color = Color.White, textAlign = TextAlign.Center)
    }
}

@Composable
fun DecorativeNumber(number: String) {
    Surface(
        modifier = Modifier.size(36.dp),
        shape = CircleShape,
        color = Color(0xFFFFD700).copy(alpha = 0.15f),
        border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.4f))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text = number, color = Color(0xFFFFD700), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SurahList(surahs: List<Surah>, state: androidx.compose.foundation.lazy.LazyListState, onSurahClick: (Surah) -> Unit) {
    LazyColumn(state = state, modifier = Modifier.fillMaxSize()) {
        items(items = surahs) { surah ->
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
        DecorativeNumber(surah.id.toString())
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = surah.name ?: "", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(text = "İniş Sırası: ${surah.revelationOrder ?: "-"}", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
        }
        Text(text = "${surah.verseCount} AYET", color = Color(0xFFFFD700), fontSize = 10.sp, fontWeight = FontWeight.Bold)
        if (surah.arabicName != null) {
            Spacer(Modifier.width(12.dp))
            Text(text = surah.arabicName, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun JuzList(searchQuery: String, state: androidx.compose.foundation.lazy.LazyListState, onJuzClick: (Int) -> Unit) {
    val juzs = (1..30).filter { it.toString().contains(searchQuery) }
    LazyColumn(state = state, modifier = Modifier.fillMaxSize()) {
        items(items = juzs) { juz ->
            val range = QuranUtils.getJuzPageRange(juz)
            SimpleListItem(number = juz, title = "${juz}. Cüz", subtitle = "Sayfa ${range.first} - ${range.second}") { onJuzClick(juz) }
            HorizontalDivider(color = Color.White.copy(alpha = 0.05f), thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
        }
    }
}

@Composable
fun PageList(searchQuery: String, state: androidx.compose.foundation.lazy.LazyListState, onPageClick: (Int) -> Unit) {
    val pages = (1..604).filter { it.toString().contains(searchQuery) }
    LazyColumn(state = state, modifier = Modifier.fillMaxSize()) {
        items(items = pages) { page ->
            val juz = QuranUtils.getJuzByPage(page)
            SimpleListItem(number = page, title = "${page}. Sayfa", subtitle = "${juz}. Cüz") { onPageClick(page) }
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
        DecorativeNumber(number.toString())
        Spacer(Modifier.width(16.dp))
        Column {
            Text(text = title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(text = subtitle, color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// Ezan Vakti Pro Osmanlı Süslü Sure Başlığı
@Composable
fun SurahHeaderDecoration(arabicName: String, fontFamily: FontFamily = FontFamily(Font(R.font.uthman_taha))) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 16.dp)
            .background(Color(0xFFE8E3D0), RoundedCornerShape(12.dp))
            .border(2.dp, Color(0xFF8B5A2B), RoundedCornerShape(12.dp))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "❖ $arabicName ❖",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = fontFamily,
                color = Color(0xFF2C1810),
                textAlign = TextAlign.Center
            )
        }
    }
}

// Ezan Vakti Pro Ayet Listesi ve Görünüm Yapısı
@Composable
fun EzanVaktiVerseList(
    verses: List<Verse>,
    surahNameArabic: String,
    detailTitle: String,
    detailSubtitle: String,
    surahId: Int,
    surahsList: List<Surah> = emptyList(),
    selectedSurahId: Int? = null,
    targetScrollVerseIndex: Int? = null,
    isPlaying: Boolean,
    isPaused: Boolean,
    playbackSpeed: Float,
    currentPlayingVerse: Int?,
    currentPlayingSurahId: Int?,
    selectedFont: String,
    quranMode: QuranMode,
    arabicFontSize: Float,
    showTransliteration: Boolean,
    selectedMealSource: String,
    bookmarks: Set<String>,
    isRepeatEnabled: Boolean,
    tajweedEnabled: Boolean,
    tajweedActiveRules: Set<TajweedRule>,
    listState: LazyListState = rememberLazyListState(),
    onPlayClick: (Verse) -> Unit,
    onStopClick: () -> Unit,
    onPauseClick: () -> Unit,
    onSpeedClick: (Float) -> Unit,
    onRepeatToggle: () -> Unit,
    onBookmarkToggle: (String) -> Unit
) {
    var lastHandledVerseTarget by remember { mutableStateOf<Pair<Int?, Int?>>(null to null) }

    // Sadece O an dinlenen ayet değiştiğinde veya Hızlı Erişim/Arama yapıldığında 1 kez kaydır (Kilitlenmeyi ve çakışmayı önler)
    LaunchedEffect(currentPlayingVerse, currentPlayingSurahId, targetScrollVerseIndex) {
        val currentTarget = currentPlayingVerse to currentPlayingSurahId
        if (currentPlayingVerse != null && currentPlayingSurahId == surahId && currentTarget != lastHandledVerseTarget) {
            lastHandledVerseTarget = currentTarget
            val idx = verses.indexOfFirst { it.surahId == currentPlayingSurahId && it.verseNumber == currentPlayingVerse }
            if (idx >= 0) {
                listState.animateScrollToItem(idx, scrollOffset = -250)
            }
        } else if (targetScrollVerseIndex != null && targetScrollVerseIndex in verses.indices) {
            listState.animateScrollToItem(targetScrollVerseIndex, scrollOffset = -50)
        }
    }

    val arabicFontFamily = when {
        selectedFont.contains("Me Quran") || selectedFont.contains("Diyanet") -> FontFamily(Font(R.font.me_quran))
        selectedFont.contains("Scheherazade") || selectedFont.contains("Şehrazat") -> FontFamily(
            Font(R.font.scheherazade_new_regular, FontWeight.Normal),
            Font(R.font.scheherazade_new_medium, FontWeight.Medium),
            Font(R.font.scheherazade_new_semibold, FontWeight.SemiBold),
            Font(R.font.scheherazade_new_bold, FontWeight.Bold)
        )
        else -> FontFamily(Font(R.font.uthman_taha))
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {
            itemsIndexed(verses) { _, verse ->
                val verseKey = "${surahId}:${verse.verseNumber}"
                val isBookmarked = bookmarks.contains(verseKey)
                
                if (verse.verseNumber == 1) {
                    val sObj = surahsList.find { it.id == verse.surahId }
                    SurahHeaderDecoration(sObj?.arabicName ?: surahNameArabic, fontFamily = arabicFontFamily)
                    if (verse.surahId != 9 && verse.surahId != 1) {
                        BismillahHeaderDecoration(fontFamily = arabicFontFamily)
                    }
                    Spacer(Modifier.height(8.dp))
                }

                EzanVaktiVerseItem(
                    verse = verse,
                    surahName = detailTitle,
                    isCurrentPlaying = currentPlayingVerse == verse.verseNumber && currentPlayingSurahId == surahId,
                    selectedFont = selectedFont,
                    quranMode = quranMode,
                    arabicFontSize = arabicFontSize,
                    showTransliteration = showTransliteration,
                    selectedMealSource = selectedMealSource,
                    isBookmarked = isBookmarked,
                    tajweedEnabled = tajweedEnabled,
                    tajweedActiveRules = tajweedActiveRules,
                    onPlayClick = { onPlayClick(verse) },
                    onBookmarkToggle = { onBookmarkToggle(verseKey) }
                )
                HorizontalDivider(color = Color.Gray.copy(alpha = 0.3f), thickness = 1.dp)
            }
        }

        // Alt Oynatıcı Barı (Ezan Vakti Pro Stili - Tam Aktif)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            color = Color(0xFFE8E3D0),
            shadowElevation = 10.dp,
            border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                if (isPlaying || isPaused) {
                    val statusText = if (currentPlayingVerse != null) {
                        "$detailTitle — $currentPlayingVerse. Ayet Okunuyor"
                    } else "Çalma Duraklatıldı"
                    Text(
                        text = statusText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF8B5A2B),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 2.dp),
                        textAlign = TextAlign.Center
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Hız Butonu
                    TextButton(onClick = {
                        val nextSpeed = when (playbackSpeed) {
                            1.0f -> 1.25f
                            1.25f -> 1.5f
                            1.5f -> 0.75f
                            else -> 1.0f
                        }
                        onSpeedClick(nextSpeed)
                    }) {
                        Text("${playbackSpeed}x", fontWeight = FontWeight.Bold, color = Color(0xFF2C1810))
                    }

                    // Oynatma Kontrolleri
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        IconButton(onClick = onStopClick) {
                            Icon(Icons.Default.Stop, contentDescription = "Durdur", tint = Color(0xFF2C1810))
                        }

                        FloatingActionButton(
                            onClick = {
                                if (isPlaying || isPaused) {
                                    onPauseClick()
                                } else if (verses.isNotEmpty()) {
                                    onPlayClick(verses.first())
                                }
                            },
                            containerColor = Color(0xFF8B5A2B),
                            contentColor = Color.White,
                            modifier = Modifier.size(48.dp),
                            shape = CircleShape
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Duraklat" else "Oynat"
                            )
                        }

                        // Tekrar Modu
                        IconButton(onClick = onRepeatToggle) {
                            Icon(
                                imageVector = Icons.Default.Repeat,
                                contentDescription = "Tekrar",
                                tint = if (isRepeatEnabled) Color(0xFFFF9800) else Color(0xFF2C1810)
                            )
                        }
                    }

                    // Yer İmi Butonu
                    val firstVerseKey = "${surahId}:${verses.firstOrNull()?.verseNumber ?: 1}"
                    val isFirstBookmarked = bookmarks.contains(firstVerseKey)
                    IconButton(onClick = { onBookmarkToggle(firstVerseKey) }) {
                        Icon(
                            imageVector = if (isFirstBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Yer İmi",
                            tint = if (isFirstBookmarked) Color(0xFFFF9800) else Color(0xFF2C1810)
                        )
                    }
                }
            }
        }
    }
}

// Ezan Vakti Pro Tekil Ayet Kartı
@Composable
fun EzanVaktiVerseItem(
    verse: Verse,
    surahName: String,
    isCurrentPlaying: Boolean,
    selectedFont: String,
    quranMode: QuranMode,
    arabicFontSize: Float,
    showTransliteration: Boolean,
    selectedMealSource: String,
    isBookmarked: Boolean,
    tajweedEnabled: Boolean,
    tajweedActiveRules: Set<TajweedRule>,
    onPlayClick: () -> Unit,
    onBookmarkToggle: () -> Unit
) {
    val transliterationText = verse.transliterationApi
        ?: verse.customTransliteration
        ?: QuranTransliterationHelper.getTransliteration(verse.verseKey, verse.arabicText)

    val arabicFontFamily = when {
        selectedFont.contains("Me Quran") || selectedFont.contains("Diyanet") -> FontFamily(Font(R.font.me_quran))
        selectedFont.contains("Scheherazade") || selectedFont.contains("Şehrazat") -> FontFamily(
            Font(R.font.scheherazade_new_regular, FontWeight.Normal),
            Font(R.font.scheherazade_new_medium, FontWeight.Medium),
            Font(R.font.scheherazade_new_semibold, FontWeight.SemiBold),
            Font(R.font.scheherazade_new_bold, FontWeight.Bold)
        )
        else -> FontFamily(Font(R.font.uthman_taha))
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isCurrentPlaying) Color(0xFFFFF9C4) else Color(0xFFFCF8EC))
            .padding(16.dp)
    ) {
        TajweedAyahText(
            surahId = verse.surahId,
            ayahId = verse.verseNumber,
            rawText = verse.arabicText ?: "",
            activeRules = if (tajweedEnabled) tajweedActiveRules else emptySet(),
            fontSize = arabicFontSize.sp,
            fontFamily = arabicFontFamily,
            textColor = Color(0xFF2C1810),
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = "$surahName ${verse.verseNumber}",
            fontSize = 11.sp,
            color = Color.Gray,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            textAlign = TextAlign.Center
        )

        if (showTransliteration && quranMode == QuranMode.MEALLI && transliterationText != null) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Türkçe Okunuşu",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2C1810)
            )
            val annotatedString = buildAnnotatedString {
                var isRed = false
                transliterationText.forEach { char ->
                    if (char == '(' || char == ')') {
                        isRed = char == '('
                        append(char)
                    } else if (isRed) {
                        withStyle(style = SpanStyle(color = Color.Red, fontWeight = FontWeight.Bold)) {
                            append(char)
                        }
                    } else {
                        append(char)
                    }
                }
            }
            Text(
                text = annotatedString,
                fontSize = 14.sp,
                color = Color(0xFF333333),
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        if (quranMode == QuranMode.MEALLI) {
            val mealText = verse.translation
            if (mealText != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = selectedMealSource,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2C1810)
                )
                Text(
                    text = mealText,
                    fontSize = 14.sp,
                    color = Color(0xFF222222),
                    lineHeight = 22.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

// Hızlı Erişim Diyaloğu
@Composable
fun QuickJumpDialog(
    surahs: List<Surah>,
    sortMode: SurahSortMode,
    onSortModeChange: (SurahSortMode) -> Unit,
    onSelect: (juz: Int?, surahId: Int?, verseNum: Int?) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedJuzNum by remember { mutableIntStateOf(1) }

    val sortedSurahs = remember(surahs, sortMode) {
        when (sortMode) {
            SurahSortMode.KURAN -> surahs.sortedBy { it.id }
            SurahSortMode.NUZUL -> surahs.sortedBy { it.revelationOrder ?: it.id }
            SurahSortMode.ALFABETIK -> surahs.sortedBy { it.name ?: "" }
        }
    }

    var selectedSurahObj by remember {
        mutableStateOf(sortedSurahs.firstOrNull())
    }

    var selectedVerseNum by remember(selectedSurahObj?.id) {
        mutableIntStateOf(1)
    }

    val surahListState = rememberLazyListState()

    // Cüz seçildiğinde o Cüzün ilk suresine otomatik kaydır ve seç
    LaunchedEffect(selectedJuzNum) {
        val firstSurahId = QuranUtils.getSurahIdsForJuz(selectedJuzNum).firstOrNull() ?: 1
        val matchedSurah = surahs.find { it.id == firstSurahId }
        if (matchedSurah != null) {
            selectedSurahObj = matchedSurah
            val indexInSorted = sortedSurahs.indexOfFirst { it.id == firstSurahId }
            if (indexInSorted >= 0) {
                surahListState.animateScrollToItem(indexInSorted)
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Hızlı Erişim", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF2C1810))
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Color.Gray)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Cüz Listesi
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Text("Cüz", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Spacer(Modifier.height(8.dp))
                        LazyColumn(modifier = Modifier.height(140.dp)) {
                            items(count = 30) { index ->
                                val j = index + 1
                                Text(
                                    text = j.toString(),
                                    fontSize = 16.sp,
                                    fontWeight = if (selectedJuzNum == j) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedJuzNum == j) Color(0xFF8B5A2B) else Color.DarkGray,
                                    modifier = Modifier
                                        .clickable { selectedJuzNum = j }
                                        .padding(vertical = 4.dp)
                                )
                            }
                        }
                    }

                    // Sure Listesi (Tüm 114 Sure Scroll Edilebilir)
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1.5f)) {
                        Text("Sure", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Spacer(Modifier.height(8.dp))
                        LazyColumn(state = surahListState, modifier = Modifier.height(140.dp)) {
                            items(items = sortedSurahs) { s ->
                                Text(
                                    text = "${s.id}- ${s.name ?: ""}",
                                    fontSize = 14.sp,
                                    fontWeight = if (selectedSurahObj?.id == s.id) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedSurahObj?.id == s.id) Color(0xFF8B5A2B) else Color.DarkGray,
                                    modifier = Modifier
                                        .clickable { selectedSurahObj = s }
                                        .padding(vertical = 4.dp)
                                )
                            }
                        }
                    }

                    // Ayet Listesi
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Text("Ayet", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Spacer(Modifier.height(8.dp))
                        val verseCount = selectedSurahObj?.verseCount ?: 7
                        LazyColumn(modifier = Modifier.height(140.dp)) {
                            items(count = verseCount) { index ->
                                val v = index + 1
                                Text(
                                    text = v.toString(),
                                    fontSize = 16.sp,
                                    fontWeight = if (selectedVerseNum == v) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedVerseNum == v) Color(0xFF8B5A2B) else Color.DarkGray,
                                    modifier = Modifier
                                        .clickable { selectedVerseNum = v }
                                        .padding(vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = Color.LightGray)
                Spacer(Modifier.height(12.dp))

                // Sıralama Butonu
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val nextMode = when (sortMode) {
                                SurahSortMode.KURAN -> SurahSortMode.NUZUL
                                SurahSortMode.NUZUL -> SurahSortMode.ALFABETIK
                                SurahSortMode.ALFABETIK -> SurahSortMode.KURAN
                            }
                            onSortModeChange(nextMode)
                        }
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Sıralama: ${sortMode.label}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF8B5A2B))
                    Icon(Icons.Default.Shuffle, contentDescription = null, tint = Color(0xFF8B5A2B))
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSelect(selectedJuzNum, selectedSurahObj?.id, selectedVerseNum) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5A2B)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("GİT", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("İPTAL", color = Color.Gray)
            }
        },
        containerColor = Color(0xFFFCF8EC),
        shape = RoundedCornerShape(16.dp)
    )
}

// Sağ Yan Menü Sheet
@Composable
fun SideMenuSheet(
    onItemClick: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val items = listOf(
        "Sureler" to Icons.Default.MenuBook,
        "Yer İmleri" to Icons.Default.BookmarkBorder,
        "Mealler" to Icons.Default.Translate,
        "Hafızlar" to Icons.Default.RecordVoiceOver,
        "Sayfa Görünümü" to Icons.Default.AutoStories,
        "Seçim ve Takip Ayarı" to Icons.Default.TouchApp,
        "Metin Ayarları" to Icons.Default.TextFields,
        "Tecvid Renklendirme" to Icons.Default.ColorLens,
        "Fihrist" to Icons.AutoMirrored.Filled.List,
        "Meal Seslendirme (TTS) Ayarları" to Icons.Default.VolumeUp,
        "Tema" to Icons.Default.Palette
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = null,
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                items.forEach { (title, icon) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onItemClick(title) }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(icon, contentDescription = null, tint = Color(0xFF8B5A2B), modifier = Modifier.size(22.dp))
                        Spacer(Modifier.width(16.dp))
                        Text(title, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF2C1810))
                    }
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.4f), thickness = 0.5.dp)
                }
            }
        },
        confirmButton = {},
        containerColor = Color(0xFFFCF8EC),
        shape = RoundedCornerShape(16.dp)
    )
}

// Mealler Seçim ve İndirme Diyaloğu (Diyanet İşleri & Elmalılı Hamdi Yazır)
@Composable
fun MeallerDialog(
    showTransliteration: Boolean,
    selectedMealId: String,
    downloadedMeals: Set<String>,
    isDownloadingMeal: String?,
    onTransliterationChange: (Boolean) -> Unit,
    onMealSelect: (mealId: String, mealName: String) -> Unit,
    onMealDownload: (mealId: String) -> Unit,
    onDismiss: () -> Unit
) {
    val meals = listOf(
        Triple("77", "Diyanet İşleri Başkanlığı (Türkçe)", "Diyanet Vakfı Resmi Meali"),
        Triple("210", "Elmalılı Hamdi Yazır (Türkçe)", "Hak Dini Kur'an Dili Meali")
    )

    var warningText by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Mealler", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF2C1810)) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                if (warningText != null) {
                    Surface(
                        color = Color(0xFFFFEBEE),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFFEF5350)),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    ) {
                        Text(
                            text = warningText!!,
                            color = Color(0xFFC62828),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onTransliterationChange(!showTransliteration) }
                        .padding(vertical = 8.dp)
                ) {
                    Checkbox(
                        checked = showTransliteration,
                        onCheckedChange = { onTransliterationChange(it) },
                        colors = CheckboxDefaults.colors(checkedColor = Color(0xFF8B5A2B))
                    )
                    Text("Türkçe Okunuşu", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF2C1810))
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                meals.forEach { (id, title, desc) ->
                    val isSelected = selectedMealId == id
                    val isDownloaded = downloadedMeals.contains(id)
                    val isDownloading = isDownloadingMeal == id

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (isDownloaded) {
                                    warningText = null
                                    onMealSelect(id, title)
                                } else {
                                    warningText = "Bu meal henüz indirilmedi! Lütfen önce İNDİR butonuna basarak meali indiriniz."
                                }
                            }
                            .padding(vertical = 8.dp)
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = {
                                if (isDownloaded) {
                                    warningText = null
                                    onMealSelect(id, title)
                                } else {
                                    warningText = "Bu meal henüz indirilmedi! Lütfen önce İNDİR butonuna basarak meali indiriniz."
                                }
                            },
                            colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF8B5A2B))
                        )
                        Column(modifier = Modifier.weight(1f).padding(horizontal = 8.dp)) {
                            Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2C1810))
                            Text(desc, fontSize = 11.sp, color = Color.Gray)
                        }

                        if (isDownloading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color(0xFF8B5A2B), strokeWidth = 2.dp)
                        } else if (isDownloaded) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = "İndirildi", tint = Color(0xFF4CAF50), modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("İndirildi", fontSize = 10.sp, color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Button(
                                onClick = {
                                    warningText = null
                                    onMealDownload(id)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5A2B)),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.FileDownload, contentDescription = "İndir", tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("İNDİR", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5A2B))) {
                Text("TAMAM", color = Color.White)
            }
        },
        containerColor = Color(0xFFFCF8EC),
        shape = RoundedCornerShape(16.dp)
    )
}

// Hafızlar Diyaloğu
@Composable
fun HafizlarDialog(
    currentReciter: String,
    onReciterChange: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val hafizlar = listOf(
        "Husayri" to "Husary_128kbps",
        "Mishary Al-Afasy" to "Alafasy_128kbps",
        "Sudais Shraim" to "Abdurrahmaan_As-Sudais_192kbps",
        "AbuBakr Ash-Shatree" to "Abu_Bakr_Ash-Shaatree_128kbps",
        "Abdul Basit Abdul Samet" to "Abdul_Basit_Murattal_64kbps",
        "Saad Al Ghamdi (Takipli)" to "Ghamadi_40kbps",
        "Mahir Al-Muaygali" to "Maher_AlMuaiqly_64kbps",
        "Muhammed Al-Minshawi" to "Minshawy_Murattal_128kbps"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Hafızlar", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF2C1810)) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                hafizlar.forEach { (name, key) ->
                    val isSelected = currentReciter == key
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onReciterChange(key) }
                            .padding(vertical = 8.dp)
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { onReciterChange(key) },
                            colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF8B5A2B))
                        )
                        Text(name, fontSize = 14.sp, color = Color(0xFF2C1810), modifier = Modifier.weight(1f))
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5A2B))) {
                Text("TAMAM", color = Color.White)
            }
        },
        containerColor = Color(0xFFFCF8EC),
        shape = RoundedCornerShape(16.dp)
    )
}

// Sayfa Görünümü Diyaloğu (Metin Görünümü & Kur'an Sayfası Mushaf)
@Composable
fun SayfaGorunumuDialog(
    currentMode: QuranViewDisplayMode,
    onModeSelect: (QuranViewDisplayMode) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Sayfa Görünümü", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF2C1810)) },
        text = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onModeSelect(QuranViewDisplayMode.METIN_GORUNUMU); onDismiss() },
                    border = BorderStroke(1.5.dp, if (currentMode == QuranViewDisplayMode.METIN_GORUNUMU) Color(0xFF8B5A2B) else Color.LightGray)
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.TextFields, contentDescription = null, tint = Color(0xFF8B5A2B))
                        Spacer(Modifier.height(8.dp))
                        Text("Metin Görünümü", fontSize = 12.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                        Text("Ayet ayet okunuş ve meal", fontSize = 10.sp, color = Color.Gray, textAlign = TextAlign.Center)
                    }
                }

                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onModeSelect(QuranViewDisplayMode.KURAN_SAYFASI); onDismiss() },
                    border = BorderStroke(1.5.dp, if (currentMode == QuranViewDisplayMode.KURAN_SAYFASI) Color(0xFF8B5A2B) else Color.LightGray)
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.MenuBook, contentDescription = null, tint = Color(0xFF8B5A2B))
                        Spacer(Modifier.height(8.dp))
                        Text("Kur'an Sayfası", fontSize = 12.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                        Text("Mushaf sayfa hat düzeni", fontSize = 10.sp, color = Color.Gray, textAlign = TextAlign.Center)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("KAPAT", color = Color(0xFF8B5A2B)) }
        },
        containerColor = Color(0xFFFCF8EC),
        shape = RoundedCornerShape(16.dp)
    )
}

// Seçim ve Takip Ayarı Diyaloğu
@Composable
fun SecimTakipDialog(
    currentMode: String,
    onModeSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val options = listOf(
        "Oklu takip (desteklenen hafızlarda)",
        "Renkli seçim (varsayılan)",
        "Vurgula",
        "Seçim yok"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Seçim ve Takip Ayarı", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF2C1810)) },
        text = {
            Column {
                options.forEach { opt ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onModeSelect(opt) }
                            .padding(vertical = 8.dp)
                    ) {
                        RadioButton(
                            selected = currentMode == opt,
                            onClick = { onModeSelect(opt) },
                            colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF8B5A2B))
                        )
                        Text(opt, fontSize = 13.sp, color = Color(0xFF2C1810))
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5A2B))) {
                Text("TAMAM", color = Color.White)
            }
        },
        containerColor = Color(0xFFFCF8EC),
        shape = RoundedCornerShape(16.dp)
    )
}

// Tecvid Renklendirme Diyaloğu (Üst Ana Switch & Her Kural Yanında Bağımsız Anahtar)
@Composable
fun TecvidDialog(
    isEnabled: Boolean,
    activeRules: Set<TajweedRule>,
    onEnabledChange: (Boolean) -> Unit,
    onToggleRule: (TajweedRule) -> Unit,
    onDismiss: () -> Unit
) {
    val rules = listOf(
        TajweedRule.QALQALAH to ("Kaf, tı, be, cim ve dal harfleri kelimenin ortasında veya sonunda sakin olarak gelmeleri halinde ses olarak vurgulu okunmasına kalkale denir." to "سُبْحَانَكَ , لَمْ يَلِدْ وَلَمْ يُولَدْ"),
        TajweedRule.GHUNNAH_IHFA to ("Tenvin ve cezmli nundan sonra ihfa / gunne harfleri gelirse genizden ses verilir." to "سَمِيعٌ بَصِيرٌ , مِنْ بَعْدِهِ"),
        TajweedRule.MADD_LAZIM to ("Harfi medden sonra sebebi medden sükun-i lazım gelirse zorunlu uzatma yapılır." to "وَلاَ الضَّالِّينَ"),
        TajweedRule.MADD_MUTTASIL_MUNFASIL to ("Harfi medden sonra sebebi medden hemze gelirse 4-5 hareke uzatılır." to "سَوَاءٌ , مِنَ السَّمَاءِ"),
        TajweedRule.MADD_TABII to ("Harfi medden sonra sebebi med bulunmazsa 2 hareke uzatılır." to "الْكِتَابُ , يُؤْمِنُونَ"),
        TajweedRule.HEAVY_RA to ("Fatha veya Damma alan Râ harfleri tok ve kalın okunur." to "رَبَّنَا , الرَّحْمَنِ"),
        TajweedRule.SILENT to ("Okunmayan veya geçişte düşen elif ve harfler grileşir." to "وَٱلصُّبْحِ")
    )

    val uthmanFont = FontFamily(Font(R.font.uthman_taha))

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Tecvid Renklendirme", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF2C1810))
                Switch(
                    checked = isEnabled && activeRules.isNotEmpty(),
                    onCheckedChange = { onEnabledChange(it) },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF8B5A2B))
                )
            }
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                rules.forEach { (rule, details) ->
                    val (desc, example) = details
                    val isRuleActive = isEnabled && activeRules.contains(rule)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(rule.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF8B5A2B))
                                Switch(
                                    checked = isRuleActive,
                                    onCheckedChange = { onToggleRule(rule) },
                                    colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF8B5A2B))
                                )
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(desc, fontSize = 11.sp, color = Color.Gray, lineHeight = 16.sp)
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = QuranTajweedColorizer.colorize(example, activeRules),
                                fontSize = 20.sp,
                                fontFamily = uthmanFont,
                                textAlign = TextAlign.Right,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5A2B))) {
                Text("KAPAT", color = Color.White)
            }
        },
        containerColor = Color(0xFFFCF8EC),
        shape = RoundedCornerShape(16.dp)
    )
}

// Yer İmleri Diyaloğu
@Composable
fun BookmarksDialog(
    bookmarks: Set<String>,
    onSelectBookmark: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val list = bookmarks.toList()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Yer İmleri", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF2C1810)) },
        text = {
            if (list.isEmpty()) {
                Text("Henüz kaydedilmiş bir yer iminiz yok.", fontSize = 13.sp, color = Color.Gray)
            } else {
                LazyColumn(modifier = Modifier.height(200.dp)) {
                    items(items = list) { verseKey ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectBookmark(verseKey) }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Bookmark, contentDescription = null, tint = Color(0xFF8B5A2B))
                            Spacer(Modifier.width(12.dp))
                            Text("Ayet: $verseKey", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2C1810))
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5A2B))) {
                Text("KAPAT", color = Color.White)
            }
        },
        containerColor = Color(0xFFFCF8EC),
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun SettingsDialog(
    currentReciter: String,
    currentFont: String,
    currentMode: QuranMode,
    currentFontSize: Float,
    onReciterChange: (String) -> Unit,
    onFontChange: (String) -> Unit,
    onModeChange: (QuranMode) -> Unit,
    onFontSizeChange: (Float) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val reciters = listOf(
        "Yasir el-Dawsari" to "Yasser_Ad-Dussary_128kbps",
        "Mishary Rashid Alafasy" to "Alafasy_128kbps",
        "Abdurrahman As-Sudais" to "Abdurrahmaan_As-Sudais_192kbps",
        "Abdulbasit Abdussamed" to "Abdul_Basit_Murattal_64kbps",
        "Mahir Al-Muaygali" to "Maher_AlMuaiqly_64kbps"
    )
    val fonts = listOf(
        "Uthman Taha (Medine Hattı)",
        "Me Quran (Diyanet Hattı)",
        "Scheherazade (Şehrazat Hattı)"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("AYARLAR", fontWeight = FontWeight.Black, color = Color(0xFFFFD700), modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                TabRow(selectedTabIndex = selectedTabIndex, containerColor = Color.Transparent, contentColor = Color(0xFFFFD700)) {
                    Tab(selected = selectedTabIndex == 0, onClick = { selectedTabIndex = 0 }) { Text("OKUYUCU", modifier = Modifier.padding(8.dp)) }
                    Tab(selected = selectedTabIndex == 1, onClick = { selectedTabIndex = 1 }) { Text("GÖRÜNÜM", modifier = Modifier.padding(8.dp)) }
                }
                Spacer(Modifier.height(16.dp))
                if (selectedTabIndex == 0) {
                    LazyColumn(modifier = Modifier.height(250.dp)) {
                        items(items = reciters) { reciter ->
                            val (name, key) = reciter
                            Row(
                                modifier = Modifier.fillMaxWidth().clickable { onReciterChange(key) }.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(selected = currentReciter == key, onClick = { onReciterChange(key) })
                                Text(name, color = Color.White)
                            }
                        }
                    }
                } else {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        Text("Okuma Modu", color = Color.White.copy(0.7f), fontSize = 12.sp)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(selected = currentMode == QuranMode.MEALLI, onClick = { onModeChange(QuranMode.MEALLI) }, label = { Text("Mealli") })
                            FilterChip(selected = currentMode == QuranMode.MEALSIZ, onClick = { onModeChange(QuranMode.MEALSIZ) }, label = { Text("Mealsiz") })
                        }
                        Spacer(Modifier.height(12.dp))
                        Text("Yazı Boyutu (${currentFontSize.toInt()} sp)", color = Color.White.copy(0.7f), fontSize = 12.sp)
                        Slider(value = currentFontSize, onValueChange = onFontSizeChange, valueRange = 20f..42f)
                        Spacer(Modifier.height(12.dp))
                        Text("Yazı Tipi", color = Color.White.copy(0.7f), fontSize = 12.sp)
                        @OptIn(ExperimentalLayoutApi::class)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            fonts.forEach { font ->
                                FilterChip(selected = currentFont == font, onClick = { onFontChange(font) }, label = { Text(font) })
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { Button(onClick = onDismiss) { Text("TAMAM") } },
        containerColor = Color(0xFF073642)
    )
}
