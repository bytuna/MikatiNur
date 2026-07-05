package com.example.mkat_nur.ui.risale

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.mkat_nur.R

data class RisaleBook(
    val id: String,
    val name: String,
    val coverPath: String // assets içindeki yol
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RisaleLibraryScreen(
    onBookClick: (RisaleBook) -> Unit,
    onMenuClick: () -> Unit
) {
    val books = listOf(
        RisaleBook("sozler", "Sözler", "file:///android_asset/risale_web/covers/sozler.jpg"),
        RisaleBook("mektubat", "Mektubat", "file:///android_asset/risale_web/covers/mektubat.jpg"),
        RisaleBook("lemalar", "Lemalar", "file:///android_asset/risale_web/covers/lemalar.jpg"),
        RisaleBook("sualar", "Şualar", "file:///android_asset/risale_web/covers/sualar.jpg"),
        RisaleBook("asayi-musa", "Asa-yı Musa", "file:///android_asset/risale_web/covers/asayi-musa.jpg"),
        RisaleBook("mesnevi-i-nuriye", "Mesnevi-i Nuriye", "file:///android_asset/risale_web/covers/mesnevi-i-nuriye.jpg")
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Mîkat-ı Nur Külliyat", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF1B263B),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFFF0F2F5)
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(padding).fillMaxSize()
        ) {
            items(books) { book ->
                BookCard(book = book, onClick = { onBookClick(book) })
            }
        }
    }
}

@Composable
fun BookCard(book: RisaleBook, onClick: () -> Unit) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(12.dp)
        ) {
            AsyncImage(
                model = book.coverPath,
                contentDescription = book.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = book.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = Color(0xFF1B263B)
            )
        }
    }
}
