package com.example.mkat_nur.ui.library

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.List
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mkat_nur.viewmodel.LibraryViewModel
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryReaderScreen(
    viewModel: LibraryViewModel,
    bookSlug: String,
    initialPage: Int,
    onBack: () -> Unit
) {
    val currentPage by viewModel.currentPage.collectAsState()
    val lugatMap by viewModel.lugatMap.collectAsState()
    val sections by viewModel.sections.collectAsState()
    val scrollState = rememberScrollState()
    val sheetState = rememberModalBottomSheetState()
    
    // Fihrist için state
    var showFihrist by remember { mutableStateOf(false) }
    
    // Sayfaya git diyaloğu için state'ler
    var showGoToPageDialog by remember { mutableStateOf(false) }
    var goToPageInput by remember { mutableStateOf("") }
    
    // Yazı boyutu state'i
    var fontScale by remember { mutableStateOf(1f) }

    // Sayfa ilk açıldığında veya değiştiğinde veriyi yükle
    LaunchedEffect(bookSlug, initialPage) {
        viewModel.loadPage(bookSlug, initialPage)
        viewModel.loadSections(bookSlug)
    }

    // Sayfa değiştiğinde en başa kaydır
    LaunchedEffect(currentPage?.pageId) {
        scrollState.scrollTo(0)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "SAYFA ${currentPage?.pageNumber ?: ""}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = { if (fontScale > 0.7f) fontScale -= 0.1f }) {
                        Text("A-", fontWeight = FontWeight.Bold)
                    }
                    IconButton(onClick = { if (fontScale < 3f) fontScale += 0.1f }) {
                        Text("A+", fontWeight = FontWeight.Bold)
                    }
                    IconButton(onClick = { showFihrist = true }) {
                        Icon(Icons.Default.List, contentDescription = "Fihrist")
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Geri Butonu
                    TextButton(
                        onClick = { viewModel.prevPage() },
                        enabled = currentPage?.prevPage != null,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.ChevronLeft, null)
                        Spacer(Modifier.width(4.dp))
                        Text("Geri", fontSize = 14.sp)
                    }

                    // Sayfa Bilgisi ve Doğrudan Gitme
                    val bookName = remember(currentPage, viewModel.books.collectAsState().value) {
                        val currentSlug = currentPage?.bookSlug
                        viewModel.books.value.find { it.slug == currentSlug }?.title ?: currentSlug?.uppercase() ?: ""
                    }

                    Text(
                        text = "${currentPage?.pageNumber ?: ""} / $bookName",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .weight(1.5f)
                            .clickable { 
                                goToPageInput = (currentPage?.pageNumber ?: "").toString()
                                showGoToPageDialog = true 
                            }
                    )

                    // İleri Butonu
                    TextButton(
                        onClick = { viewModel.nextPage() },
                        enabled = currentPage?.nextPage != null,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("İleri", fontSize = 14.sp)
                        Spacer(Modifier.width(4.dp))
                        Icon(Icons.Default.ChevronRight, null)
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFFCF5E5))
                .pointerInput(Unit) {
                    var totalX = 0f
                    detectDragGestures(
                        onDrag = { change, dragAmount ->
                            // Sadece çok net yatay hareketleri sayfa çevirme olarak algıla
                            if (abs(dragAmount.x) > abs(dragAmount.y) * 3) {
                                totalX += dragAmount.x
                                change.consume()
                            }
                        },
                        onDragEnd = {
                            if (totalX > 100) viewModel.prevPage()
                            else if (totalX < -100) viewModel.nextPage()
                            totalX = 0f
                        }
                    )
                }
        ) {
            currentPage?.let { page ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(16.dp)
                ) {
                    if (page.pageId == "error") {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = "Sayfa içeriği veritabanında bulunamadı.\n(Kitap: $bookSlug, Sayfa: $initialPage)",
                                textAlign = TextAlign.Center,
                                color = Color.Red
                            )
                        }
                    } else {
                        val plainText = remember(page.contentHtml) {
                            android.text.Html.fromHtml(page.contentHtml ?: "", android.text.Html.FROM_HTML_MODE_COMPACT).toString()
                        }
                        
                        LugatTextComponent(
                            kitapMetni = plainText,
                            lugatMap = lugatMap,
                            fontScale = fontScale,
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = TextStyle(
                                fontSize = 18.sp,
                                lineHeight = 28.sp,
                                textAlign = TextAlign.Justify,
                                color = Color.Black
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(100.dp))
                }
            } ?: Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        if (showGoToPageDialog) {
            AlertDialog(
                onDismissRequest = { showGoToPageDialog = false },
                title = { Text("Sayfaya Git") },
                text = {
                    OutlinedTextField(
                        value = goToPageInput,
                        onValueChange = { if (it.all { char -> char.isDigit() }) goToPageInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Sayfa Numarası") },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                        ),
                        singleLine = true
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        val pageNum = goToPageInput.toIntOrNull()
                        if (pageNum != null) {
                            viewModel.loadPage(bookSlug, pageNum)
                            showGoToPageDialog = false
                        }
                    }) { Text("GİT") }
                },
                dismissButton = {
                    TextButton(onClick = { showGoToPageDialog = false }) { Text("İPTAL") }
                }
            )
        }

        if (showFihrist) {
            ModalBottomSheet(
                onDismissRequest = { showFihrist = false }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.7f)
                        .padding(bottom = 24.dp)
                ) {
                    Text(
                        text = "İÇİNDEKİLER",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(16.dp)
                    )
                    HorizontalDivider()
                    val expandedIds = remember { mutableStateMapOf<String, Boolean>() }
                    
                    LazyColumn {
                        // Sadece parent'ı genişletilmiş olanları veya root olanları filtrele
                        val visibleSections = sections.filter { section ->
                            var currentParentId = section.parentId
                            var isVisible = true
                            while (currentParentId != null) {
                                if (expandedIds[currentParentId] != true) {
                                    isVisible = false
                                    break
                                }
                                currentParentId = sections.find { it.id == currentParentId }?.parentId
                            }
                            isVisible
                        }

                        items(visibleSections) { section ->
                            val hasChildren = sections.any { it.parentId == section.id }
                            val isExpanded = expandedIds[section.id] ?: false

                            ListItem(
                                headlineContent = { 
                                    Text(
                                        text = section.title ?: "",
                                        fontWeight = if ((section.depth ?: 0) == 0) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = if ((section.depth ?: 0) == 0) 16.sp else 14.sp,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    ) 
                                },
                                supportingContent = { Text("Sayfa ${section.pageStart ?: ""}", fontSize = 12.sp) },
                                leadingContent = {
                                    if (hasChildren) {
                                        Icon(
                                            if (isExpanded) Icons.Default.ExpandMore else Icons.Default.ChevronRight,
                                            contentDescription = null,
                                            modifier = Modifier.clickable {
                                                expandedIds[section.id] = !isExpanded
                                            }
                                        )
                                    } else {
                                        Spacer(modifier = Modifier.size(24.dp))
                                    }
                                },
                                modifier = Modifier
                                    .padding(start = ((section.depth ?: 0) * 12).dp)
                                    .clickable {
                                        if (hasChildren) {
                                            expandedIds[section.id] = !isExpanded
                                        } else {
                                            showFihrist = false
                                            viewModel.loadPage(bookSlug, section.pageStart ?: 27)
                                        }
                                    }
                            )
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)
                        }
                    }
                }
            }
        }
    }
}
