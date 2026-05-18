package com.example.mkat_nur.ui.risale

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import com.example.mkat_nur.model.RisaleBook
import com.example.mkat_nur.model.RisalePage
import com.example.mkat_nur.model.RisaleSection
import com.example.mkat_nur.viewmodel.RisaleUiState
import com.example.mkat_nur.viewmodel.RisaleViewModel
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import com.example.mkat_nur.R

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
                Column {
                    CenterAlignedTopAppBar(
                        title = {
                            Text(
                                text = if (isSearching) "ARAMA" else selectedBook?.name?.uppercase() ?: "RİSALE-İ NUR",
                                fontWeight = FontWeight.Black,
                                color = if (themeMode == "Dark" || selectedBook == null) Color(0xFFFFD700) else Color(0xFF1B263B)
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = {
                                if (isSearching) isSearching = false
                                else if (selectedBook != null) viewModel.selectBook(null)
                                else onMenuClick()
                            }) {
                                Icon(
                                    imageVector = if (selectedBook == null && !isSearching) Icons.Default.Menu else Icons.Default.ArrowBack,
                                    contentDescription = null,
                                    tint = if (themeMode == "Dark" || selectedBook == null) Color.White else Color.Black
                                )
                            }
                        },
                        actions = {
                            if (selectedBook == null && !isSearching) {
                                IconButton(onClick = { showBookmarks = true }) {
                                    Icon(Icons.Default.Bookmarks, null, tint = Color.White)
                                }
                                IconButton(onClick = { isSearching = true }) {
                                    Icon(Icons.Default.Search, null, tint = Color.White)
                                }
                            } else if (selectedBook != null) {
                                IconButton(onClick = { viewModel.toggleBookmark() }) {
                                    Icon(
                                        imageVector = if (currentPageData?.isBookmarked == true) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                                        contentDescription = null,
                                        tint = if (themeMode == "Dark") Color(0xFFFFD700) else Color(0xFF1B263B)
                                    )
                                }
                            }
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                    )
                    
                    if (selectedBook == null && !isSearching) {
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
                androidx.compose.foundation.lazy.LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
            .height(260.dp) // Biraz daha optimize edilmiş oran
            .clickable { onClick(book) },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (book.coverImageRes != null) {
                Image(
                    painter = painterResource(id = book.coverImageRes),
                    contentDescription = book.name,
                    contentScale = ContentScale.FillBounds, // Kapak formuna tam yayılması için
                    modifier = Modifier.fillMaxSize()
                )
                
                // Resmin üzerine hafif bir derinlik katmak için degrade
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.3f)
                                ),
                                startY = 300f
                            )
                        )
                )
            } else {
                // Resim yoksa eski renkli tasarım
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(book.coverColor))
                        .padding(16.dp)
                ) {
                    Column(modifier = Modifier.align(Alignment.Center)) {
                        Text(
                            text = book.name,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = book.author,
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
            
            // Sayfa sayısı etiketi - Daha şık görünüm
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp),
                color = Color.Black.copy(alpha = 0.6f),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = "${book.pageCount} S.",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
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
    val themeMode by viewModel.themeMode.collectAsState()
    val selectedVerse by viewModel.selectedVerse.collectAsState()
    
    var showSettings by remember { mutableStateOf(false) }
    var showIndex by remember { mutableStateOf(false) }
    var showJumpToPage by remember { mutableStateOf(false) }
    var jumpToPageInput by remember { mutableStateOf("") }
    var selectedWord by remember { mutableStateOf<String?>(null) }

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
        viewModel.loadPage(book.id, pagerState.currentPage + 1)
    }

    if (showJumpToPage) {
        AlertDialog(
            onDismissRequest = { showJumpToPage = false },
            title = { Text("Sayfaya Git") },
            text = {
                OutlinedTextField(
                    value = jumpToPageInput,
                    onValueChange = { if (it.all { char -> char.isDigit() }) jumpToPageInput = it },
                    label = { Text("Sayfa Numarası (1-${book.pageCount})") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val page = jumpToPageInput.toIntOrNull()
                    if (page != null && page in 1..book.pageCount) {
                        scope.launch { pagerState.scrollToPage(page - 1) }
                        showJumpToPage = false
                        jumpToPageInput = ""
                    }
                }) { Text("GİT") }
            },
            dismissButton = {
                TextButton(onClick = { showJumpToPage = false }) { Text("İPTAL") }
            }
        )
    }

    if (selectedVerse != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearSelectedVerse() },
            title = { 
                Text(
                    text = "Ayet - ${selectedVerse!!.verseKey}", 
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFD700)
                ) 
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = selectedVerse!!.arabicText ?: "",
                        textAlign = TextAlign.Right,
                        fontSize = 24.sp,
                        modifier = Modifier.fillMaxWidth(),
                        lineHeight = 36.sp,
                        fontFamily = arabicFont
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = selectedVerse!!.translation ?: "",
                        fontSize = 16.sp,
                        textAlign = TextAlign.Justify,
                        fontFamily = serifFont
                    )
                }
            },
            confirmButton = { 
                TextButton(onClick = { viewModel.clearSelectedVerse() }) { 
                    Text("KAPAT", color = Color(0xFFFFD700)) 
                } 
            },
            containerColor = Color(0xFF1B263B),
            titleContentColor = Color.White,
            textContentColor = Color.White
        )
    }

    if (selectedWord != null) {
        AlertDialog(
            onDismissRequest = { selectedWord = null },
            title = { Text(selectedWord!!, fontWeight = FontWeight.Bold, fontFamily = serifFont) },
            text = { Text(viewModel.getWordMeaning(selectedWord!!), fontFamily = serifFont) },
            confirmButton = { TextButton(onClick = { selectedWord = null }) { Text("TAMAM") } }
        )
    }

    if (showSettings) {
        ModalBottomSheet(onDismissRequest = { showSettings = false }) {
            Column(Modifier.padding(24.dp)) {
                Text("Okuma Ayarları", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Spacer(Modifier.height(16.dp))
                
                Text("Yazı Boyutu: ${fontSize.toInt()}sp")
                Slider(
                    value = fontSize,
                    onValueChange = { viewModel.setFontSize(it) },
                    valueRange = 12f..32f
                )
                
                Spacer(Modifier.height(16.dp))
                Text("Tema")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("White", "Sepia", "Dark", "Original").forEach { theme ->
                        FilterChip(
                            selected = themeMode == theme,
                            onClick = { viewModel.setTheme(theme) },
                            label = { Text(theme) }
                        )
                    }
                }
                Spacer(Modifier.height(40.dp))
            }
        }
    }

    if (showIndex) {
        ModalBottomSheet(onDismissRequest = { showIndex = false }) {
            Column(Modifier.padding(16.dp).fillMaxHeight(0.6f)) {
                Text("Fihrist - ${book.name}", fontWeight = FontWeight.Bold, fontSize = 20.sp, fontFamily = serifFont)
                Spacer(Modifier.height(16.dp))
                if (book.sections.isEmpty()) {
                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                        Text("Fihrist bulunamadı.", color = Color.Gray)
                    }
                } else {
                    androidx.compose.foundation.lazy.LazyColumn {
                        items(book.sections) { section ->
                            ListItem(
                                headlineContent = { Text(section.title, fontFamily = serifFont) },
                                trailingContent = { Text("S. ${section.pageNumber}", color = Color.Gray, fontFamily = serifFont) },
                                modifier = Modifier.clickable {
                                    scope.launch { pagerState.scrollToPage(section.pageNumber - 1) }
                                    showIndex = false
                                }
                            )
                            HorizontalDivider(color = Color.Black.copy(alpha = 0.05f))
                        }
                    }
                }
            }
        }
    }
    
    Column(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { _ ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .background(paperColor, RoundedCornerShape(16.dp))
                    .padding(24.dp)
            ) {
                Column {
                    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
                    
                    val content = pageData?.content ?: "Yükleniyor..."
                    val annotatedString = parseCustomHtml(content, arabicFont, serifFont)
                    
                    Text(
                        text = annotatedString,
                        color = textColor,
                        fontSize = fontSize.sp,
                        lineHeight = (fontSize * 1.6).sp,
                        textAlign = TextAlign.Justify,
                        onTextLayout = { textLayoutResult = it },
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .pointerInput(content) {
                                detectTapGestures { offset ->
                                    textLayoutResult?.let { layout ->
                                        if (offset.y < layout.size.height) {
                                            val position = layout.getOffsetForPosition(offset)
                                            val wordRange = findWordBounds(content, position)
                                            if (wordRange.first < wordRange.last && wordRange.last <= content.length) {
                                                val word = content.substring(wordRange.first, wordRange.last)
                                                if (word.isNotBlank() && !word.contains("<") && !word.contains(">")) {
                                                    selectedWord = word
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                    )
                    
                    Text(
                        text = "- ${pagerState.currentPage + 1} -",
                        color = textColor.copy(alpha = 0.5f),
                        fontSize = 14.sp,
                        modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 16.dp),
                        fontFamily = serifFont
                    )
                }
            }
        }
        
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.Black.copy(alpha = 0.6f)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Sayfa: ${pagerState.currentPage + 1} / ${book.pageCount}",
                    color = Color.White,
                    fontFamily = serifFont,
                    modifier = Modifier.clickable { showJumpToPage = true }
                )
                Row {
                    IconButton(onClick = { showIndex = true }) { Icon(Icons.Default.List, null, tint = Color.White) }
                    IconButton(onClick = { showSettings = true }) { Icon(Icons.Default.Settings, null, tint = Color.White) }
                }
            }
        }
    }
}

@Composable
fun RisaleSearchScreen(viewModel: RisaleViewModel, onPageClick: (RisalePage) -> Unit) {
    var query by remember { mutableStateOf("") }
    val results by viewModel.searchResults.collectAsState()

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = { 
                query = it
                viewModel.search(it)
            },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Tüm külliyatta ara...", color = Color.White.copy(0.5f)) },
            leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.White) },
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
        )
        
        Spacer(Modifier.height(16.dp))
        
        androidx.compose.foundation.lazy.LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(results) { page ->
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { onPageClick(page) },
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(0.1f))
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("${page.bookId.uppercase()} - Sayfa ${page.pageNumber}", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold)
                        Text(page.content.take(150) + "...", color = Color.White, fontSize = 12.sp, maxLines = 3)
                    }
                }
            }
        }
    }
}

fun parseCustomHtml(text: String, arabicFont: FontFamily, serifFont: FontFamily): AnnotatedString {
    return buildAnnotatedString {
        val parts = text.split(Regex("(?=<)|(?<=>)"))
        parts.forEachIndexed { index, part ->
            when {
                part.startsWith("<center>") || part == "</center>" -> { /* Skip */ }
                part.startsWith("<font color='#D32F2F'>") -> { /* skip */ }
                part == "</font>" -> { /* skip */ }
                part.startsWith("<h1>") -> { /* skip */ }
                part == "</h1>" -> { /* skip */ }
                else -> {
                    val prevPart = if (index > 0) parts[index - 1] else ""
                    if (prevPart.contains("color='#D32F2F'")) {
                        withStyle(style = SpanStyle(color = Color(0xFFD32F2F), fontWeight = FontWeight.Bold, fontFamily = arabicFont, fontSize = 22.sp)) {
                            append(part)
                        }
                    } else if (prevPart.contains("<h1>")) {
                        withStyle(style = SpanStyle(fontSize = 28.sp, fontWeight = FontWeight.Black, fontFamily = serifFont)) {
                            append(part)
                        }
                    } else {
                        withStyle(style = SpanStyle(fontFamily = serifFont)) {
                            append(part)
                        }
                    }
                }
            }
        }
    }
}

fun findWordBounds(text: String, index: Int): IntRange {
    if (text.isEmpty() || index < 0 || index >= text.length) return 0..0
    
    var start = index
    while (start > 0 && !text[start - 1].isWhitespace() && text[start - 1] !in listOf('.', ',', '!', '?', ':', ';', '(', ')', '"')) {
        start--
    }
    
    var end = index
    while (end < text.length && !text[end].isWhitespace() && text[end] !in listOf('.', ',', '!', '?', ':', ';', '(', ')', '"')) {
        end++
    }
    
    return IntRange(start, end)
}
