package com.example.mkat_nur.ui.tesbihat

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TesbihatScreen(onMenuClick: () -> Unit) {
    val context = LocalContext.current
    var selectedPrayer by remember { mutableStateOf("sabah") }
    var language by remember { mutableStateOf("tr") } // "tr" or "ar"
    
    // Metni dosyadan okuyan fonksiyon
    fun loadTesbihatText(prayer: String, lang: String): String {
        return try {
            val fileName = "${prayer}_${lang}.txt"
            context.assets.open(fileName).bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            "İçerik yüklenemedi (${prayer}_${lang}.txt)"
        }
    }

    val tesbihatContent = remember(selectedPrayer, language) {
        loadTesbihatText(selectedPrayer, language)
    }

    val bgColors = listOf(Color(0xFF1B263B), Color(0xFF0D1B2A))

    Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(bgColors))) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("TESBİHAT", fontWeight = FontWeight.Black, color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = onMenuClick) { Icon(Icons.Default.Menu, null, tint = Color.White) }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { padding ->
            Column(modifier = Modifier.padding(padding).fillMaxSize()) {
                // Vakit Seçimi
                ScrollableTabRow(
                    selectedTabIndex = listOf("sabah", "ogle", "ikindi", "aksam", "yatsi").indexOf(selectedPrayer),
                    containerColor = Color.Transparent,
                    contentColor = Color.White,
                    edgePadding = 16.dp,
                    divider = {}
                ) {
                    val prayers = listOf("sabah" to "SABAH", "ogle" to "ÖĞLE", "ikindi" to "İKİNDİ", "aksam" to "AKŞAM", "yatsi" to "YATSI")
                    prayers.forEach { (key, label) ->
                        Tab(
                            selected = selectedPrayer == key,
                            onClick = { selectedPrayer = key },
                            text = { Text(label, fontWeight = if (selectedPrayer == key) FontWeight.Bold else FontWeight.Normal) }
                        )
                    }
                }

                // Dil Seçimi
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    FilterChip(
                        selected = language == "tr",
                        onClick = { language = "tr" },
                        label = { Text("Türkçe") },
                        colors = FilterChipDefaults.filterChipColors(
                            labelColor = Color.White,
                            selectedLabelColor = Color.Black,
                            selectedContainerColor = Color(0xFFFF9800)
                        )
                    )
                    Spacer(Modifier.width(8.dp))
                    FilterChip(
                        selected = language == "ar",
                        onClick = { language = "ar" },
                        label = { Text("Arapça") },
                        colors = FilterChipDefaults.filterChipColors(
                            labelColor = Color.White,
                            selectedLabelColor = Color.Black,
                            selectedContainerColor = Color(0xFFFF9800)
                        )
                    )
                }

                // İçerik Alanı
                Card(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        tesbihatContent.split("\n\n").forEach { paragraph ->
                            val isInstruction = paragraph.trim().let { 
                                it.startsWith("Denilir") || it.contains("okunur") || 
                                it.contains("defa") || it.contains("tamamlanır") ||
                                it.contains("sonra") || it.contains("edilir") ||
                                it.contains("geçilir") || it.contains("devam edilir") ||
                                it.contains("tamamlanır") || it.contains("Ayetleri") ||
                                it.contains("çevrilerek") || it.contains("getirilip") ||
                                it.contains("çekilir")
                            }
                            
                            Text(
                                text = paragraph,
                                color = if (isInstruction) Color(0xFFFF9800) else Color.White,
                                fontSize = if (isInstruction) 14.sp else 22.sp, // Arapça için biraz daha büyük font daha iyi olur
                                fontWeight = if (isInstruction) FontWeight.Bold else FontWeight.Normal,
                                lineHeight = if (isInstruction) 20.sp else 34.sp,
                                textAlign = if (language == "ar" && !isInstruction) androidx.compose.ui.text.style.TextAlign.Right else androidx.compose.ui.text.style.TextAlign.Left,
                                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
