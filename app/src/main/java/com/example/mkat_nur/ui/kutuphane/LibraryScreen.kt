package com.example.mkat_nur.ui.kutuphane

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Ink = Color(0xFF3B2412)
private val trLocale = Locale("tr", "TR")

private val BookWidth = 96.dp
private val BookHeight = 144.dp

enum class LibraryThemeConfig(
    val key: String,
    val title: String,
    val headerBg: List<Color>,
    val plaqueBg: Color,
    val plaqueBorder: Color,
    val plaqueText: Color,
    val bodyBg: List<Color>,
    val plankTop: List<Color>,
    val plankFront: List<Color>,
    val bookTitleColor: Color
) {
    BAL_MESE(
        key = "bal_mese",
        title = "3D Bal Meşe (Ahşap - Varsayılan)",
        headerBg = listOf(Color(0xFF6E4324), Color(0xFF533119)),
        plaqueBg = Color(0xFFFFFBEF),
        plaqueBorder = Color(0xFFD4A76A),
        plaqueText = Color(0xFF4A2810),
        bodyBg = listOf(Color(0xFF8D5B36), Color(0xFF734626), Color(0xFF643B1E), Color(0xFF563117)),
        plankTop = listOf(Color(0xFFFFF3E0), Color(0xFFE8B275)),
        plankFront = listOf(Color(0xFFB8783D), Color(0xFF8F5625)),
        bookTitleColor = Color(0xFFFFF8EA)
    ),
    KOYU_MAUN(
        key = "koyu_maun",
        title = "Koyu Maun (Klasik)",
        headerBg = listOf(Color(0xFF381C0E), Color(0xFF241007)),
        plaqueBg = Color(0xFFFFF4DC),
        plaqueBorder = Color(0xFF8C532B),
        plaqueText = Color(0xFF3B180A),
        bodyBg = listOf(Color(0xFF4E2B17), Color(0xFF3A1C0D), Color(0xFF2C1307), Color(0xFF1F0B03)),
        plankTop = listOf(Color(0xFFFFE0B2), Color(0xFF9E5C2D)),
        plankFront = listOf(Color(0xFF6D3B18), Color(0xFF45220B)),
        bookTitleColor = Color(0xFFFFE0B2)
    ),
    MODERN_ACIK(
        key = "modern_acik",
        title = "Modern Açık (Krem)",
        headerBg = listOf(Color(0xFFEFE8DF), Color(0xFFE2D7C9)),
        plaqueBg = Color(0xFFFFFFFF),
        plaqueBorder = Color(0xFFC4B5A5),
        plaqueText = Color(0xFF3E3A36),
        bodyBg = listOf(Color(0xFFF7F4EF), Color(0xFFEFECE6), Color(0xFFE6E2DB), Color(0xFFDDD8D0)),
        plankTop = listOf(Color(0xFFFFFFFF), Color(0xFFE5D8C8)),
        plankFront = listOf(Color(0xFFC7B5A1), Color(0xFFA89682)),
        bookTitleColor = Color(0xFF4A423A)
    ),
    GECE(
        key = "gece",
        title = "Gece Modu (Koyu Slate)",
        headerBg = listOf(Color(0xFF2D2D2D), Color(0xFF1E1E1E)),
        plaqueBg = Color(0xFF383838),
        plaqueBorder = Color(0xFF555555),
        plaqueText = Color(0xFFEEEEEE),
        bodyBg = listOf(Color(0xFF262626), Color(0xFF1E1E1E), Color(0xFF181818), Color(0xFF121212)),
        plankTop = listOf(Color(0xFF505050), Color(0xFF3B3B3B)),
        plankFront = listOf(Color(0xFF2B2B2B), Color(0xFF1C1C1C)),
        bookTitleColor = Color(0xFFDDDDDD)
    );

    companion object {
        fun fromKey(key: String): LibraryThemeConfig {
            return entries.firstOrNull { it.key == key } ?: BAL_MESE
        }
    }
}

/** WhatsApp, Mail, Telegram vb. üzerinden güzel bir anlatım ile tavsiye paylaşımı yapar */
fun shareAppRecommendation(context: Context) {
    val shareText = """
        📖 Risale-i Nur Külliyatı Dijital Kütüphanesi

        Bediüzzaman Said Nursî Hazretleri'nin tüm eserlerini tefekkür modu, 7.9MB detaylı lügat, ayet ve hadîs meâlleri, 3D kitaplık temaları ve tüm külliyatta tam metin arama desteğiyle okuyabilirsiniz.

        Siz de indirin ve bu kudsî envar-ı imaniyeden istifade edin!
    """.trimIndent()

    try {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Risale-i Nur Dijital Kütüphanesi")
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        val chooser = Intent.createChooser(intent, "Risale-i Nur Uygulamasını Paylaş")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    } catch (_: Exception) {}
}

@Composable
fun LibraryScreen(
    shelves: List<Shelf>,
    listState: LazyListState,
    prefs: Prefs,
    onReloadShelves: () -> Unit = {},
    onOpenBook: (Book) -> Unit,
    onOpenBookAtPageIndex: (Book, Int, String) -> Unit = { book, pageIdx, _ ->
        prefs.setPosition(book.id, pageIdx)
        onOpenBook(book)
    },
    onResetBook: (Book) -> Unit,
    onRemoveRecentBook: (Book) -> Unit,
) {
    val context = LocalContext.current
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var searching by rememberSaveable { mutableStateOf(false) }
    var query by rememberSaveable { mutableStateOf("") }
    var currentThemeKey by remember { mutableStateOf(prefs.libraryTheme) }

    var showThemeDialog by remember { mutableStateOf(false) }
    var showFavoritesDialog by remember { mutableStateOf(false) }
    var showNotesDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    val themeConfig = remember(currentThemeKey) { LibraryThemeConfig.fromKey(currentThemeKey) }

    var searchResults by remember { mutableStateOf<List<SearchResultItem>?>(null) }
    var isSearchingFullText by remember { mutableStateOf(false) }

    BackHandler(enabled = drawerState.isOpen) { scope.launch { drawerState.close() } }
    BackHandler(enabled = searching && !drawerState.isOpen) {
        searching = false
        query = ""
    }

    val q = query.trim().lowercase(trLocale)

    // Külliyat içi tam metin arama (300ms Debounce & Coroutine IO)
    LaunchedEffect(q, searching) {
        if (searching && q.length >= 2) {
            isSearchingFullText = true
            delay(300L) // 300ms debounce
            withContext(Dispatchers.IO) {
                searchResults = Repository.searchInAllBooks(context, query)
            }
            isSearchingFullText = false
        } else {
            searchResults = null
        }
    }

    val visibleShelves = remember(shelves, q) {
        if (q.isEmpty()) {
            shelves
        } else {
            shelves
                .map { s -> s.copy(books = s.books.filter { it.title.lowercase(trLocale).contains(q) }) }
                .filter { it.books.isNotEmpty() }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            LibraryDrawer(
                onClose = { scope.launch { drawerState.close() } },
                onOpenFavorites = { showFavoritesDialog = true },
                onOpenNotes = { showNotesDialog = true },
                onOpenSettings = { showSettingsDialog = true },
                onOpenAbout = { showAboutDialog = true }
            )
        },
    ) {
        Column(Modifier.fillMaxSize().background(Color.White)) {
            LibraryTopBar(
                searching = searching,
                query = query,
                onQueryChange = { query = it },
                onMenu = { scope.launch { drawerState.open() } },
                onSearchToggle = {
                    if (searching) query = ""
                    searching = !searching
                },
                onThemeClick = { showThemeDialog = true }
            )

            if (searching && q.length >= 2) {
                // Külliyat İçi Tam Metin Arama Görünümü
                Box(modifier = Modifier.fillMaxSize().background(Color(0xFFFAF7F2))) {
                    if (isSearchingFullText) {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = Color(0xFF7B2412))
                            Spacer(Modifier.height(12.dp))
                            Text("Tüm Külliyat içinde aranıyor...", fontSize = 14.sp, color = Color.Gray)
                        }
                    } else {
                        val results = searchResults ?: emptyList()
                        if (results.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Arama kriterinize uygun sonuç bulunamadı.", color = Color.Gray, fontSize = 14.sp)
                            }
                        } else {
                            Column(modifier = Modifier.fillMaxSize()) {
                                Text(
                                    text = "Külliyat İçi Arama Sonuçları (${results.size} Adet Bulundu)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF7B2412),
                                    modifier = Modifier.padding(16.dp)
                                )
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = WindowInsets.navigationBars.asPaddingValues()
                                ) {
                                    items(results) { item ->
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color(0xFFFFF8E7),
                                            shadowElevation = 1.5.dp,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 16.dp, vertical = 5.dp)
                                                .clickable { onOpenBookAtPageIndex(item.book, item.pageIndex, query) }
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        modifier = Modifier.weight(1f, fill = false)
                                                    ) {
                                                        Text(
                                                            text = item.book.title,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 14.sp,
                                                            color = Color(0xFF7B2412)
                                                        )
                                                        Spacer(Modifier.width(6.dp))
                                                        Text(
                                                            text = "• ${item.sectionTitle}",
                                                            fontSize = 12.sp,
                                                            color = Color(0xFF5D4037),
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                    }
                                                    Spacer(Modifier.width(8.dp))
                                                    Text(
                                                        text = "Sayfa ${item.pageNumber}",
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 12.sp,
                                                        color = Color(0xFF7B2412)
                                                    )
                                                }
                                                Spacer(Modifier.height(6.dp))
                                                Text(
                                                    text = item.snippetAnnotated,
                                                    fontSize = 13.sp,
                                                    color = Color.DarkGray,
                                                    lineHeight = 18.sp,
                                                    maxLines = 3,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = WindowInsets.navigationBars.asPaddingValues(),
                ) {
                    items(visibleShelves, key = { it.key }) { shelf ->
                        ShelfSection(
                            shelf = shelf,
                            prefs = prefs,
                            themeConfig = themeConfig,
                            forceOpen = searching && q.isNotEmpty(),
                            onOpenBook = onOpenBook,
                            onResetBook = onResetBook,
                            onRemoveRecentBook = onRemoveRecentBook,
                        )
                    }
                }
            }
        }
    }

    if (showThemeDialog) {
        LibraryThemeDialog(
            currentThemeKey = currentThemeKey,
            onDismiss = { showThemeDialog = false },
            onSelectTheme = { selectedKey ->
                currentThemeKey = selectedKey
                prefs.libraryTheme = selectedKey
                showThemeDialog = false
            }
        )
    }

    if (showFavoritesDialog) {
        FavoritesDialog(
            shelves = shelves,
            prefs = prefs,
            onDismiss = { showFavoritesDialog = false },
            onOpenBookAtPageIndex = { book, pageIdx ->
                showFavoritesDialog = false
                onOpenBookAtPageIndex(book, pageIdx, "")
            }
        )
    }

    if (showNotesDialog) {
        NotesDialog(
            prefs = prefs,
            onDismiss = { showNotesDialog = false }
        )
    }

    if (showSettingsDialog) {
        LibrarySettingsDialog(
            prefs = prefs,
            currentThemeKey = currentThemeKey,
            onDismiss = { showSettingsDialog = false },
            onSelectTheme = { selectedKey ->
                currentThemeKey = selectedKey
                prefs.libraryTheme = selectedKey
            },
            onReloadShelves = onReloadShelves
        )
    }

    if (showAboutDialog) {
        AboutDialog(onDismiss = { showAboutDialog = false })
    }
}

// ---------------------------------------------------------------- üst çubuk

@Composable
private fun LibraryTopBar(
    searching: Boolean,
    query: String,
    onQueryChange: (String) -> Unit,
    onMenu: () -> Unit,
    onSearchToggle: () -> Unit,
    onThemeClick: () -> Unit
) {
    val focus = remember { FocusRequester() }
    LaunchedEffect(searching) { if (searching) focus.requestFocus() }
    val gray = Color(0xFF555555)

    Row(
        Modifier
            .fillMaxWidth()
            .background(Color.White)
            .statusBarsPadding()
            .height(56.dp)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onMenu) {
            Icon(Icons.Default.Menu, contentDescription = "Menü", tint = gray)
        }
        if (searching) {
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                textStyle = TextStyle(fontSize = 18.sp, color = Color(0xFF333333)),
                modifier = Modifier.weight(1f).padding(start = 8.dp).focusRequester(focus),
                decorationBox = { inner ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (query.isEmpty()) {
                            Text("Tüm Külliyatta Ara…", color = Color(0xFF999999), fontSize = 18.sp)
                        }
                        inner()
                    }
                },
            )
            IconButton(onClick = onSearchToggle) {
                Icon(Icons.Default.Close, contentDescription = "Aramayı kapat", tint = gray)
            }
        } else {
            Text(
                text = "Kütüphane",
                color = gray,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f).padding(start = 12.dp),
            )
            IconButton(onClick = onThemeClick) {
                Icon(Icons.Default.Palette, contentDescription = "Kitaplık Teması", tint = gray)
            }
            IconButton(onClick = onSearchToggle) {
                Icon(Icons.Default.Search, contentDescription = "Ara", tint = gray)
            }
        }
    }
}

// ---------------------------------------------------------------- hamburger panel (Risale Bordo Çubuk & FontRLN)

@Composable
private fun LibraryDrawer(
    onClose: () -> Unit,
    onOpenFavorites: () -> Unit,
    onOpenNotes: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenAbout: () -> Unit
) {
    val context = LocalContext.current
    val risaleFont = remember { getFontFamily(context, "fontrln") }

    ModalDrawerSheet {
        // En Üst Koyu Bordo Risale Çubuğu Header
        Column(
            Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF7B2412), Color(0xFF5A140A))
                    )
                )
                .padding(horizontal = 20.dp, vertical = 26.dp),
        ) {
            Text(
                text = "Kütüphane",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFFFFF4DC)
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Risale-i Nur Külliyatı",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontFamily = risaleFont
            )
        }
        Spacer(Modifier.height(8.dp))
        val colors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = Color(0xFFF1DDB5),
            selectedIconColor = Color(0xFF7B2412),
            selectedTextColor = Color(0xFF7B2412),
        )
        val itemModifier = Modifier.padding(horizontal = 12.dp)
        NavigationDrawerItem(
            label = { Text("Kütüphane") },
            selected = true,
            onClick = onClose,
            icon = { Icon(Icons.Default.Home, contentDescription = null) },
            colors = colors,
            modifier = itemModifier,
        )
        NavigationDrawerItem(
            label = { Text("Notlarım") },
            selected = false,
            onClick = {
                onClose()
                onOpenNotes()
            },
            icon = { Icon(Icons.Default.EditNote, contentDescription = null) },
            colors = colors,
            modifier = itemModifier,
        )
        NavigationDrawerItem(
            label = { Text("Favoriler & Yer İmleri") },
            selected = false,
            onClick = {
                onClose()
                onOpenFavorites()
            },
            icon = { Icon(Icons.Default.Favorite, contentDescription = null) },
            colors = colors,
            modifier = itemModifier,
        )
        NavigationDrawerItem(
            label = { Text("Ayarlar") },
            selected = false,
            onClick = {
                onClose()
                onOpenSettings()
            },
            icon = { Icon(Icons.Default.Settings, contentDescription = null) },
            colors = colors,
            modifier = itemModifier,
        )
        NavigationDrawerItem(
            label = { Text("Hakkında") },
            selected = false,
            onClick = {
                onClose()
                onOpenAbout()
            },
            icon = { Icon(Icons.Default.Info, contentDescription = null) },
            colors = colors,
            modifier = itemModifier,
        )
        Spacer(Modifier.height(16.dp))
        Text(
            "Risale-i Nur Külliyatı Dijital Kütüphanesi v1.2",
            modifier = Modifier.padding(horizontal = 24.dp),
            fontSize = 12.sp,
            color = Color(0xFF888888),
        )
    }
}

// ---------------------------------------------------------------- raf

@Composable
private fun ShelfSection(
    shelf: Shelf,
    prefs: Prefs,
    themeConfig: LibraryThemeConfig,
    forceOpen: Boolean,
    onOpenBook: (Book) -> Unit,
    onResetBook: (Book) -> Unit,
    onRemoveRecentBook: (Book) -> Unit,
) {
    var expanded by remember(shelf.key) { mutableStateOf(prefs.shelfOpen(shelf.key, shelf.defaultOpen)) }
    val open = expanded || forceOpen
    val isRecentShelf = shelf.key == "son-okunanlar" || shelf.title == "Son Okunanlar"

    Column(Modifier.fillMaxWidth()) {
        ShelfPlaqueHeader(
            title = shelf.title,
            open = open,
            themeConfig = themeConfig,
            onToggle = {
                if (!forceOpen) {
                    expanded = !expanded
                    prefs.setShelfOpen(shelf.key, expanded)
                }
            },
        )
        AnimatedVisibility(
            visible = open,
            enter = expandVertically(),
            exit = shrinkVertically(),
        ) {
            ShelfBody(
                books = shelf.books,
                isRecentShelf = isRecentShelf,
                themeConfig = themeConfig,
                onOpenBook = onOpenBook,
                onResetBook = onResetBook,
                onRemoveRecentBook = onRemoveRecentBook,
            )
        }
    }
}

// Etiket Başlığı
@Composable
private fun ShelfPlaqueHeader(
    title: String,
    open: Boolean,
    themeConfig: LibraryThemeConfig,
    onToggle: () -> Unit
) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(42.dp)
            .background(
                Brush.verticalGradient(themeConfig.headerBg)
            )
            .drawBehind {
                drawRect(Color(0x55FFFFFF), size = Size(size.width, 1.5.dp.toPx()))
                drawRect(
                    Color(0x66000000),
                    topLeft = Offset(0f, size.height - 1.5.dp.toPx()),
                    size = Size(size.width, 1.5.dp.toPx()),
                )
            }
            .clickable(onClick = onToggle),
    ) {
        // Fildişi / Krem Ahşaba Çakma Etiket Plaket (Plaque)
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = themeConfig.plaqueBg,
            shadowElevation = 5.dp,
            modifier = Modifier
                .align(Alignment.Center)
                .border(1.5.dp, themeConfig.plaqueBorder, RoundedCornerShape(14.dp))
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                // Sol pirinç çivi başı süsü
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(Color(0xFFB8860B), CircleShape)
                        .border(0.5.dp, Color(0xFFFFE4B5), CircleShape)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,
                    color = themeConfig.plaqueText,
                    maxLines = 1
                )
                Spacer(Modifier.width(8.dp))
                // Sağ pirinç çivi başı süsü
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(Color(0xFFB8860B), CircleShape)
                        .border(0.5.dp, Color(0xFFFFE4B5), CircleShape)
                )
            }
        }

        val rotation by animateFloatAsState(if (open) 0f else 90f, label = "arrow")
        Box(
            Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 8.dp)
                .size(width = 24.dp, height = 22.dp)
                .background(Color(0xFF7B2412), RoundedCornerShape(4.dp))
                .border(1.dp, Color(0xFFE8C98A), RoundedCornerShape(4.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Default.KeyboardArrowRight,
                contentDescription = if (open) "Rafı kapat" else "Rafı aç",
                tint = Color(0xFFFFE9B8),
                modifier = Modifier.size(18.dp).rotate(rotation),
            )
        }
    }
}

// 3D Derinlikli Ahşap Kalas / Raf
@Composable
private fun Shelf3DPlank(themeConfig: LibraryThemeConfig) {
    Column(Modifier.fillMaxWidth()) {
        // 1. Rafın Üst Yüzeyi (Bal / Meşe #E8B275 + Üst Işıltı Yansıması)
        Box(
            Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(
                    Brush.verticalGradient(themeConfig.plankTop)
                )
                .drawBehind {
                    drawRect(Color(0xCCFFFFFF), size = Size(size.width, 1.5.dp.toPx()))
                }
        )

        // 2. Rafın Ön Pervazı (16px kalınlık)
        Box(
            Modifier
                .fillMaxWidth()
                .height(16.dp)
                .background(
                    Brush.verticalGradient(themeConfig.plankFront)
                )
                .drawBehind {
                    drawRect(Color(0x88FFFFFF), size = Size(size.width, 1.dp.toPx()))
                    drawRect(
                        Color(0x88000000),
                        topLeft = Offset(0f, size.height - 1.5.dp.toPx()),
                        size = Size(size.width, 1.5.dp.toPx())
                    )
                }
        )

        // 3. Alt Derinlik Gölgesi (Derin 3D Drop-Shadow)
        Box(
            Modifier
                .fillMaxWidth()
                .height(14.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xAA000000), Color(0x44000000), Color.Transparent)
                    )
                )
        )
    }
}

@Composable
private fun ShelfBody(
    books: List<Book>,
    isRecentShelf: Boolean,
    themeConfig: LibraryThemeConfig,
    onOpenBook: (Book) -> Unit,
    onResetBook: (Book) -> Unit,
    onRemoveRecentBook: (Book) -> Unit,
) {
    val shelfHeight = if (books.isEmpty()) 140.dp else BookHeight + 76.dp
    Column(Modifier.fillMaxWidth()) {
        // Açık Sıcak Meşe Kitaplık Arka Paneli (#8D5B36 -> #563117)
        Box(
            Modifier
                .fillMaxWidth()
                .height(shelfHeight)
                .background(
                    Brush.verticalGradient(themeConfig.bodyBg)
                )
                .drawBehind {
                    // Sol ve Sağ İç Duvar Gölgesi (3D Yan Paneller)
                    val w = 16.dp.toPx()
                    drawRect(
                        Brush.horizontalGradient(listOf(Color(0x66000000), Color.Transparent), startX = 0f, endX = w),
                        size = Size(w, size.height)
                    )
                    drawRect(
                        Brush.horizontalGradient(listOf(Color.Transparent, Color(0x66000000)), startX = size.width - w, endX = size.width),
                        topLeft = Offset(size.width - w, 0f),
                        size = Size(w, size.height)
                    )
                },
        ) {
            // Rafın üst kısmındaki iç gölge
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .background(Brush.verticalGradient(listOf(Color(0x77000000), Color.Transparent))),
            )

            if (books.isEmpty()) {
                EmptyShelfLabel(Modifier.align(Alignment.Center))
            } else {
                LazyRow(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 0.dp),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    items(books, key = { it.id }) { book ->
                        BookItem(
                            book = book,
                            isRecentShelf = isRecentShelf,
                            themeConfig = themeConfig,
                            onOpen = { onOpenBook(book) },
                            onReset = { onResetBook(book) },
                            onRemoveRecent = { onRemoveRecentBook(book) }
                        )
                    }
                }
            }
        }

        // 3D Ahşap Kalas / Raf Pervazı
        Shelf3DPlank(themeConfig)
    }
}

@Composable
private fun EmptyShelfLabel(modifier: Modifier = Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        val cream = Color(0xFFFFF3E0)
        Canvas(Modifier.size(width = 30.dp, height = 26.dp)) {
            val w = size.width / 5f
            val heights = listOf(0.75f, 1f, 0.85f, 0.95f)
            heights.forEachIndexed { i, hFrac ->
                val h = size.height * hFrac
                drawRect(
                    cream,
                    topLeft = Offset(i * (w + 2.dp.toPx() / 2), size.height - h),
                    size = Size(w, h),
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Text("Raf boş", color = cream, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
    }
}

// ---------------------------------------------------------------- kitap

@Composable
private fun BookItem(
    book: Book,
    isRecentShelf: Boolean,
    themeConfig: LibraryThemeConfig,
    onOpen: () -> Unit,
    onReset: () -> Unit,
    onRemoveRecent: () -> Unit
) {
    var menuOpen by remember { mutableStateOf(false) }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(BookWidth)
    ) {
        // Kitap resminin hemen üstündeki başlık
        Text(
            text = book.title,
            color = themeConfig.bookTitleColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp)
        )
        Box(Modifier.width(BookWidth).height(BookHeight)) {
            BookCover(
                book = book,
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(onClick = onOpen)
            )

            // Sağ üst menü ikonu
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(22.dp)
                    .background(Color(0xCC000000), RoundedCornerShape(4.dp))
                    .clickable { menuOpen = true },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Menu,
                    contentDescription = "Kitap seçenekleri",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )

                DropdownMenu(
                    expanded = menuOpen,
                    onDismissRequest = { menuOpen = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Baştan Başla") },
                        onClick = {
                            menuOpen = false
                            onReset()
                        }
                    )
                    if (isRecentShelf) {
                        DropdownMenuItem(
                            text = { Text("Son Okunanlardan Çıkar") },
                            onClick = {
                                menuOpen = false
                                onRemoveRecent()
                            }
                        )
                    }
                }
            }
        }

        // Kitabın altına 3D derinlikli oval zemin gölgesi (Rafta dik durma etkisi)
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
        ) {
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xEE000000), Color(0x66000000), Color.Transparent),
                    center = Offset(size.width / 2f, size.height / 2f),
                    radius = size.width / 1.7f
                )
            )
        }
    }
}

// ---------------------------------------------------------------- Kitaplık Temaları Ayar Diyaloğu

@Composable
private fun LibraryThemeDialog(
    currentThemeKey: String,
    onDismiss: () -> Unit,
    onSelectTheme: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Palette, contentDescription = null, tint = Color(0xFF7B2412))
                Spacer(Modifier.width(8.dp))
                Text("Kitaplık Teması", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Kütüphane ekranınız için bir tema seçiniz:", fontSize = 13.sp, color = Color.Gray)
                Spacer(Modifier.height(4.dp))
                LibraryThemeConfig.entries.forEach { theme ->
                    val selected = currentThemeKey == theme.key
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (selected) Color(0xFF7B2412) else Color(0xFFFAF2E6),
                        shadowElevation = if (selected) 3.dp else 1.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectTheme(theme.key) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = theme.title,
                                fontSize = 14.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                color = if (selected) Color.White else Color(0xFF3B2412)
                            )
                            if (selected) {
                                Text("✓ Seçili", fontSize = 12.sp, color = Color(0xFFFFD54F), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Kapat") }
        }
    )
}

// ---------------------------------------------------------------- Notlarım Diyaloğu (Çoklu Not Alma & Düzenleme)

@Composable
private fun NotesDialog(
    prefs: Prefs,
    onDismiss: () -> Unit
) {
    var notesList by remember { mutableStateOf(prefs.getNotes()) }
    var editingNote by remember { mutableStateOf<NoteItem?>(null) }
    var isAddingNew by remember { mutableStateOf(false) }

    var titleInput by remember { mutableStateOf("") }
    var contentInput by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.EditNote, contentDescription = null, tint = Color(0xFF7B2412))
                    Spacer(Modifier.width(8.dp))
                    Text("Notlarım", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                if (!isAddingNew && editingNote == null) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF7B2412),
                        modifier = Modifier.clickable {
                            titleInput = ""
                            contentInput = ""
                            isAddingNew = true
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Yeni Not", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (isAddingNew || editingNote != null) {
                    // Not Oluşturma / Düzenleme Formu
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = titleInput,
                            onValueChange = { titleInput = it },
                            label = { Text("Not Başlığı...") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = contentInput,
                            onValueChange = { contentInput = it },
                            label = { Text("Not İçeriği...") },
                            minLines = 4,
                            maxLines = 6,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = {
                                isAddingNew = false
                                editingNote = null
                            }) {
                                Text("İptal")
                            }
                            Spacer(Modifier.width(8.dp))
                            TextButton(onClick = {
                                if (titleInput.isNotBlank() || contentInput.isNotBlank()) {
                                    val targetTitle = titleInput.ifBlank { "Not" }
                                    prefs.saveNote(targetTitle, contentInput, editingNote?.id)
                                    notesList = prefs.getNotes()
                                    isAddingNew = false
                                    editingNote = null
                                }
                            }) {
                                Text("Kaydet", fontWeight = FontWeight.Bold, color = Color(0xFF7B2412))
                            }
                        }
                    }
                } else {
                    // Not Listesi
                    if (notesList.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Henüz kaydedilmiş notunuz yok.\n'Yeni Not' butonuna basarak ekleyebilirsiniz.", fontSize = 12.sp, color = Color.Gray, textAlign = TextAlign.Center)
                        }
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxWidth().height(300.dp)) {
                            items(notesList, key = { it.id }) { note ->
                                val dateStr = remember(note.timestamp) {
                                    SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date(note.timestamp))
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFFFF8E7),
                                    shadowElevation = 1.dp,
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(note.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF7B2412))
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(dateStr, fontSize = 10.sp, color = Color.Gray)
                                                Spacer(Modifier.width(6.dp))
                                                IconButton(
                                                    onClick = {
                                                        editingNote = note
                                                        titleInput = note.title
                                                        contentInput = note.content
                                                    },
                                                    modifier = Modifier.size(22.dp)
                                                ) {
                                                    Icon(Icons.Default.Edit, contentDescription = "Düzenle", tint = Color.DarkGray, modifier = Modifier.size(14.dp))
                                                }
                                                IconButton(
                                                    onClick = {
                                                        prefs.deleteNote(note.id)
                                                        notesList = prefs.getNotes()
                                                    },
                                                    modifier = Modifier.size(22.dp)
                                                ) {
                                                    Icon(Icons.Default.Delete, contentDescription = "Sil", tint = Color(0xFFD32F2F), modifier = Modifier.size(14.dp))
                                                }
                                            }
                                        }
                                        if (note.content.isNotBlank()) {
                                            Spacer(Modifier.height(4.dp))
                                            Text(note.content, fontSize = 12.sp, color = Color.DarkGray, maxLines = 4, overflow = TextOverflow.Ellipsis)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (!isAddingNew && editingNote == null) {
                TextButton(onClick = onDismiss) { Text("Kapat") }
            }
        }
    )
}

// ---------------------------------------------------------------- Favoriler & Yer İmleri Diyaloğu

@Composable
private fun FavoritesDialog(
    shelves: List<Shelf>,
    prefs: Prefs,
    onDismiss: () -> Unit,
    onOpenBookAtPageIndex: (Book, Int) -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Yer İmleri, 1: Son Okunanlar

    val allBooks = remember(shelves) { shelves.flatMap { it.books }.distinctBy { it.id } }

    val bookmarkedPages = remember(allBooks) {
        val list = ArrayList<Triple<Book, Int, String>>() // Book, pageNum, snippet
        for (book in allBooks) {
            val bms = prefs.bookmarks(book.id)
            if (bms.isNotEmpty()) {
                val pages = Repository.loadPages(context, book)
                for (bmPageNum in bms) {
                    val pageIdx = pages.indexOfFirst { it.number == bmPageNum }
                    val snippet = if (pageIdx >= 0) pages[pageIdx].text.take(80).replace("\n", " ").trim() + "..." else "Sayfa $bmPageNum"
                    list.add(Triple(book, bmPageNum, snippet))
                }
            }
        }
        list
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Favorite, contentDescription = null, tint = Color(0xFF7B2412))
                Spacer(Modifier.width(8.dp))
                Text("Favoriler & Koleksiyonum", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Tab Seçici
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (selectedTab == 0) Color(0xFF7B2412) else Color(0xFFEEEEEE),
                        modifier = Modifier.weight(1f).clickable { selectedTab = 0 }
                    ) {
                        Text(
                            text = "📑 Yer İmleri (${bookmarkedPages.size})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedTab == 0) Color.White else Color.Black,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (selectedTab == 1) Color(0xFF7B2412) else Color(0xFFEEEEEE),
                        modifier = Modifier.weight(1f).clickable { selectedTab = 1 }
                    ) {
                        Text(
                            text = "⏱️ Son Okunanlar",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selectedTab == 1) Color.White else Color.Black,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }

                if (selectedTab == 0) {
                    if (bookmarkedPages.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Henüz kaydedilmiş yer imi bulunmuyor.\nOkuma ekranındaki yer imi ikonuna basarak ekleyebilirsiniz.", fontSize = 12.sp, color = Color.Gray, textAlign = TextAlign.Center)
                        }
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxWidth().height(280.dp)) {
                            items(bookmarkedPages) { (book, pageNum, snippet) ->
                                val pages = remember(book.id) { Repository.loadPages(context, book) }
                                val pageIdx = remember(pages, pageNum) { (pages.indexOfFirst { it.number == pageNum }).coerceAtLeast(0) }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFFFF8E7),
                                    shadowElevation = 1.dp,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clickable { onOpenBookAtPageIndex(book, pageIdx) }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("${book.title} - Sayfa $pageNum", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF7B2412))
                                            Spacer(Modifier.height(2.dp))
                                            Text(snippet, fontSize = 11.sp, color = Color.DarkGray, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                        }
                                        Icon(Icons.Default.Bookmark, contentDescription = null, tint = Color(0xFFD32F2F), modifier = Modifier.size(20.dp))
                                    }
                                }
                            }
                        }
                    }
                } else {
                    val recentBookIds = prefs.recentBookIds()
                    val recentBooks = remember(recentBookIds, allBooks) {
                        recentBookIds.mapNotNull { id -> allBooks.firstOrNull { it.id == id } }
                    }

                    if (recentBooks.isEmpty()) {
                        Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                            Text("Okuma geçmişi bulunamadı.", fontSize = 12.sp, color = Color.Gray)
                        }
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxWidth().height(280.dp)) {
                            items(recentBooks) { book ->
                                val pageIdx = prefs.position(book.id)
                                val readSecs = prefs.readingTimeSeconds(book.id)
                                val readMins = (readSecs / 60).toInt()

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFFFF8E7),
                                    shadowElevation = 1.dp,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clickable { onOpenBookAtPageIndex(book, pageIdx) }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(book.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF7B2412))
                                            Spacer(Modifier.height(2.dp))
                                            Text("Süre: $readMins Dakika • Kalınan Sayfa İndeksi: ${pageIdx + 1}", fontSize = 11.sp, color = Color.DarkGray)
                                        }
                                        Text("Devam Et >", fontSize = 11.sp, color = Color(0xFF7B2412), fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Kapat") }
        }
    )
}

// ---------------------------------------------------------------- Gelişmiş Ayarlar & Sekmeli Yedekleme Diyaloğu

@Composable
private fun LibrarySettingsDialog(
    prefs: Prefs,
    currentThemeKey: String,
    onDismiss: () -> Unit,
    onSelectTheme: (String) -> Unit,
    onReloadShelves: () -> Unit
) {
    val context = LocalContext.current
    var selectedSettingsTab by remember { mutableIntStateOf(0) } // 0: Okuma & Renkler, 1: Temalar, 2: Yedekleme (İçe/Dışa Aktar)

    var keepScreenOn by remember { mutableStateOf(prefs.keepScreenOn) }
    var currentPageColor by remember { mutableStateOf(prefs.pageColorTheme) }
    var exportedFilePath by remember { mutableStateOf<String?>(null) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    // Sistem Dosya Seçici (İçe Aktar için)
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            try {
                val content = context.contentResolver.openInputStream(selectedUri)?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }
                if (!content.isNullOrBlank()) {
                    val success = prefs.restoreBackupJson(context, content)
                    if (success) {
                        onReloadShelves()
                        currentPageColor = prefs.pageColorTheme
                        onSelectTheme(prefs.libraryTheme)
                        statusMessage = "✓ Tüm yer imleri ve son okunan kitaplar başarıyla yüklendi!"
                    } else {
                        statusMessage = "⚠️ Seçilen dosya geçerli bir yedek formatı değil!"
                    }
                }
            } catch (e: Exception) {
                statusMessage = "⚠️ Dosya okunurken hata oluştu."
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Settings, contentDescription = null, tint = Color(0xFF7B2412))
                Spacer(Modifier.width(8.dp))
                Text("Ayarlar", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Sekme Başlıkları
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val tabs = listOf("📖 Okuma", "🎨 Temalar", "💾 Yedekleme")
                    tabs.forEachIndexed { index, label ->
                        val selected = selectedSettingsTab == index
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (selected) Color(0xFF7B2412) else Color(0xFFEEEEEE),
                            modifier = Modifier.weight(1f).clickable { selectedSettingsTab = index }
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selected) Color.White else Color.Black,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                }

                when (selectedSettingsTab) {
                    0 -> {
                        // Sekme 0: Okuma & Sayfa Renkleri
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Ekran Uykusunu Engelle", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                    Text("Okuma yaparken ekranınız kapanmaz", fontSize = 11.sp, color = Color.Gray)
                                }
                                Switch(
                                    checked = keepScreenOn,
                                    onCheckedChange = {
                                        keepScreenOn = it
                                        prefs.keepScreenOn = it
                                    }
                                )
                            }
                            Box(Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFEEEEEE)))

                            // Sayfa Renk Temaları
                            Text("Varsayılan Sayfa Arka Plan Rengi", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7B2412))
                            val pageColors = listOf(
                                "gunduz" to ("Gündüz (Krem)" to Color(0xFFF7F2EB)),
                                "sepya" to ("Sepya" to Color(0xFFF4ECD8)),
                                "yesil" to ("Soft Yeşil" to Color(0xFFE8F2E8)),
                                "mavi" to ("Soft Mavi" to Color(0xFFEBF2F7)),
                                "gece" to ("Gece Modu" to Color(0xFF1B1B1B))
                            )
                            pageColors.forEach { (key, info) ->
                                val (name, colorVal) = info
                                val isSel = currentPageColor == key
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = colorVal,
                                    shadowElevation = 1.dp,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.5.dp, if (isSel) Color(0xFF7B2412) else Color.Transparent, RoundedCornerShape(8.dp))
                                        .clickable {
                                            currentPageColor = key
                                            prefs.pageColorTheme = key
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(name, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (key == "gece") Color.White else Color.Black)
                                        if (isSel) {
                                            Text("✓ Seçili", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7B2412))
                                        }
                                    }
                                }
                            }
                        }
                    }
                    1 -> {
                        // Sekme 1: Temalar
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Kitaplık Görünüm Teması Seçiniz:", fontSize = 12.sp, color = Color.Gray)
                            LibraryThemeConfig.entries.forEach { theme ->
                                val selected = currentThemeKey == theme.key
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (selected) Color(0xFF7B2412) else Color(0xFFFAF2E6),
                                    shadowElevation = if (selected) 2.dp else 1.dp,
                                    modifier = Modifier.fillMaxWidth().clickable { onSelectTheme(theme.key) }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = theme.title,
                                            fontSize = 13.sp,
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (selected) Color.White else Color(0xFF3B2412)
                                        )
                                        if (selected) {
                                            Text("✓ Seçili", fontSize = 11.sp, color = Color(0xFFFFD54F), fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    2 -> {
                        // Sekme 2: Yedekleme (İçe/Dışa Aktar)
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            // ℹ️ "i" Harfli Bilgi Balonu
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFE3F2FD),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF90CAF9)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(modifier = Modifier.padding(10.dp)) {
                                    Icon(
                                        Icons.Default.Info,
                                        contentDescription = null,
                                        tint = Color(0xFF1976D2),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Column {
                                        Text("Yedekleme & Aktarım Rehberi", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF0D47A1))
                                        Spacer(Modifier.height(2.dp))
                                        Text(
                                            text = "• Dışa Aktar: Yer imlerinizi, notlarınızı ve okuma konumlarınızı 'Downloads' klasörünüze .json dosyası olarak kaydeder.\n• İçe Aktar: Telefon değiştirdiğinizde 'İçe Aktar' butonuna basarak dosya yöneticisinden yedek dosyasını seçmeniz yeterlidir.",
                                            fontSize = 11.sp,
                                            color = Color(0xFF1565C0),
                                            lineHeight = 15.sp
                                        )
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Dışa Aktar (Dosyaya Kaydet)
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF7B2412),
                                    modifier = Modifier.weight(1f).clickable {
                                        val exportedFile = prefs.exportBackupFile(context)
                                        if (exportedFile != null) {
                                            exportedFilePath = exportedFile.absolutePath
                                            statusMessage = "✓ Yedek dosyası Downloads klasörüne kaydedildi!"
                                        } else {
                                            statusMessage = "⚠️ Dosya oluşturulurken hata meydana geldi."
                                        }
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(vertical = 10.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Upload, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Dışa Aktar", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }

                                // İçe Aktar (Dosyadan Yükle)
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF5D4037),
                                    modifier = Modifier.weight(1f).clickable {
                                        filePickerLauncher.launch("*/*")
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(vertical = 10.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Download, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("İçe Aktar", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            if (exportedFilePath != null) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFFFF8E7),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text("Kaydedilen Dosya Yolu:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF7B2412))
                                        Text(exportedFilePath!!, fontSize = 10.sp, color = Color.DarkGray)
                                        Spacer(Modifier.height(4.dp))
                                        Row(
                                            modifier = Modifier.clickable {
                                                try {
                                                    val intent = Intent(Intent.ACTION_VIEW).apply {
                                                        setDataAndType(Uri.parse(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path), "*/*")
                                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                    }
                                                    context.startActivity(intent)
                                                } catch (_: Exception) {}
                                            },
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.FolderOpen, contentDescription = null, tint = Color(0xFF7B2412), modifier = Modifier.size(14.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text("Klasörü Aç (Downloads)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7B2412))
                                        }
                                    }
                                }
                            }

                            if (statusMessage != null) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFE8F5E9),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = statusMessage!!,
                                        fontSize = 11.sp,
                                        color = Color(0xFF2E7D32),
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Tamam") }
        }
    )
}

// ---------------------------------------------------------------- Hakkında Diyaloğu

@Composable
private fun AboutDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF7B2412))
                Spacer(Modifier.width(8.dp))
                Text("Hakkında", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Risale-i Nur Külliyatı Dijital Kütüphanesi",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF7B2412)
                )
                Text(
                    text = "Bediüzzaman Said Nursî Hazretleri tarafından kaleme alınan Risale-i Nur Külliyatı'nın tüm eserlerini dijital ortamda en yüksek kalite, tefekkür modu, lügat ve ayet meâli desteğiyle okuma imkânı sunar.",
                    fontSize = 12.sp,
                    color = Color.DarkGray,
                    lineHeight = 17.sp
                )
                Box(Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFEEEEEE)))
                Text("📚 Kaynak ve Basım Referansı:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF7B2412))
                Text("Eser metinleri Diyanet İşleri Başkanlığı ve Hizmet Vakfı neşriyatı orijinal nüshaları esas alınarak hazırlanmıştır.", fontSize = 11.sp, color = Color.Gray)

                Box(Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFEEEEEE)))
                Text("Sürüm: v1.2.0 Pro", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                Text("İlim, irfan ve hayır maksadıyla geliştirilmiştir.", fontSize = 11.sp, color = Color.Gray)

                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF7B2412),
                        modifier = Modifier.weight(1f).clickable {
                            shareAppRecommendation(context)
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Tavsiye Et / Paylaş", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Kapat") }
        }
    )
}