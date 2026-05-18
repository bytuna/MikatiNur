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
import com.example.mkat_nur.util.AiImageService
import com.example.mkat_nur.util.ShareUtils
import com.example.mkat_nur.viewmodel.PrayerViewModel
import com.example.mkat_nur.viewmodel.QuranViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareCardScreen(
    prayerViewModel: PrayerViewModel = viewModel(),
    quranViewModel: QuranViewModel = viewModel(),
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val isPrayerAiLoading by prayerViewModel.isAiLoading.collectAsState()
    val isQuranAiLoading by quranViewModel.isAiLoading.collectAsState()
    val isAiLoading = isPrayerAiLoading || isQuranAiLoading
    
    val randomVerse by quranViewModel.randomVerse.collectAsState()
    val randomSurahName by quranViewModel.randomSurahName.collectAsState()
    
    var selectedStyle by remember { mutableStateOf<AiImageService.ShareStyle?>(null) }
    var showStyleMenu by remember { mutableStateOf(false) }

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
                        IconButton(onClick = { quranViewModel.fetchRandomVerse() }, enabled = !isAiLoading) {
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
                if (randomVerse == null || isAiLoading) {
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

                    Text("Görsel Tarzı Seçin", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))

                    Spacer(Modifier.height(8.dp))

                    Box(modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = { showStyleMenu = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = selectedStyle?.displayName ?: "Standart (Uygulama İçi)",
                                    color = if (selectedStyle == null) Color.White.copy(alpha = 0.6f) else Color(0xFFFFD700)
                                )
                                Icon(Icons.Default.ArrowDropDown, null, tint = Color.White)
                            }
                        }

                        DropdownMenu(
                            expanded = showStyleMenu,
                            onDismissRequest = { showStyleMenu = false },
                            modifier = Modifier.background(Color(0xFF1B263B)).fillMaxWidth(0.85f)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Standart (Mîkat-ı Nur Arka Planları)", color = Color.White) },
                                leadingIcon = { Icon(Icons.Default.PhotoLibrary, null, tint = Color.White.copy(alpha = 0.7f)) },
                                onClick = {
                                    selectedStyle = null
                                    showStyleMenu = false
                                }
                            )
                            
                            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                            
                            AiImageService.ShareStyle.entries.forEach { style ->
                                DropdownMenuItem(
                                    text = { Text(style.displayName, color = Color.White) },
                                    leadingIcon = { Icon(Icons.Default.AutoAwesome, null, tint = Color(0xFFFFD700)) },
                                    onClick = {
                                        selectedStyle = style
                                        showStyleMenu = false
                                    }
                                )
                            }
                        }
                    }

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
                            
                            if (selectedStyle == null) {
                                ShareUtils.shareInfoAsImage(context, title, content, source, arabicText = verse.arabicText)
                            } else {
                                prayerViewModel.shareWithAi(context, title, content, source, selectedStyle!!, arabicText = verse.arabicText)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(60.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                        shape = RoundedCornerShape(16.dp),
                        enabled = !isAiLoading
                    ) {
                        Icon(
                            imageVector = if (selectedStyle != null) Icons.Default.AutoAwesome else Icons.Default.Share,
                            contentDescription = null,
                            tint = Color(0xFF1B263B)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = if (selectedStyle != null) "AI İLE OLUŞTUR VE PAYLAŞ" else "PAYLAŞ",
                            color = Color(0xFF1B263B),
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp
                        )
                    }

                    TextButton(
                        onClick = { quranViewModel.fetchRandomVerse() },
                        modifier = Modifier.padding(top = 8.dp),
                        enabled = !isAiLoading
                    ) {
                        Text("BAŞKA BİR AYET SEÇ", color = Color.White.copy(alpha = 0.6f), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
