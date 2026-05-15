package com.example.mkat_nur.ui.quran

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mkat_nur.model.Surah
import com.example.mkat_nur.model.Verse
import com.example.mkat_nur.viewmodel.QuranUiState
import com.example.mkat_nur.viewmodel.QuranViewModel
import com.example.mkat_nur.viewmodel.SurahDetailUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranScreen(
    viewModel: QuranViewModel = viewModel(),
    onMenuClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val detailUiState by viewModel.detailUiState.collectAsState()
    var selectedSurah by remember { mutableStateOf<Surah?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(0) }

    val bgColors = listOf(Color(0xFF002B36), Color(0xFF073642)) // Koyu petrol/mavi tema

    Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(bgColors))) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Column {
                    CenterAlignedTopAppBar(
                        title = {
                            Text(
                                text = if (selectedSurah == null) "KUR'AN-I KERİM" else selectedSurah!!.name.uppercase(),
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFFFD700),
                                letterSpacing = 2.sp
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = {
                                if (selectedSurah != null) {
                                    selectedSurah = null
                                } else {
                                    onMenuClick()
                                }
                            }) {
                                Icon(
                                    imageVector = if (selectedSurah == null) Icons.Default.Menu else Icons.Default.ArrowBack,
                                    contentDescription = null,
                                    tint = Color.White
                                )
                            }
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                    )
                    
                    if (selectedSurah == null) {
                        TabRow(
                            selectedTabIndex = selectedTab,
                            containerColor = Color.Transparent,
                            contentColor = Color(0xFFFFD700),
                            indicator = { tabPositions ->
                                TabRowDefaults.SecondaryIndicator(
                                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                    color = Color(0xFFFFD700)
                                )
                            },
                            divider = {}
                        ) {
                            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                                Text("SURELER", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold, color = if(selectedTab == 0) Color(0xFFFFD700) else Color.White.copy(0.6f))
                            }
                            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                                Text("CÜZLER", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold, color = if(selectedTab == 1) Color(0xFFFFD700) else Color.White.copy(0.6f))
                            }
                            Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }) {
                                Text("SAYFALAR", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold, color = if(selectedTab == 2) Color(0xFFFFD700) else Color.White.copy(0.6f))
                            }
                        }

                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            placeholder = { Text("Sure ara...", color = Color.White.copy(alpha = 0.5f)) },
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
                if (selectedSurah == null) {
                    when (val state = uiState) {
                        is QuranUiState.Loading -> LoadingBox()
                        is QuranUiState.Success -> {
                            val filteredSurahs = state.surahs.filter { 
                                it.name.contains(searchQuery, ignoreCase = true) || 
                                it.id.toString() == searchQuery 
                            }
                            SurahList(filteredSurahs) { surah ->
                                selectedSurah = surah
                                viewModel.fetchSurahDetail(surah.id)
                            }
                        }
                        is QuranUiState.Error -> ErrorBox(state.message)
                    }
                } else {
                    when (val state = detailUiState) {
                        is SurahDetailUiState.Loading -> LoadingBox()
                        is SurahDetailUiState.Success -> VerseList(state.verses, selectedSurah!!)
                        is SurahDetailUiState.Error -> ErrorBox(state.message)
                        else -> {}
                    }
                }
            }
        }
    }
}

@Composable
fun LoadingBox() {
    Box(Modifier.fillMaxSize(), Alignment.Center) {
        CircularProgressIndicator(color = Color(0xFFFFD700))
    }
}

@Composable
fun ErrorBox(message: String) {
    Box(Modifier.fillMaxSize(), Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Bir sorun oluştu", color = Color.White, fontWeight = FontWeight.Bold)
            Text(message, color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun SurahList(surahs: List<Surah>, onSurahClick: (Surah) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 16.dp),
    ) {
        items(surahs) { surah ->
            SurahItem(surah, onSurahClick)
            HorizontalDivider(color = Color.White.copy(alpha = 0.05f), thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
        }
    }
}

@Composable
fun SurahItem(surah: Surah, onClick: (Surah) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(surah) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(40.dp)) {
            // Basit bir çerçeve ikonu yerine yıldız kullandık
            Icon(
                imageVector = Icons.Default.PlayArrow, // Geçici simge
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = Color(0xFFFFD700).copy(alpha = 0.3f)
            )
            Text(surah.id.toString(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
        
        Spacer(Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = surah.name,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = (surah.revelationPlace ?: "Mekkî").uppercase(),
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(" • ", color = Color.White.copy(alpha = 0.3f))
                Text(
                    text = "${surah.verseCount} AYET",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        if (surah.arabicName != null) {
            Text(
                text = surah.arabicName,
                color = Color(0xFFFFD700),
                fontSize = 24.sp,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Right
            )
        }
    }
}

@Composable
fun VerseList(verses: List<Verse>, surah: Surah) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Surah Info Header (e-Diyanet style)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFD700).copy(alpha = 0.1f)),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.2f))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = surah.name,
                        color = Color(0xFFFFD700),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "— ${surah.revelationPlace ?: "Mekkî"} • ${surah.verseCount} AYET —",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    
                    Spacer(Modifier.height(20.dp))
                    
                    // Bismillah
                    if (surah.id != 1 && surah.id != 9) {
                        Text(
                            text = "بِسْمِ اللّٰهِ الرَّحْمٰنِ الرَّح۪يمِ",
                            color = Color.White,
                            fontSize = 28.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                    
                    Spacer(Modifier.height(16.dp))
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = { /* TODO: Hepsini Oynat */ },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 24.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, null, tint = Color(0xFF002B36))
                            Spacer(Modifier.width(8.dp))
                            Text("DİNLE", color = Color(0xFF002B36), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
        
        items(verses) { verse ->
            VerseItem(verse)
        }
    }
}

@Composable
fun VerseItem(verse: Verse) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFCF5E5), RoundedCornerShape(16.dp)) // Kağıt rengi arka plan
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ayet Numarası (Süslü)
            Surface(
                modifier = Modifier.size(34.dp),
                shape = CircleShape,
                color = Color(0xFF073642).copy(alpha = 0.05f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF073642).copy(alpha = 0.2f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = verse.verseNumber.toString(),
                        color = Color(0xFF073642),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
            
            // Ayet İşlemleri (Paylaş, Dinle, Kopyala vb.)
            Row {
                IconButton(onClick = { /* TODO: Ses */ }) {
                    Icon(Icons.Default.PlayArrow, null, tint = Color(0xFF073642).copy(alpha = 0.5f))
                }
            }
        }
        
        Spacer(Modifier.height(16.dp))
        
        // Arapça Metin (Okunaklı ve Büyük)
        Text(
            text = verse.content,
            color = Color(0xFF000000), // Siyah/Koyu renk Arapça için daha iyi
            fontSize = 28.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Right,
            lineHeight = 48.sp
        )
        
        val translationText = verse.translations?.firstOrNull()?.text
        if (translationText != null) {
            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = Color(0xFF073642).copy(alpha = 0.1f))
            Spacer(Modifier.height(12.dp))
            // Meal (Türkçe)
            Text(
                text = translationText,
                color = Color(0xFF1B263B),
                fontSize = 16.sp,
                lineHeight = 26.sp,
                textAlign = TextAlign.Justify,
                fontWeight = FontWeight.Normal
            )
        }
    }
}
