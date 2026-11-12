package com.example.mkat_nur.ui.share

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mkat_nur.R
import com.example.mkat_nur.util.ShareUtils
import com.example.mkat_nur.viewmodel.PrayerViewModel
import com.example.mkat_nur.viewmodel.QuranViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareCardScreen(
    quranViewModel: QuranViewModel = viewModel(),
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val isVerseLoading by quranViewModel.isAiLoading.collectAsState()

    val randomVerse by quranViewModel.randomVerse.collectAsState()
    val randomSurahName by quranViewModel.randomSurahName.collectAsState()

    LaunchedEffect(Unit) {
        if (randomVerse == null) {
            quranViewModel.fetchRandomVerse()
        }
    }

    val bgColors = listOf(Color(0xFF0D1B2A), Color(0xFF1B263B))

    Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(bgColors))) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("RASTGELE AYET PAYLAŞ", fontWeight = FontWeight.Black, color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                        }
                    },
                    actions = {
                        IconButton(onClick = { quranViewModel.fetchRandomVerse() }, enabled = !isVerseLoading) {
                            Icon(Icons.Default.Refresh, "Yenile", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(20.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (randomVerse == null || isVerseLoading) {
                    CircularProgressIndicator(color = Color(0xFFFFD700))
                    Spacer(Modifier.height(16.dp))
                    Text("Rastgele bir ayet seçiliyor...", color = Color.White.copy(alpha = 0.7f))
                } else {
                    // Kart Önizlemesi
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(24.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.2f))
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.FormatQuote, null, tint = Color(0xFFFFD700), modifier = Modifier.size(40.dp))
                            
                            Text(
                                text = randomVerse?.arabicText ?: "",
                                color = Color.White,
                                fontSize = 28.sp,
                                textAlign = TextAlign.Center,
                                fontFamily = FontFamily(Font(R.font.uthman_taha)),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                            )
                            
                            Text(
                                text = "“${randomVerse?.translation}”",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                            
                            Spacer(Modifier.height(16.dp))
                            
                            val displaySource = if (randomSurahName != null) {
                                "$randomSurahName Suresi, ${randomVerse?.verseNumber}. Ayet"
                            } else {
                                "Kur'an-ı Kerim (${randomVerse?.verseKey})"
                            }

                            Text(
                                text = "- $displaySource -",
                                color = Color(0xFFFFD700).copy(alpha = 0.7f),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(Modifier.height(32.dp))

                    Spacer(Modifier.height(24.dp))

                    Button(
                        onClick = {
                            val verse = randomVerse ?: return@Button
                            val title = "Ayet-i Kerime"
                            val content = verse.translation ?: ""
                            val source = if (randomSurahName != null) {
                                "$randomSurahName Suresi, ${verse.verseNumber}. Ayet"
                            } else {
                                "Kur'an-ı Kerim (${verse.verseKey})"
                            }
                            ShareUtils.shareInfoAsImage(context, title, content, source, arabicText = verse.arabicText)
                        },
                        modifier = Modifier.fillMaxWidth().height(60.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                        shape = RoundedCornerShape(16.dp),
                        enabled = !isVerseLoading
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            tint = Color(0xFF1B263B)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = "PAYLAŞ",
                            color = Color(0xFF1B263B),
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp
                        )
                    }

                    TextButton(
                        onClick = { quranViewModel.fetchRandomVerse() },
                        modifier = Modifier.padding(top = 8.dp),
                        enabled = !isVerseLoading
                    ) {
                        Text("BAŞKA BİR AYET SEÇ", color = Color.White.copy(alpha = 0.6f), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
