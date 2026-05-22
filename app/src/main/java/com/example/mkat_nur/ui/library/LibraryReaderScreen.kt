package com.example.mkat_nur.ui.library

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
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
    val scrollState = rememberScrollState()
    val sheetState = rememberModalBottomSheetState()
    var showSheet by remember { mutableStateOf(false) }

    // Sayfa ilk açıldığında veya değiştiğinde veriyi yükle
    LaunchedEffect(bookSlug, initialPage) {
        viewModel.loadPage(bookSlug, initialPage)
    }

    // Kelime anlamı değiştiğinde bottom sheet'i göster
    LaunchedEffect(wordMeaning) {
        if (wordMeaning != null) {
            showSheet = true
        }
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
                }
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewModel.prevPage() },
                        enabled = currentPage?.prevPage != null
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ChevronLeft, null)
                            Text("Geri")
                        }
                    }

                    Text(
                        "${currentPage?.pageNumber ?: ""} / ${currentPage?.bookSlug?.uppercase() ?: ""}",
                        fontWeight = FontWeight.Medium
                    )

                    IconButton(
                        onClick = { viewModel.nextPage() },
                        enabled = currentPage?.nextPage != null
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("İleri")
                            Icon(Icons.Default.ChevronRight, null)
                        }
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
                val annotatedString = remember(page.contentHtml) {
                    HtmlTextHelper.parseHtmlWithDictionary(page.contentHtml)
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(16.dp)
                ) {
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
                    Spacer(modifier = Modifier.height(100.dp))
                }
            } ?: Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
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
    }
}
