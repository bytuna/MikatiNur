package com.example.mkat_nur.ui.risale

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.util.lerp
import kotlin.math.absoluteValue
import com.example.mkat_nur.R
import com.example.mkat_nur.model.RisaleBook
import com.example.mkat_nur.model.RisalePage
import com.example.mkat_nur.util.RisaleTextEngine
import com.example.mkat_nur.viewmodel.RisaleUiState
import com.example.mkat_nur.viewmodel.RisaleViewModel
import kotlinx.coroutines.launch

@Deprecated("Yeni kütüphane yapısına (LibraryIndexScreen) geçildi.")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RisaleScreen(
    viewModel: RisaleViewModel = viewModel(),
    onMenuClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedBook by viewModel.selectedBook.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val currentPageData by viewModel.currentPageData.collectAsState()
    
    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    var showBookmarks by remember { mutableStateOf(false) }

    val bgColors = if (selectedBook == null) {
        listOf(Color(0xFF0D1B2A), Color(0xFF1B263B))
    } else {
        when (themeMode) {
            "Dark" -> listOf(Color(0xFF121212), Color(0xFF1E1E1E))
            "Sepia" -> listOf(Color(0xFFFCF5E5), Color(0xFFFCF5E5))
            "Original" -> listOf(Color(0xFFFFF9C4), Color(0xFFFFF9C4))
            else -> listOf(Color.White, Color.White)
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(bgColors))) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                if (selectedBook == null) {
                    Column {
                        CenterAlignedTopAppBar(
                            title = {
                                Text(
                                    text = if (isSearching) "ARAMA" else "RİSALE-İ NUR",
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFFFFD700)
                                )
                            },
                            navigationIcon = {
                                IconButton(onClick = {
                                    if (isSearching) isSearching = false
                                    else onMenuClick()
                                }) {
                                    Icon(
                                        imageVector = if (!isSearching) Icons.Default.Menu else Icons.Default.ArrowBack,
                                        contentDescription = null,
                                        tint = Color.White
                                    )
                                }
                            },
                            actions = {
                                if (!isSearching) {
                                    IconButton(onClick = { showBookmarks = true }) {
                                        Icon(Icons.Default.Bookmarks, null, tint = Color.White)
                                    }
                                    IconButton(onClick = { isSearching = true }) {
                                        Icon(Icons.Default.Search, null, tint = Color.White)
                                    }
                                }
                            },
                            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                        )
                        
                        if (!isSearching) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                                placeholder = { Text("Kitap ara...", color = Color.White.copy(alpha = 0.5f)) },
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
                if (isSearching) {
                    RisaleSearchScreen(viewModel) { page ->
                        isSearching = false
                        viewModel.selectBook(viewModel.uiState.value.let { if(it is RisaleUiState.Success) it.books.find { b -> b.id == page.bookId } else null })
                        viewModel.loadPage(page.bookId, page.pageNumber)
                    }
                } else if (selectedBook == null) {
                    when (val state = uiState) {
                        is RisaleUiState.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator(color = Color(0xFFFFD700)) }
                        is RisaleUiState.Success -> {
                            val filteredBooks = state.books.filter { it.name.contains(searchQuery, ignoreCase = true) }
                            RisaleBookList(filteredBooks) { viewModel.selectBook(it) }
                        }
                        is RisaleUiState.Error -> Box(Modifier.fillMaxSize(), Alignment.Center) { Text(state.message, color = Color.White) }
                    }
                } else {
                    RisaleReader(selectedBook!!, viewModel)
                }
            }
        }
    }

    if (showBookmarks) {
        BookmarkSheet(viewModel) { page ->
            showBookmarks = false
            viewModel.selectBook(viewModel.uiState.value.let { if(it is RisaleUiState.Success) it.books.find { b -> b.id == page.bookId } else null })
            viewModel.loadPage(page.bookId, page.pageNumber)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarkSheet(viewModel: RisaleViewModel, onPageClick: (RisalePage) -> Unit) {
    val bookmarks by viewModel.bookmarks.collectAsState()
    ModalBottomSheet(onDismissRequest = { /* dismiss handled by state in parent */ }) {
        Column(Modifier.padding(16.dp).fillMaxHeight(0.6f)) {
            Text("İşaretli Sayfalar", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Spacer(Modifier.height(16.dp))
            if (bookmarks.isEmpty()) {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Text("Henüz işaretli sayfa yok.", color = Color.Gray)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(bookmarks) { page ->
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable { onPageClick(page) },
                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(0.05f))
                        ) {
                            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Bookmark, null, tint = Color(0xFFFFD700))
                                Spacer(Modifier.width(16.dp))
                                Column {
                                    Text("${page.bookId.uppercase()} - Sayfa ${page.pageNumber}", fontWeight = FontWeight.Bold)
                                    Text(page.content.take(60) + "...", fontSize = 12.sp, maxLines = 1)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RisaleBookList(books: List<RisaleBook>, onBookClick: (RisaleBook) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(books) { book ->
            RisaleBookItem(book, onBookClick)
        }
    }
}

@Composable
fun RisaleBookItem(book: RisaleBook, onClick: (RisaleBook) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .clickable { onClick(book) },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (book.coverImageRes != null) {
                Image(
                    painter = painterResource(id = book.coverImageRes),
                    contentDescription = book.name,
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(modifier = Modifier.fillMaxSize().background(Color(book.coverColor)))
            }
            Text(
                text = book.name,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Center).padding(16.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun LugatPopup(
    word: String,
    meaningData: String,
    onDismiss: () -> Unit,
    arabicFont: FontFamily,
    serifFont: FontFamily
) {
    val parts = meaningData.split("|")
    val arabic = if (parts.size > 1) parts[0] else ""
    val meaning = if (parts.size > 1) parts[1] else parts[0]

    Popup(
        alignment = Alignment.TopCenter,
        onDismissRequest = onDismiss
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(top = 100.dp)
                .shadow(16.dp, RoundedCornerShape(8.dp)),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, Color.LightGray)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                val annotatedHeader = buildAnnotatedString {
                    withStyle(SpanStyle(color = Color(0xFFB71C1C), fontWeight = FontWeight.Black, fontSize = 18.sp)) {
                        append(word.uppercase())
                    }
                    if (arabic.isNotEmpty()) {
                        append(" ")
                        withStyle(SpanStyle(color = Color(0xFFB71C1C), fontFamily = arabicFont, fontSize = 22.sp)) {
                            append(arabic)
                        }
                    }
                    withStyle(SpanStyle(color = Color.Black, fontWeight = FontWeight.Normal, fontSize = 16.sp)) {
                        append(" ")
                        append(meaning)
                    }
                }
                Text(text = annotatedHeader, lineHeight = 24.sp)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RisaleReader(book: RisaleBook, viewModel: RisaleViewModel) {
    val pagerState = rememberPagerState(pageCount = { book.pageCount })
    val scope = rememberCoroutineScope()
    val pageData by viewModel.currentPageData.collectAsState()
    val fontSize by viewModel.fontSize.collectAsState()
    val lineSpacing by viewModel.lineSpacing.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val selectedVerse by viewModel.selectedVerse.collectAsState()
    val notes by viewModel.notes.collectAsState()
    
    var showSettings by remember { mutableStateOf(false) }
    var showNotes by remember { mutableStateOf(false) }
    var showAddNote by remember { mutableStateOf(false) }
    var showFihrist by remember { mutableStateOf(false) }
    var showGoToPage by remember { mutableStateOf(false) }
    var newNoteText by remember { mutableStateOf("") }
    var goToPageText by remember { mutableStateOf("") }
    var selectedWordMeaning by remember { mutableStateOf<String?>(null) }
    var rawSelectedWord by remember { mutableStateOf("") }
    var activeFootnoteContent by remember { mutableStateOf<String?>(null) }
    
    val arabicFont = FontFamily(Font(R.font.uthman_taha))
    val serifFont = FontFamily.Serif

    val textColor = if (themeMode == "Dark") Color.White else Color.Black
    val paperColor = when (themeMode) {
        "Dark" -> Color(0xFF1E1E1E)
        "Sepia" -> Color(0xFFFCF5E5)
        "Original" -> Color(0xFFFFF9C4)
        else -> Color.White
    }

    LaunchedEffect(pagerState.currentPage) {
        // Pager index (0-tabanlı) + Kitabın ilk sayfası = Gerçek sayfa numarası
        val actualPage = pagerState.currentPage + book.firstPage
        viewModel.loadPage(book.id, actualPage)
    }

    // Scroll to page when loadPage is called from outside the pager (like search or bookmark)
    val currentPage by viewModel.currentPage.collectAsState()
    LaunchedEffect(currentPage) {
        val targetPagerPage = currentPage - book.firstPage
        if (pagerState.currentPage != targetPagerPage && targetPagerPage >= 0) {
            pagerState.scrollToPage(targetPagerPage)
        }
    }

    if (showFihrist) {
        ModalBottomSheet(onDismissRequest = { showFihrist = false }) {
            Column(Modifier.padding(16.dp).fillMaxHeight(0.6f)) {
                Text("Fihrist - ${book.name}", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Spacer(Modifier.height(16.dp))
                if (book.sections.isEmpty()) {
                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                        Text("Bu kitap için fihrist henüz eklenmemiş.", color = Color.Gray)
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(book.sections) { section ->
                            Card(
                                modifier = Modifier.fillMaxWidth().clickable {
                                    viewModel.goToPage(section.pageNumber)
                                    showFihrist = false
                                },
                                colors = CardDefaults.cardColors(containerColor = Color.White.copy(0.05f))
                            ) {
                                Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(section.title, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                                    Text("S. ${section.pageNumber}", color = Color.Gray)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showGoToPage) {
        AlertDialog(
            onDismissRequest = { showGoToPage = false },
            title = { Text("Sayfaya Git") },
            text = {
                OutlinedTextField(
                    value = goToPageText,
                    onValueChange = { if (it.all { char -> char.isDigit() }) goToPageText = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Sayfa numarası (1-${book.pageCount})") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val page = goToPageText.toIntOrNull()
                    if (page != null) {
                        viewModel.goToPage(page)
                        showGoToPage = false
                        goToPageText = ""
                    }
                }) { Text("GİT") }
            },
            dismissButton = { TextButton(onClick = { showGoToPage = false }) { Text("İPTAL") } }
        )
    }

    if (selectedWordMeaning != null && selectedWordMeaning != "VERSE_MODE") {
        LugatPopup(
            word = rawSelectedWord,
            meaningData = selectedWordMeaning!!,
            onDismiss = { selectedWordMeaning = null },
            arabicFont = arabicFont,
            serifFont = serifFont
        )
    }

    if (selectedVerse != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearSelectedVerse() },
            title = { Text("Ayet - ${selectedVerse!!.verseKey}", color = Color(0xFFB71C1C)) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(selectedVerse!!.arabicText ?: "", textAlign = TextAlign.Right, fontSize = 22.sp, fontFamily = arabicFont, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    Text(selectedVerse!!.translation ?: "", fontSize = 16.sp)
                }
            },
            confirmButton = { TextButton(onClick = { viewModel.clearSelectedVerse() }) { Text("TAMAM") } }
        )
    }

    if (showSettings) {
        ModalBottomSheet(onDismissRequest = { showSettings = false }) {
            Column(Modifier.padding(24.dp)) {
                Text("Okuma Ayarları", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Spacer(Modifier.height(24.dp))
                
                Text("Yazı Boyutu: ${fontSize.toInt()}")
                Slider(value = fontSize, onValueChange = { viewModel.setFontSize(it) }, valueRange = 14f..36f)
                
                Text("Satır Aralığı: ${String.format("%.1f", lineSpacing)}")
                Slider(value = lineSpacing, onValueChange = { viewModel.setLineSpacing(it) }, valueRange = 1.0f..2.5f)

                Text("Arka Plan")
                Row(Modifier.padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    val themes = listOf("White" to Color.White, "Sepia" to Color(0xFFFCF5E5), "Original" to Color(0xFFFFF9C4), "Dark" to Color(0xFF1E1E1E))
                    themes.forEach { (name, color) ->
                        Box(
                            Modifier.size(40.dp).background(color, RoundedCornerShape(20.dp))
                                .border(2.dp, if(themeMode == name) Color(0xFFB71C1C) else Color.LightGray, RoundedCornerShape(20.dp))
                                .clickable { viewModel.setTheme(name) }
                        )
                    }
                }
                Spacer(Modifier.height(40.dp))
            }
        }
    }

    if (showNotes) {
        ModalBottomSheet(onDismissRequest = { showNotes = false }) {
            Column(Modifier.padding(16.dp).fillMaxHeight(0.6f)) {
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Text("Sayfa Notları", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    IconButton(onClick = { showAddNote = true }) { Icon(Icons.Default.Add, null) }
                }
                Spacer(Modifier.height(16.dp))
                if (notes.isEmpty()) {
                    Box(Modifier.fillMaxSize(), Alignment.Center) { Text("Henüz not eklenmemiş.", color = Color.Gray) }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(notes) { note ->
                            Card(colors = CardDefaults.cardColors(containerColor = Color.White.copy(0.05f))) {
                                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text(note, Modifier.weight(1f))
                                    IconButton(onClick = { viewModel.deleteNote(note) }) { Icon(Icons.Default.Delete, null, tint = Color.Gray) }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddNote) {
        AlertDialog(
            onDismissRequest = { showAddNote = false },
            title = { Text("Not Ekle") },
            text = {
                OutlinedTextField(
                    value = newNoteText,
                    onValueChange = { newNoteText = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Notunuzu yazın...") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newNoteText.isNotBlank()) {
                        viewModel.addNote(newNoteText)
                        newNoteText = ""
                        showAddNote = false
                    }
                }) { Text("KAYDET") }
            },
            dismissButton = { TextButton(onClick = { showAddNote = false }) { Text("İPTAL") } }
        )
    }

    if (activeFootnoteContent != null) {
        AlertDialog(
            onDismissRequest = { activeFootnoteContent = null },
            title = { 
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFFB71C1C), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Info, null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "Dipnot Açıklaması", 
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = textColor
                    )
                }
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    HorizontalDivider(color = textColor.copy(alpha = 0.1f), thickness = 1.dp)
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = activeFootnoteContent!!,
                        fontSize = 16.sp,
                        lineHeight = 24.sp,
                        color = textColor,
                        textAlign = TextAlign.Start
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { activeFootnoteContent = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB71C1C)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("KAPAT", fontWeight = FontWeight.Bold)
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = paperColor,
            tonalElevation = 8.dp
        )
    }

    Scaffold(
        topBar = {
            Surface(color = Color(0xFF2C2C2C), contentColor = Color.White) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.selectBook(null) }) {
                        Icon(Icons.Default.KeyboardArrowUp, null)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${book.name} - ${pagerState.currentPage + book.firstPage}/${book.pageCount + book.firstPage - 1}", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text("Mîkat-ı Nur Okuyucu", fontSize = 10.sp, color = Color.Gray)
                    }
                    IconButton(onClick = { viewModel.toggleBookmark() }) {
                        Icon(if (pageData?.isBookmarked == true) Icons.Default.Bookmark else Icons.Default.BookmarkBorder, null)
                    }
                }
            }
        },
        bottomBar = {
            Surface(color = Color(0xFFF5F5F5), shadowElevation = 8.dp) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val actions = listOf(
                        "Ayarlar" to Icons.Default.Settings,
                        "İşaret" to Icons.Default.Bookmark,
                        "Notlar" to Icons.Default.StickyNote2,
                        "Geçiş" to Icons.Default.TrendingFlat,
                        "Fihrist" to Icons.Default.List
                    )
                    actions.forEach { (label, icon) ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { 
                            when(label) {
                                "Ayarlar" -> showSettings = true
                                "Notlar" -> showNotes = true
                                "İşaret" -> viewModel.toggleBookmark()
                                "Geçiş" -> showGoToPage = true
                                "Fihrist" -> showFihrist = true
                            }
                        }) {
                            Icon(icon, null, tint = if (label == "İşaret" && pageData?.isBookmarked == true) Color(0xFFFFD700) else Color.Gray, modifier = Modifier.size(24.dp))
                            Text(label, fontSize = 10.sp, color = Color.Gray)
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.padding(innerPadding).fillMaxSize()
        ) { _ ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(paperColor)
                    .padding(20.dp)
            ) {
                SelectionContainer {
                    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
                    val scrollState = rememberScrollState()
                    val content = pageData?.content ?: "Yükleniyor..."
                    val annotatedString = remember(content, fontSize) {
                        RisaleTextEngine.buildPremiumText(
                            content = content,
                            arabicFont = arabicFont,
                            serifFont = serifFont,
                            fontSize = fontSize,
                            onFootnoteClick = { activeFootnoteContent = it }
                        )
                    }

                    Text(
                        text = annotatedString,
                        color = textColor,
                        lineHeight = (fontSize * lineSpacing).sp,
                        textAlign = TextAlign.Justify,
                        onTextLayout = { textLayoutResult = it },
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .pointerInput(content, fontSize) {
                                detectTapGestures { offset ->
                                    textLayoutResult?.let { layout ->
                                        val adjustedOffset = androidx.compose.ui.geometry.Offset(offset.x, offset.y + scrollState.value)
                                        
                                        // Kelime bazlı Lugat araması
                                        if (adjustedOffset.y < layout.size.height && adjustedOffset.x < layout.size.width) {
                                            val position = layout.getOffsetForPosition(adjustedOffset)
                                            
                                            // Önce LinkAnnotation (Dipnot) var mı kontrol edelim
                                            val links = annotatedString.getLinkAnnotations(position, position)
                                            val footnoteLink = links.firstOrNull { it.item is LinkAnnotation.Clickable }
                                            
                                            if (footnoteLink != null) {
                                                // LinkAnnotation var, kendi listener'ı tetiklenebilir 
                                                // veya biz buradan manuel tetikleyebiliriz.
                                                (footnoteLink.item as LinkAnnotation.Clickable).linkInteractionListener?.onClick(footnoteLink.item)
                                            } else {
                                                // Link yoksa kelime aramasına devam et
                                                val wordRange = findWordBounds(annotatedString.text, position)
                                                if (wordRange.first < wordRange.last) {
                                                    val word = annotatedString.substring(wordRange.first, wordRange.last).trim()
                                                    
                                                    if (!word.startsWith("[") || !word.endsWith("]")) {
                                                        val meaning = viewModel.getWordMeaning(word)
                                                        if (meaning.isNotEmpty()) {
                                                            rawSelectedWord = word
                                                            selectedWordMeaning = meaning
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                    )
                }
            }
        }
    }
}

@Composable
fun RisaleSearchScreen(viewModel: RisaleViewModel, onPageClick: (RisalePage) -> Unit) {
    var query by remember { mutableStateOf("") }
    val results by viewModel.searchResults.collectAsState()

    Column(Modifier.fillMaxSize().background(Color(0xFF0D1B2A)).padding(16.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it; viewModel.search(it) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Tüm külliyatta (kelime veya sayfa) ara...", color = Color.White.copy(0.5f)) },
            leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.White) },
            trailingIcon = { if(query.isNotEmpty()) IconButton(onClick = { query = ""; viewModel.search("") }) { Icon(Icons.Default.Close, null, tint = Color.White) } },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White, 
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color(0xFFFFD700),
                unfocusedBorderColor = Color.White.copy(0.3f)
            ),
            shape = RoundedCornerShape(12.dp)
        )
        
        Spacer(Modifier.height(16.dp))
        
        if (results.isEmpty() && query.length >= 3) {
            Box(Modifier.fillMaxSize(), Alignment.Center) {
                Text("Sonuç bulunamadı.", color = Color.Gray)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(results) { page ->
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { onPageClick(page) },
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(0.1f)),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color.White.copy(0.1f))
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                                Text(
                                    text = page.bookId.uppercase(),
                                    color = Color(0xFFFFD700),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "Sayfa ${page.pageNumber}",
                                    color = Color.White.copy(0.6f),
                                    fontSize = 12.sp
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            
                            // Arama terimini vurgula (Basit snippet)
                            val snippet = getSearchSnippet(page.content, query)
                            Text(
                                text = snippet,
                                color = Color.White,
                                fontSize = 14.sp,
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

fun getSearchSnippet(content: String, query: String): String {
    val cleanContent = content.replace(Regex("<[^>]*>"), "").replace("\n", " ")
    val index = cleanContent.indexOf(query, ignoreCase = true)
    if (index == -1) return cleanContent.take(150) + "..."
    
    val start = (index - 40).coerceAtLeast(0)
    val end = (index + query.length + 80).coerceAtMost(cleanContent.length)
    
    var prefix = if (start > 0) "..." else ""
    var suffix = if (end < cleanContent.length) "..." else ""
    
    return prefix + cleanContent.substring(start, end).trim() + suffix
}

fun findWordBounds(text: String, index: Int): IntRange {
    if (text.isEmpty() || index < 0 || index >= text.length) return 0..0
    var start = index
    while (start > 0 && !text[start - 1].isWhitespace() && text[start - 1] !in listOf('.', ',', '!', '?', ';', '(', ')', '"', '\n')) {
        start--
    }
    var end = index
    while (end < text.length && !text[end].isWhitespace() && text[end] !in listOf('.', ',', '!', '?', ';', '(', ')', '"', '\n')) {
        end++
    }
    return IntRange(start, end)
}
