package com.example.mkat_nur.ui.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mkat_nur.viewmodel.LibraryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryIndexScreen(
    viewModel: LibraryViewModel,
    onMenuClick: () -> Unit,
    onNavigateToReader: (String, Int) -> Unit
) {
    val books by viewModel.books.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "KÜTÜPHANE",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFD700)
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(
                            imageVector = Icons.Default.Menu,
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
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(books) { book ->
                    val lastPage = book.lastReadPage
                    ListItem(
                        headlineContent = { Text(book.title ?: "", fontWeight = FontWeight.Bold) },
                        supportingContent = { 
                            if (lastPage != null) {
                                Text(
                                    "Okumaya Devam Et: Sayfa $lastPage",
                                    fontSize = 12.sp,
                                    color = Color(0xFF4CAF50) // Okunmuş kitap yeşil
                                )
                            } else {
                                Text("${book.pageCount ?: 0} Sayfa", fontSize = 12.sp, color = Color.Gray)
                            }
                        },
                        trailingContent = { Icon(Icons.Default.ChevronRight, null) },
                        modifier = Modifier.clickable {
                            // Eğer daha önce okunduysa kaldığı yerden, okunmadıysa 0 (akıllı tespit) ile başlat
                            onNavigateToReader(book.slug ?: "", lastPage ?: 0)
                        }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color.Gray.copy(alpha = 0.2f))
                }
            }
        }
    }
}
