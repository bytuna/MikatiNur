package com.example.mkat_nur.ui.kutuphane

import android.content.Context
import android.graphics.Typeface
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.FormatStrikethrough
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val PaperColor = Color(0xFFF7F2EB)
private val SepyaColor = Color(0xFFF4ECD8)
private val NightBgColor = Color(0xFF1B1B1B)

private val TopBarDark = Color(0xFF7B2412) // Risale-i Nur Bordo Kırmızı Tonu
private val TextInk = Color(0xFF2B1B0E)
private val PageInk = Color(0xFF6B4A2B)

fun getFontFamily(context: Context, key: String): FontFamily {
    return when (key) {
        "fontrln", "risale" -> loadTypefaceFromAsset(context, "fonts/fontrln.ttf")
            ?: loadTypefaceFromAsset(context, "fonts/Sozler.ttf")
            ?: FontFamily.Serif
        "arabic_uthmantaha", "uthman_taha" -> loadTypefaceFromAsset(context, "fonts/arabic_uthmantaha.ttf")
            ?: loadTypefaceFromAsset(context, "fonts/UthmanTaha.ttf")
            ?: FontFamily.Serif
        "arabic_amiri", "amiri" -> loadTypefaceFromAsset(context, "fonts/arabic_amiri.ttf")
            ?: loadTypefaceFromAsset(context, "fonts/Amiri.ttf")
            ?: FontFamily.Serif
        "arabic_naskh", "naskh" -> loadTypefaceFromAsset(context, "fonts/arabic_naskh.ttf")
            ?: FontFamily.Serif
        "lora", "turkish_lora" -> loadTypefaceFromAsset(context, "fonts/turkish_lora.ttf")
            ?: FontFamily.Serif
        "inter", "sans" -> loadTypefaceFromAsset(context, "fonts/turkish_inter.ttf")
            ?: FontFamily.SansSerif
        "monospace" -> FontFamily.Monospace
        else -> FontFamily.Serif
    }
}

private fun loadTypefaceFromAsset(context: Context, path: String): FontFamily? {
    return try {
        context.assets.open(path).close()
        val tf = Typeface.createFromAsset(context.assets, path)
        FontFamily(tf)
    } catch (_: Exception) {
        null
    }
}

@Composable
fun ReaderScreen(
    initialBook: Book,
    allShelves: List<Shelf>,
    prefs: Prefs,
    searchQuery: String? = null,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current

    // Okuma sırasında ekran kararmasın; ayarlardaki "Ekran açık kalsın" tercihini uygula
    val readerView = LocalView.current
    DisposableEffect(prefs.keepScreenOn) {
        readerView.keepScreenOn = prefs.keepScreenOn
        onDispose { readerView.keepScreenOn = false }
    }

    var openBooks by remember { mutableStateOf(listOf(initialBook)) }
    var activeBookIndex by remember { mutableIntStateOf(0) }
    var showAddBookDialog by remember { mutableStateOf(false) }

    val activeBook = openBooks.getOrElse(activeBookIndex) { openBooks.first() }

    LaunchedEffect(activeBook.id) {
        prefs.addRecentBook(activeBook.id)
    }

    val pages by produceState<List<BookPage>?>(null, activeBook.id) {
        value = withContext(Dispatchers.IO) {
            runCatching { Repository.loadPages(context, activeBook) }.getOrNull()
        }
    }

    val loaded = pages
    if (loaded == null) {
        Box(
            Modifier.fillMaxSize().background(PaperColor),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(color = Color(0xFF7B2412))
        }
    } else {
        ReaderContent(
            book = activeBook,
            pages = loaded,
            openBooks = openBooks,
            activeBookIndex = activeBookIndex,
            prefs = prefs,
            searchQuery = searchQuery,
            onBack = onBack,
            onTabSelect = { newIdx -> activeBookIndex = newIdx },
            onTabClose = { index ->
                if (openBooks.size <= 1) {
                    onBack()
                } else {
                    val newBooks = openBooks.toMutableList()
                    newBooks.removeAt(index)
                    openBooks = newBooks
                    if (activeBookIndex >= newBooks.size) {
                        activeBookIndex = newBooks.lastIndex
                    }
                }
            },
            onAddClick = { showAddBookDialog = true }
        )
    }

    if (showAddBookDialog) {
        AddBookDialog(
            allShelves = allShelves,
            openBookIds = openBooks.map { it.id }.toSet(),
            onDismiss = { showAddBookDialog = false },
            onSelectBook = { selectedBook ->
                val existingIndex = openBooks.indexOfFirst { it.id == selectedBook.id }
                if (existingIndex >= 0) {
                    activeBookIndex = existingIndex
                } else {
                    openBooks = openBooks + selectedBook
                    activeBookIndex = openBooks.lastIndex
                }
                prefs.addRecentBook(selectedBook.id)
                showAddBookDialog = false
            }
        )
    }
}

@Composable
private fun ReaderContent(
    book: Book,
    pages: List<BookPage>,
    openBooks: List<Book>,
    activeBookIndex: Int,
    prefs: Prefs,
    searchQuery: String?,
    onBack: () -> Unit,
    onTabSelect: (Int) -> Unit,
    onTabClose: (Int) -> Unit,
    onAddClick: () -> Unit
) {
    val context = LocalContext.current
    var fontSp by remember { mutableFloatStateOf(prefs.fontSp) }
    var textFontKey by remember { mutableStateOf(prefs.textFontFamily) }
    var arabicFontKey by remember { mutableStateOf(prefs.arabicFontFamily) }
    var pageColorTheme by remember { mutableStateOf(prefs.pageColorTheme) }

    var isTefekkurMode by remember { mutableStateOf(prefs.tefekkurMode) }
    var isAutoScrolling by remember { mutableStateOf(false) }
    var autoScrollSpeed by remember { mutableFloatStateOf(0.75f) }

    var showGoto by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showFihristDialog by remember { mutableStateOf(false) }
    var showDictionaryDialog by remember { mutableStateOf(false) }
    var showMealDialog by remember { mutableStateOf(false) }
    var showQuoteCardDialog by remember { mutableStateOf(false) }
    var selectedFootnote by remember { mutableStateOf<Footnote?>(null) }

    var quoteTextToShare by remember { mutableStateOf("") }
    var dictionarySearchTerm by remember { mutableStateOf("") }
    var mealSearchVerse by remember { mutableStateOf("") }
    var readingSeconds by remember { mutableLongStateOf(prefs.readingTimeSeconds(book.id)) }

    val scope = rememberCoroutineScope()
    val textFont = remember(textFontKey) { getFontFamily(context, textFontKey) }
    val arabicFont = remember(arabicFontKey) { getFontFamily(context, arabicFontKey) }
    val footnotesMap = remember(book.id) { Repository.loadFootnotes(context, book) }

    // Okuma süresini sayaçla arttır
    LaunchedEffect(book.id) {
        while (true) {
            delay(1000L)
            readingSeconds += 1
            prefs.addReadingTime(book.id, 1L)
        }
    }

    // Her kitap için ayrı scroll durumu sakla
    val listStates = remember { mutableMapOf<String, LazyListState>() }
    val listState = remember(book.id) {
        listStates.getOrPut(book.id) {
            val initialIndex = if (pages.isEmpty()) 0 else prefs.position(book.id).coerceIn(0, pages.lastIndex)
            LazyListState(firstVisibleItemIndex = initialIndex)
        }
    }

    // Hedef sayfaya ve kelimenin geçtiği satıra hassas odaklanma (Scroll Focus)
    LaunchedEffect(book.id, searchQuery) {
        if (pages.isNotEmpty()) {
            val targetIdx = prefs.position(book.id).coerceIn(0, pages.lastIndex)
            val page = pages.getOrNull(targetIdx)
            var lineScrollOffsetPx = 0

            if (page != null && !searchQuery.isNullOrBlank()) {
                val lines = page.text.split("\n")
                val normQuery = Repository.normalizeSearchText(searchQuery)
                val queryWords = normQuery.split(" ").filter { it.isNotBlank() }

                val matchedLineIndex = lines.indexOfFirst { line ->
                    val normLine = Repository.normalizeSearchText(line)
                    queryWords.any { qw -> normLine.contains(qw) }
                }

                if (matchedLineIndex > 0) {
                    val density = context.resources.displayMetrics.density
                    val approxLineHeightPx = (fontSp * 1.55f * density) + (8f * density)
                    lineScrollOffsetPx = (matchedLineIndex * approxLineHeightPx).toInt()
                }
            }

            listState.scrollToItem(index = targetIdx, scrollOffset = lineScrollOffsetPx)
        }
    }

    // Otomatik Sayfa Akıtma (Auto-Scroll) Motoru
    LaunchedEffect(isAutoScrolling, autoScrollSpeed) {
        if (isAutoScrolling) {
            while (isAutoScrolling) {
                listState.scrollBy(autoScrollSpeed)
                delay(16L) // ~60 FPS akıcı dikey kaydırma
            }
        }
    }

    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }.collect { prefs.setPosition(book.id, it) }
    }

    val currentIndex by remember { derivedStateOf { listState.firstVisibleItemIndex } }
    val currentNumber = pages.getOrNull(currentIndex)?.number ?: (currentIndex + 1)
    val firstNumber = pages.firstNotNullOfOrNull { it.number } ?: 1
    val lastNumber = pages.lastOrNull { it.number != null }?.number ?: pages.size

    val progressPercent = if (pages.isNotEmpty()) {
        (((currentIndex + 1).toFloat() / pages.size) * 100).toInt().coerceIn(0, 100)
    } else 0

    val isNightMode = pageColorTheme == "gece"
    val bgColor = when (pageColorTheme) {
        "sepya" -> SepyaColor
        "yesil" -> Color(0xFFE8F2E8)
        "mavi" -> Color(0xFFEBF2F7)
        "gece" -> NightBgColor
        else -> PaperColor
    }
    val textInk = if (isNightMode) Color(0xFFE2D8CC) else TextInk
    val pageInk = if (isNightMode) Color(0xFF9E8A76) else PageInk
    var isBookmarked by remember(book.id, currentNumber) { mutableStateOf(prefs.isBookmarked(book.id, currentNumber)) }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().background(bgColor)) {
            ModernReaderTopBar(
                title = book.title,
                pageLabel = "s. $currentNumber",
                openBooks = openBooks,
                activeBookIndex = activeBookIndex,
                isNightMode = isNightMode,
                isBookmarked = isBookmarked,
                onBack = onBack,
                onTabSelect = onTabSelect,
                onTabClose = onTabClose,
                onAddClick = onAddClick,
                onFihristClick = { showFihristDialog = true },
                onBookmarkClick = {
                    isBookmarked = prefs.toggleBookmark(book.id, currentNumber)
                },
                onSmaller = {
                    fontSp = (fontSp - 1f).coerceAtLeast(14f)
                    prefs.fontSp = fontSp
                },
                onBigger = {
                    fontSp = (fontSp + 1f).coerceAtMost(34f)
                    prefs.fontSp = fontSp
                },
                onToggleNight = {
                    pageColorTheme = if (isNightMode) "gunduz" else "gece"
                    prefs.pageColorTheme = pageColorTheme
                },
                onSettingsClick = { showSettingsDialog = true },
                onPageClick = { showGoto = true }
            )

            SelectionContainer(modifier = Modifier.weight(1f).fillMaxWidth()) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = WindowInsets.navigationBars.asPaddingValues(),
                ) {
                    items(pages.size) { index ->
                        val page = pages[index]
                        val pageFootnotes = page.number?.let { footnotesMap[it] } ?: emptyList()
                        PageBlock(
                            page = page,
                            footnotes = pageFootnotes,
                            searchQuery = searchQuery,
                            fontSp = fontSp,
                            textFont = textFont,
                            arabicFont = arabicFont,
                            isTefekkur = isTefekkurMode,
                            isNightMode = isNightMode,
                            textInk = textInk,
                            pageInk = pageInk,
                            bgColor = bgColor,
                            onPageTap = { isAutoScrolling = !isAutoScrolling },
                            onWordClick = { clickedWord ->
                                dictionarySearchTerm = clickedWord
                                showDictionaryDialog = true
                            },
                            onFootnoteClick = { fn -> selectedFootnote = fn },
                            onArabicVerseClick = { verseQuery ->
                                mealSearchVerse = verseQuery
                                showMealDialog = true
                            }
                        )
                    }
                }
            }

            ModernBottomBar(
                bookTitle = book.title,
                pageNumber = currentNumber,
                progress = progressPercent,
                readingSeconds = readingSeconds,
                isNightMode = isNightMode,
                isAutoScrolling = isAutoScrolling,
                onToggleAutoScroll = { isAutoScrolling = !isAutoScrolling }
            )
        }

        // Otomatik Sayfa Akıtma Kontrol Çubuğu (0.5x, 0.75x, 1.0x, 2.0x)
        if (isAutoScrolling) {
            AutoScrollControlBar(
                speed = autoScrollSpeed,
                isScrolling = isAutoScrolling,
                onTogglePlay = { isAutoScrolling = !isAutoScrolling },
                onSpeedChange = { newSpeed -> autoScrollSpeed = newSpeed },
                onClose = { isAutoScrolling = false },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 60.dp, end = 16.dp)
            )
        }
    }

    if (showSettingsDialog) {
        ReaderSettingsDialog(
            fontSp = fontSp,
            textFontKey = textFontKey,
            arabicFontKey = arabicFontKey,
            pageColorTheme = pageColorTheme,
            isTefekkurMode = isTefekkurMode,
            onDismiss = { showSettingsDialog = false },
            onFontChange = { newSp ->
                fontSp = newSp
                prefs.fontSp = newSp
            },
            onTextFontChange = { key ->
                textFontKey = key
                prefs.textFontFamily = key
            },
            onArabicFontChange = { key ->
                arabicFontKey = key
                prefs.arabicFontFamily = key
            },
            onThemeChange = { key ->
                pageColorTheme = key
                prefs.pageColorTheme = key
            },
            onTefekkurToggle = {
                isTefekkurMode = !isTefekkurMode
                prefs.tefekkurMode = isTefekkurMode
            }
        )
    }

    if (showFihristDialog) {
        FihristDialog(
            book = book,
            currentPageNumber = currentNumber,
            onDismiss = { showFihristDialog = false },
            onSelectSection = { startPage ->
                showFihristDialog = false
                val exact = pages.indexOfFirst { it.number == startPage }
                val next = pages.indexOfFirst { (it.number ?: Int.MIN_VALUE) >= startPage }
                val target = when {
                    exact >= 0 -> exact
                    next >= 0 -> next
                    else -> pages.lastIndex
                }
                scope.launch { listState.scrollToItem(target) }
            }
        )
    }

    if (showDictionaryDialog) {
        DictionaryDialog(
            initialTerm = dictionarySearchTerm,
            onDismiss = { showDictionaryDialog = false }
        )
    }

    if (showMealDialog) {
        MealDialog(
            initialVerse = mealSearchVerse,
            onDismiss = { showMealDialog = false }
        )
    }

    if (selectedFootnote != null) {
        FootnoteDialog(
            footnote = selectedFootnote!!,
            onDismiss = { selectedFootnote = null }
        )
    }

    if (showQuoteCardDialog) {
        QuoteCardDialog(
            quoteText = quoteTextToShare,
            bookTitle = book.title,
            pageNumber = currentNumber,
            onDismiss = { showQuoteCardDialog = false }
        )
    }

    if (showGoto) {
        GotoDialog(
            first = firstNumber,
            last = lastNumber,
            onDismiss = { showGoto = false },
            onGo = { n ->
                showGoto = false
                val exact = pages.indexOfFirst { it.number == n }
                val next = pages.indexOfFirst { (it.number ?: Int.MIN_VALUE) >= n }
                val target = when {
                    exact >= 0 -> exact
                    next >= 0 -> next
                    else -> pages.lastIndex
                }
                scope.launch { listState.scrollToItem(target) }
            },
        )
    }
}

// ---------------------------------------------------------------- Sayfa Bloğu & Lügat / Arama Vurgulamalı Metin

private fun buildPageLineAnnotatedString(
    context: Context,
    cleanLine: String,
    searchQuery: String?,
    isTefekkur: Boolean,
    isNightMode: Boolean
): AnnotatedString {
    val base = Repository.applyTefekkurMode(cleanLine, isTefekkur, isNightMode)
    val builder = AnnotatedString.Builder(base)

    // 1. Lügat kelimelerini ince çizgi ile vurgula
    val wordRegex = Regex("[a-zA-ZçğıöşüÇĞİÖŞÜâîûÂÎÛ]+")
    val matches = wordRegex.findAll(cleanLine)
    val underlineColor = if (isNightMode) Color(0xFFFFCC80) else Color(0xFF7B2412)

    for (m in matches) {
        val word = m.value
        if (word.length >= 3 && DictionaryRepository.contains(context, word)) {
            val start = m.range.first
            val end = m.range.last + 1
            builder.addStyle(
                style = SpanStyle(
                    textDecoration = TextDecoration.Underline,
                    color = underlineColor,
                    fontWeight = FontWeight.Medium
                ),
                start = start,
                end = end
            )
            builder.addStringAnnotation(
                tag = "LUGAT",
                annotation = word,
                start = start,
                end = end
            )
        }
    }

    // 2. Arama yapılan kelimeleri fosforlu sarı/amber renkle vurgula
    if (!searchQuery.isNullOrBlank()) {
        val queryWords = searchQuery.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
        val normLine = Repository.normalizeSearchText(cleanLine)

        for (qw in queryWords) {
            val normQw = Repository.normalizeSearchText(qw)
            if (normQw.length >= 2) {
                var searchFrom = 0
                while (searchFrom < normLine.length) {
                    val found = normLine.indexOf(normQw, searchFrom)
                    if (found < 0) break
                    val matchEnd = (found + normQw.length).coerceAtMost(cleanLine.length)
                    builder.addStyle(
                        style = SpanStyle(
                            background = Color(0xFFFFD54F), // Fosforlu sarı/amber vurgu
                            color = Color(0xFF3E2723),
                            fontWeight = FontWeight.Bold
                        ),
                        start = found,
                        end = matchEnd
                    )
                    searchFrom = found + normQw.length
                }
            }
        }
    }

    return builder.toAnnotatedString()
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PageBlock(
    page: BookPage,
    footnotes: List<Footnote>,
    searchQuery: String?,
    fontSp: Float,
    textFont: FontFamily,
    arabicFont: FontFamily,
    isTefekkur: Boolean,
    isNightMode: Boolean,
    textInk: Color,
    pageInk: Color,
    bgColor: Color,
    onPageTap: () -> Unit,
    onWordClick: (String) -> Unit,
    onFootnoteClick: (Footnote) -> Unit,
    onArabicVerseClick: (String) -> Unit
) {
    val context = LocalContext.current
    val titleFont = remember { getFontFamily(context, "fontrln") }
    val spineLeft = (page.number ?: 0) % 2 == 1
    val lines = remember(page.text) { page.text.split("\n") }

    Column(
        Modifier
            .fillMaxWidth()
            .background(bgColor)
            .drawBehind {
                val strong = if (isNightMode) Color(0x22000000) else Color(0x125A3A10)
                val weak = if (isNightMode) Color(0x0A000000) else Color(0x065A3A10)
                val w = 10.dp.toPx()
                drawRect(
                    Brush.horizontalGradient(listOf(if (spineLeft) strong else weak, Color.Transparent), startX = 0f, endX = w),
                    size = Size(w, size.height),
                )
                drawRect(
                    Brush.horizontalGradient(
                        listOf(Color.Transparent, if (spineLeft) weak else strong),
                        startX = size.width - w,
                        endX = size.width,
                    ),
                    topLeft = Offset(size.width - w, 0f),
                    size = Size(w, size.height),
                )
            }
            .clickable { onPageTap() }
            .padding(horizontal = 8.dp, vertical = 8.dp),
    ) {
        lines.forEach { line ->
            val trimmed = line.trim()
            if (trimmed.isNotBlank()) {
                val isHeading = trimmed.startsWith("&")
                val mealIdMatch = Regex("\\|(\\d+)@").find(trimmed)
                val mealId = mealIdMatch?.groupValues?.get(1)

                val cleanLine = trimmed
                    .removePrefix("&")
                    .removePrefix("~")
                    .replace(Regex("\\|\\d+@"), "")
                    .trim()

                val isArabicLine = trimmed.startsWith("~") || cleanLine.count { it in '\u0600'..'\u06FF' } > 3

                if (isHeading) {
                    Text(
                        text = cleanLine,
                        color = if (isNightMode) Color(0xFFFFCC80) else Color(0xFF5A1808),
                        fontFamily = titleFont,
                        fontSize = (fontSp * 1.55f).sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = (fontSp * 2.2f).sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp)
                    )
                } else if (isArabicLine) {
                    val targetMealQuery = mealId ?: cleanLine
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = cleanLine,
                            color = if (isNightMode) Color(0xFFEF5350) else Color(0xFFC62828),
                            fontFamily = arabicFont,
                            fontSize = (fontSp * 1.4f).sp,
                            lineHeight = (fontSp * 2.2f).sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onArabicVerseClick(targetMealQuery) }
                        )
                        IconButton(
                            onClick = { onArabicVerseClick(targetMealQuery) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                Icons.Default.AutoStories,
                                contentDescription = "Meâl Göster",
                                tint = if (isNightMode) Color(0xFFFFB74D) else Color(0xFF7B2412),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                } else {
                    val annotatedLine = remember(cleanLine, searchQuery, isTefekkur, isNightMode) {
                        buildPageLineAnnotatedString(context, cleanLine, searchQuery, isTefekkur, isNightMode)
                    }

                    ClickableText(
                        text = annotatedLine,
                        style = TextStyle(
                            color = textInk,
                            fontFamily = textFont,
                            fontSize = fontSp.sp,
                            lineHeight = (fontSp * 1.55f).sp,
                            textAlign = TextAlign.Justify
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        onClick = { offset ->
                            val annotation = annotatedLine.getStringAnnotations("LUGAT", offset, offset).firstOrNull()
                            if (annotation != null) {
                                onWordClick(annotation.item)
                            } else {
                                onPageTap()
                            }
                        }
                    )
                }
            }
        }

        // Hâşiye (Footnote) Rozetleri
        if (footnotes.isNotEmpty()) {
            Spacer(Modifier.height(6.dp))
            Box(Modifier.fillMaxWidth().height(1.dp).background(if (isNightMode) Color(0x33FFFFFF) else Color(0x336B4A2B)))
            Spacer(Modifier.height(4.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                footnotes.forEach { fn ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isNightMode) Color(0xFF3E2723) else Color(0xFFFBE9E7),
                        shadowElevation = 2.dp,
                        modifier = Modifier.clickable { onFootnoteClick(fn) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "[${fn.symbol}]",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isNightMode) Color(0xFFFF8A65) else Color(0xFFD84315)
                            )
                        }
                    }
                }
            }
        }

        if (page.number != null) {
            Text(
                text = "— ${page.number} —",
                color = pageInk,
                fontFamily = textFont,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 6.dp),
            )
        }
        PageDivider(isNightMode)
    }
}

@Composable
private fun PageDivider(isNightMode: Boolean) {
    Column(Modifier.fillMaxWidth().padding(top = 4.dp)) {
        Box(Modifier.fillMaxWidth().height(1.dp).background(if (isNightMode) Color(0x33FFFFFF) else Color(0x336B4A2B)))
        Box(
            Modifier
                .fillMaxWidth()
                .height(6.dp)
                .background(Brush.verticalGradient(listOf(Color(0x1A000000), Color.Transparent))),
        )
    }
}

// ---------------------------------------------------------------- Üst ve Alt Barlar

@Composable
private fun ModernReaderTopBar(
    title: String,
    pageLabel: String,
    openBooks: List<Book>,
    activeBookIndex: Int,
    isNightMode: Boolean,
    isBookmarked: Boolean,
    onBack: () -> Unit,
    onTabSelect: (Int) -> Unit,
    onTabClose: (Int) -> Unit,
    onAddClick: () -> Unit,
    onFihristClick: () -> Unit,
    onBookmarkClick: () -> Unit,
    onSmaller: () -> Unit,
    onBigger: () -> Unit,
    onToggleNight: () -> Unit,
    onSettingsClick: () -> Unit,
    onPageClick: () -> Unit,
) {
    val topBarBg = if (isNightMode) Color(0xFF2B0E0A) else TopBarDark
    val subBarBg = if (isNightMode) Color(0xFF262626) else Color(0xFFEBE5DF)
    val infoBarBg = if (isNightMode) Color(0xFF1F1F1F) else Color(0xFFFAF7F2)
    val textColor = if (isNightMode) Color(0xFFE0E0E0) else Color.DarkGray

    Column(
        Modifier
            .fillMaxWidth()
            .statusBarsPadding()
    ) {
        // Risale-i Nur Asil Bordo Kırmızı Ana Çubuk
        Row(
            Modifier
                .fillMaxWidth()
                .background(topBarBg)
                .height(48.dp)
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri", tint = Color.White)
            }
            Text(
                text = "Risale-i Nur",
                color = Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        // Sekmeler Satırı (Multi-Tab Çalışma Ortamı)
        LazyRow(
            Modifier
                .fillMaxWidth()
                .background(subBarBg)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            itemsIndexed(openBooks) { index, openBook ->
                val isActive = index == activeBookIndex
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (isActive) {
                        if (isNightMode) Color(0xFF5A1808) else Color(0xFF7B2412)
                    } else {
                        if (isNightMode) Color(0xFF333333) else Color(0xFFC0B5A8)
                    },
                    modifier = Modifier.clickable { onTabSelect(index) }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = openBook.title,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
                        )
                        Spacer(Modifier.width(6.dp))
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Kapat",
                            tint = Color.White,
                            modifier = Modifier
                                .size(14.dp)
                                .clickable { onTabClose(index) }
                        )
                    }
                }
            }

            item {
                // + Butonu (Kitap Ekle)
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .background(if (isNightMode) Color(0xFF5A1808) else Color(0xFF7B2412), CircleShape)
                        .clickable { onAddClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Çalışma Ortamına Kitap Ekle",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // Sayfa Bilgisi & İkonlar
        Row(
            Modifier
                .fillMaxWidth()
                .background(infoBarBg)
                .padding(horizontal = 8.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$title / $pageLabel",
                color = textColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable { onPageClick() }
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Fihrist İkonu
                IconButton(onClick = onFihristClick, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = "Fihrist", tint = textColor)
                }
                // Yer İmi İkonu
                IconButton(onClick = onBookmarkClick, modifier = Modifier.size(28.dp)) {
                    Icon(
                        if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Yer İmi",
                        tint = if (isBookmarked) Color(0xFFD32F2F) else textColor
                    )
                }
                TextButton(onClick = onSmaller, contentPadding = PaddingValues(horizontal = 2.dp)) {
                    Text("A-", color = textColor, fontSize = 13.sp)
                }
                TextButton(onClick = onBigger, contentPadding = PaddingValues(horizontal = 2.dp)) {
                    Text("A+", color = textColor, fontSize = 16.sp)
                }
                IconButton(onClick = onToggleNight, modifier = Modifier.size(28.dp)) {
                    Icon(
                        if (isNightMode) Icons.Default.WbSunny else Icons.Default.Nightlight,
                        contentDescription = "Gece/Gündüz Modu",
                        tint = if (isNightMode) Color(0xFFFFB74D) else Color(0xFF455A64)
                    )
                }
                IconButton(onClick = onSettingsClick, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Settings, contentDescription = "Ayarlar", tint = textColor)
                }
            }
        }
    }
}

@Composable
private fun ModernBottomBar(
    bookTitle: String,
    pageNumber: Int,
    progress: Int,
    readingSeconds: Long,
    isNightMode: Boolean,
    isAutoScrolling: Boolean,
    onToggleAutoScroll: () -> Unit
) {
    val barBg = if (isNightMode) Color(0xFF222222) else Color(0xFFECE7E1)
    val mainText = if (isNightMode) Color(0xFFE0E0E0) else Color.DarkGray
    val subText = if (isNightMode) Color(0xFF888888) else Color.Gray

    val minutes = (readingSeconds / 60).toInt()

    Surface(
        color = barBg,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("SÜRE: $minutes DK", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = mainText)
                Text("İLERLEME", fontSize = 9.sp, color = subText)
            }
            Text(
                text = "$progress%",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = mainText
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Otomatik Sayfa Akıtma Butonu
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isAutoScrolling) Color(0xFF7B2412) else Color(0xFFB58E6E),
                    modifier = Modifier.clickable { onToggleAutoScroll() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (isAutoScrolling) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(if (isAutoScrolling) "Durdur" else "Oto-Akıt", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.width(8.dp))
                Column(horizontalAlignment = Alignment.End) {
                    Text("KONUM", fontSize = 9.sp, color = subText)
                    Text("$bookTitle / S. $pageNumber", fontSize = 9.sp, fontWeight = FontWeight.Medium, color = mainText)
                }
            }
        }
    }
}

// ---------------------------------------------------------------- Otomatik Sayfa Akıtma Kontrol Çubuğu

@Composable
private fun AutoScrollControlBar(
    speed: Float,
    isScrolling: Boolean,
    onTogglePlay: () -> Unit,
    onSpeedChange: (Float) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color(0xF02B1B0E),
        shadowElevation = 8.dp,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(onClick = onTogglePlay, modifier = Modifier.size(32.dp)) {
                Icon(if (isScrolling) Icons.Default.Pause else Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
            }
            listOf(0.5f to "0.5x", 0.75f to "0.75x", 1.0f to "1.0x", 2.0f to "2.0x").forEach { (spVal, label) ->
                val selected = speed == spVal
                Box(
                    modifier = Modifier
                        .background(if (selected) Color(0xFF7B2412) else Color(0x33FFFFFF), CircleShape)
                        .clickable { onSpeedChange(spVal) }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(label, fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
            IconButton(onClick = onClose, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Close, contentDescription = null, tint = Color.Gray)
            }
        }
    }
}

// ---------------------------------------------------------------- Çark / Okuma Ayarları Diyaloğu (Görsellerle Birebir Aynı 5 Renk Seçeneği)

@Composable
private fun ReaderSettingsDialog(
    fontSp: Float,
    textFontKey: String,
    arabicFontKey: String,
    pageColorTheme: String,
    isTefekkurMode: Boolean,
    onDismiss: () -> Unit,
    onFontChange: (Float) -> Unit,
    onTextFontChange: (String) -> Unit,
    onArabicFontChange: (String) -> Unit,
    onThemeChange: (String) -> Unit,
    onTefekkurToggle: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Okuma Ayarları", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                // Sayfa Arka Plan Renk Temaları (Görsel 1 ile Birebir Aynı Kart Tasarımı)
                Text("Varsayılan Sayfa Arka Plan Rengi", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7B2412))
                Spacer(Modifier.height(6.dp))
                val pageColors = listOf(
                    "gunduz" to ("Gündüz (Krem)" to Color(0xFFF7F2EB)),
                    "sepya" to ("Sepya" to Color(0xFFF4ECD8)),
                    "yesil" to ("Soft Yeşil" to Color(0xFFE8F2E8)),
                    "mavi" to ("Soft Mavi" to Color(0xFFEBF2F7)),
                    "gece" to ("Gece Modu" to Color(0xFF1B1B1B))
                )
                pageColors.forEach { (key, info) ->
                    val (name, colorVal) = info
                    val isSel = pageColorTheme == key
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = colorVal,
                        shadowElevation = 1.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                            .border(1.5.dp, if (isSel) Color(0xFF7B2412) else Color.Transparent, RoundedCornerShape(8.dp))
                            .clickable { onThemeChange(key) }
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

                Spacer(Modifier.height(10.dp))

                // Yazı Boyutu Kontrolü
                Text("Yazı Boyutu (${fontSp.toInt()} sp)", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = { onFontChange((fontSp - 1f).coerceAtLeast(14f)) }) {
                        Text("A- Küçült", fontSize = 14.sp)
                    }
                    TextButton(onClick = { onFontChange((fontSp + 1f).coerceAtMost(34f)) }) {
                        Text("A+ Büyüt", fontSize = 16.sp)
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Türkçe Yazı Tipi Seçimi
                Text("Türkçe Yazı Tipi", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val fontOptions = listOf("lora" to "Lora (Kitap)", "inter" to "Inter (Modern)", "serif" to "Klasik")
                    fontOptions.forEach { (key, label) ->
                        val selected = textFontKey == key
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (selected) Color(0xFF7B2412) else Color(0xFFEEEEEE),
                            modifier = Modifier.clickable { onTextFontChange(key) }
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                color = if (selected) Color.White else Color.Black,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Arapça Yazı Tipi Seçimi
                Text("Arapça Yazı Tipi", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val arabicOptions = listOf("arabic_uthmantaha" to "Uthman Taha", "arabic_amiri" to "Amiri", "arabic_naskh" to "Naskh")
                    arabicOptions.forEach { (key, label) ->
                        val selected = arabicFontKey == key
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (selected) Color(0xFF7B2412) else Color(0xFFEEEEEE),
                            modifier = Modifier.clickable { onArabicFontChange(key) }
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                color = if (selected) Color.White else Color.Black,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Tefekkür Modu Switch
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Tefekkür Modu", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Text("Cümleleri pastel renklerle vurgular", fontSize = 11.sp, color = Color.Gray)
                    }
                    Switch(checked = isTefekkurMode, onCheckedChange = { onTefekkurToggle() })
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Tamam") }
        }
    )
}

// ---------------------------------------------------------------- Dinamik Fihrist / Ağaç Yapısı Diyaloğu

@Composable
private fun FihristDialog(
    book: Book,
    currentPageNumber: Int,
    onDismiss: () -> Unit,
    onSelectSection: (Int) -> Unit
) {
    val context = LocalContext.current
    val sections = remember(book.id) { Repository.loadSections(context, book) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("${book.title} - Fihrist", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        text = {
            if (sections.isEmpty()) {
                Text("Bu kitap için fihrist bulunamadı.", fontSize = 13.sp, color = Color.Gray)
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(360.dp)
                ) {
                    items(sections, key = { it.id }) { sec ->
                        val isCurrent = currentPageNumber >= sec.startPage
                        val indentDp = (sec.level * 16).dp
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectSection(sec.startPage) }
                                .padding(start = indentDp + 4.dp, top = 8.dp, bottom = 8.dp, end = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = sec.title,
                                fontSize = (13 - (sec.level * 0.5f)).coerceAtLeast(11f).sp,
                                fontWeight = if (sec.level == 0) FontWeight.Bold else FontWeight.Normal,
                                color = if (isCurrent) Color(0xFF7B2412) else Color.Unspecified,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "s. ${sec.startPage}",
                                fontSize = 11.sp,
                                color = if (isCurrent) Color(0xFF7B2412) else Color.Gray,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Box(Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFEEEEEE)))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Kapat") }
        }
    )
}

// ---------------------------------------------------------------- Risale Lügat Diyaloğu

@Composable
private fun DictionaryDialog(
    initialTerm: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var term by remember { mutableStateOf(initialTerm) }
    val result = remember(term) { if (term.isNotBlank()) DictionaryRepository.lookup(context, term, isInteractiveSearch = true) else null }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Translate, contentDescription = null, tint = Color(0xFF7B2412))
                Spacer(Modifier.width(8.dp))
                Text("Risale Lügati", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = term,
                    onValueChange = { term = it },
                    label = { Text("Kelime Ara (ör. tefekkür, bürhan, hâşiye...)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                if (term.isNotBlank()) {
                    if (result != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFFF8E7),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(result.word, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF7B2412))
                                Spacer(Modifier.height(4.dp))
                                Text(result.def, fontSize = 13.sp, color = Color.DarkGray)
                            }
                        }
                    } else {
                        Text("Kelime bulunamadı.", fontSize = 13.sp, color = Color.Gray)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Kapat") }
        }
    )
}

// ---------------------------------------------------------------- Meâl (Ayet / Hadîs Çevirisi) Diyaloğu

@Composable
private fun MealDialog(
    initialVerse: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var query by remember { mutableStateOf(initialVerse) }
    val result = remember(query) { if (query.isNotBlank()) MealRepository.lookup(context, query) else null }
    val allMeals = remember { MealRepository.getAllMeals(context) }

    val parts = remember(result) {
        if (result != null) MealRepository.parseMealParts(result) else emptyList()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AutoStories, contentDescription = null, tint = Color(0xFF7B2412))
                Spacer(Modifier.width(8.dp))
                Text("Ayet & Hadîs Meâli", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("Ayet / Hadîs No veya Kelime Ara...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                if (parts.isNotEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        parts.forEachIndexed { index, part ->
                            val isHadis = part.type == MealType.HADIS
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFFFF8E7),
                                shadowElevation = 1.dp,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (parts.size > 1) "Meâl (${index + 1}. Kısım):" else "Meâl:",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = Color(0xFF7B2412)
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = if (isHadis) Color(0xFFE65100) else Color(0xFF2E7D32)
                                        ) {
                                            Text(
                                                text = part.type.label,
                                                color = Color.White,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                    }
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        text = part.text,
                                        fontSize = 13.sp,
                                        color = Color.DarkGray,
                                        lineHeight = 19.sp
                                    )
                                }
                            }
                        }
                    }
                } else if (query.isNotBlank()) {
                    Text("Uygun meâl bulunamadı.", fontSize = 13.sp, color = Color.Gray)
                } else {
                    Text("Toplam ${allMeals.size} meâl kaydı yüklendi. Arapça ayet/hadîs metinlerine tıklayarak meâlini görebilirsiniz.", fontSize = 12.sp, color = Color.Gray)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Kapat") }
        }
    )
}

// ---------------------------------------------------------------- Hâşiye (Dipnot) Detay Diyaloğu

@Composable
private fun FootnoteDialog(
    footnote: Footnote,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xFF7B2412)
                ) {
                    Text(
                        text = footnote.symbol,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text("Hâşiye (s. ${footnote.pageNumber})", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFFFF8E7),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = footnote.text,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Serif,
                        lineHeight = 20.sp,
                        color = Color(0xFF2B1B0E)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Tamam") }
        }
    )
}

// ---------------------------------------------------------------- Alıntı / Görsel Kart Oluşturucu

@Composable
private fun QuoteCardDialog(
    quoteText: String,
    bookTitle: String,
    pageNumber: Int,
    onDismiss: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    var copied by remember { mutableStateOf(false) }

    val fullCitation = "“$quoteText”\n\n— $bookTitle, Sayfa $pageNumber"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.FormatQuote, contentDescription = null, tint = Color(0xFF7B2412))
                Spacer(Modifier.width(8.dp))
                Text("Alıntı Kartı", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFAF2E6),
                    shadowElevation = 4.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFD4B896), RoundedCornerShape(12.dp))
                        .padding(2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.FormatQuote, contentDescription = null, tint = Color(0xFF7B2412), modifier = Modifier.size(24.dp))
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "“$quoteText”",
                            fontSize = 14.sp,
                            fontFamily = FontFamily.Serif,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            textAlign = TextAlign.Center,
                            color = Color(0xFF2B1B0E)
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = "— $bookTitle / S. $pageNumber",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF7B2412)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    clipboardManager.setText(AnnotatedString(fullCitation))
                    copied = true
                }
            ) {
                Text(if (copied) "Kopyalandı!" else "Metni Kopyala")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Kapat") }
        }
    )
}

// ---------------------------------------------------------------- Çalışma Ortamına Kitap Ekle Diyaloğu

@Composable
private fun AddBookDialog(
    allShelves: List<Shelf>,
    openBookIds: Set<String>,
    onDismiss: () -> Unit,
    onSelectBook: (Book) -> Unit
) {
    val allBooks = remember(allShelves) {
        allShelves.flatMap { it.books }.distinctBy { it.id }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Çalışma Ortamına Kitap Ekle", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
            ) {
                items(allBooks, key = { it.id }) { book ->
                    val isOpen = openBookIds.contains(book.id)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectBook(book) }
                            .padding(vertical = 10.dp, horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = book.title,
                            fontSize = 14.sp,
                            fontWeight = if (isOpen) FontWeight.Bold else FontWeight.Normal,
                            color = if (isOpen) Color(0xFF7B2412) else Color.Unspecified
                        )
                        if (isOpen) {
                            Text("Açık", fontSize = 11.sp, color = Color.Gray)
                        } else {
                            Text("Ekle", fontSize = 12.sp, color = Color(0xFF7B2412), fontWeight = FontWeight.SemiBold)
                        }
                    }
                    Box(Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFEEEEEE)))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Kapat") }
        }
    )
}

// ---------------------------------------------------------------- Metin Seçim & Kalem Menüsü

@Composable
private fun SelectionMenu(onDismiss: () -> Unit) {
    val highlightColors = listOf(
        Color(0xFFE53935), Color(0xFFFDD835), Color(0xFF00ACC1),
        Color(0xFFFB8C00), Color(0xFF43A047), Color(0xFF1E88E5), Color(0xFFE91E63)
    )

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xF0303030),
        shadowElevation = 8.dp,
        modifier = Modifier.padding(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Close, contentDescription = null, tint = Color(0xFFEF5350))
                }
                IconButton(onClick = { }, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.FormatBold, contentDescription = null, tint = Color.White)
                }
                IconButton(onClick = { }, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.FormatItalic, contentDescription = null, tint = Color.White)
                }
                IconButton(onClick = { }, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.FormatStrikethrough, contentDescription = null, tint = Color.White)
                }
            }

            Row(
                modifier = Modifier.padding(vertical = 5.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                highlightColors.forEach { color ->
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .background(color, CircleShape)
                            .border(1.dp, Color.White, CircleShape)
                            .clickable { onDismiss() }
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { }, modifier = Modifier.size(26.dp)) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color.White)
                }
                IconButton(onClick = { }, modifier = Modifier.size(26.dp)) {
                    Icon(Icons.Default.Search, contentDescription = null, tint = Color.White)
                }
                IconButton(onClick = { }, modifier = Modifier.size(26.dp)) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = Color.White)
                }
                IconButton(onClick = { }, modifier = Modifier.size(26.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White)
                }
                IconButton(onClick = { }, modifier = Modifier.size(26.dp)) {
                    Icon(Icons.Default.Bookmark, contentDescription = null, tint = Color.White)
                }
            }
        }
    }
}

@Composable
private fun GotoDialog(first: Int, last: Int, onDismiss: () -> Unit, onGo: (Int) -> Unit) {
    var input by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Sayfaya Git") },
        text = {
            Column {
                Text("Sayfa numarası giriniz ($first - $last):", fontSize = 13.sp, color = Color.Gray)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it.filter { c -> c.isDigit() }.take(6) },
                    label = { Text("Sayfa No") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val n = input.toIntOrNull()
                if (n != null) onGo(n.coerceIn(first, last)) else onDismiss()
            }) { Text("Git") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("İptal") } },
    )
}