package com.example.mkat_nur.ui.library

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.List
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryReaderScreen(
    viewModel: LibraryViewModel,
    bookSlug: String,
    initialPage: Int,
    onBack: () -> Unit
) {
    val currentPage by viewModel.currentPage.collectAsState()
    val wordMeaning by viewModel.wordMeaning.collectAsState()
    val sections by viewModel.sections.collectAsState()
    val scrollState = rememberScrollState()
    val sheetState = rememberModalBottomSheetState()
    var showSheet by remember { mutableStateOf(false) }
    
    // Fihrist için state
    var showFihrist by remember { mutableStateOf(false) }
    
    // Sayfaya git diyaloğu için state'ler
    var showGoToPageDialog by remember { mutableStateOf(false) }
    var goToPageInput by remember { mutableStateOf("") }

    // Sayfa ilk açıldığında veya değiştiğinde veriyi yükle
    LaunchedEffect(bookSlug, initialPage) {
        viewModel.loadPage(bookSlug, initialPage)
        viewModel.loadSections(bookSlug)
    }

    // Kelime anlamı değiştiğinde bottom sheet'i göster
    LaunchedEffect(wordMeaning) {
        if (wordMeaning != null) {
            showSheet = true
        }
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
                .background(Color(0xFFFCF5E5)) // Sepya benzeri kağıt rengi
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
                        val annotatedString = remember(page.contentHtml) {
                            HtmlTextHelper.parseHtmlWithDictionary(page.contentHtml ?: "")
                        }
                        ClickableText(
                            text = annotatedString,
                            style = TextStyle(
                                fontSize = 18.sp,
                                lineHeight = 28.sp,
                                textAlign = TextAlign.Justify,
                                color = Color.Black
                            ),
                            onClick = { offset ->
                                annotatedString.getStringAnnotations(tag = "DICTIONARY", start = offset, end = offset)
                                    .firstOrNull()?.let { annotation ->
                                        viewModel.searchWordMeaning(annotation.item)
                                    }
                            }
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

        if (showSheet && wordMeaning != null) {
            ModalBottomSheet(
                onDismissRequest = {
                    showSheet = false
                    viewModel.clearWordMeaning()
                },
                sheetState = sheetState
            ) {
                Column(
                    modifier = Modifier
                        .padding(start = 24.dp, end = 24.dp, bottom = 48.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = wordMeaning?.kelime ?: "",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color(0xFFB71C1C),
                        fontWeight = FontWeight.Bold
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Text(
                        text = wordMeaning?.anlam ?: "",
                        style = MaterialTheme.typography.bodyLarge,
                        lineHeight = 24.sp
                    )
                }
            }
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
                    LazyColumn {
                        items(sections) { section ->
                            ListItem(
                                headlineContent = { 
                                    Text(
                                        text = section.title ?: "",
                                        fontWeight = if ((section.depth ?: 0) == 0) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = if ((section.depth ?: 0) == 0) 16.sp else 14.sp
                                    ) 
                                },
                                supportingContent = { Text("Sayfa ${section.pageStart ?: ""}") },
                                modifier = Modifier
                                    .padding(start = ((section.depth ?: 0) * 16).dp)
                                    .clickable {
                                        showFihrist = false
                                        viewModel.loadPage(bookSlug, section.pageStart ?: 27)
                                    }
                            )
                            if ((section.depth ?: 0) == 0) {
                                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)
                            }
                        }
                    }
                }
            }
        }
    }
}
