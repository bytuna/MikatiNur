package com.example.mkat_nur.ui.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mkat_nur.data.local.entity.Book
import com.example.mkat_nur.data.local.entity.Section
import com.example.mkat_nur.viewmodel.LibraryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryIndexScreen(
    viewModel: LibraryViewModel,
    onMenuClick: () -> Unit,
    onNavigateToReader: (String, Int) -> Unit
) {
    val books by viewModel.books.collectAsState()
    val sections by viewModel.sections.collectAsState()
    var selectedBook by remember { mutableStateOf<Book?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        selectedBook?.title ?: "KÜTÜPHANE",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFD700)
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (selectedBook != null) {
                            selectedBook = null
                        } else {
                            onMenuClick()
                        }
                    }) {
                        Icon(
                            imageVector = if (selectedBook == null) Icons.Default.Menu else Icons.Default.ArrowBack,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1B263B))
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            if (selectedBook == null) {
                // Kitap Listesi
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(books) { book ->
                        ListItem(
                            headlineContent = { Text(book.title ?: "", fontWeight = FontWeight.Bold) },
                            supportingContent = { Text("${book.pageCount ?: 0} Sayfa", fontSize = 12.sp) },
                            trailingContent = { Icon(Icons.Default.ChevronRight, null) },
                            modifier = Modifier.clickable {
                                selectedBook = book
                                viewModel.loadSections(book.slug ?: "")
                            }
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color.Gray.copy(alpha = 0.2f))
                    }
                }
            } else {
                // Fihrist (Section) Listesi
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(sections) { section ->
                        SectionItem(section) {
                            onNavigateToReader(section.bookSlug ?: "", section.pageStart ?: 1)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SectionItem(section: Section, onClick: () -> Unit) {
    // Derinliğe göre girinti (Padding)
    val paddingStart = ((section.depth ?: 0) * 16).dp
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .padding(start = paddingStart + 16.dp, end = 16.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = section.title ?: "",
                fontSize = if ((section.depth ?: 0) == 0) 16.sp else 14.sp,
                fontWeight = if ((section.depth ?: 0) == 0) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "S. ${section.pageStart ?: 0}",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color.Gray.copy(alpha = 0.1f))
}
