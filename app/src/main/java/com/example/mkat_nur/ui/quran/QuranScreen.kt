package com.example.mkat_nur.ui.quran

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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

    val bgColors = listOf(Color(0xFF0D1B2A), Color(0xFF1B263B))

    Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(bgColors))) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = if (selectedSurah == null) "Kur'an-ı Kerim" else selectedSurah!!.name,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
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
            }
        ) { padding ->
            Box(modifier = Modifier.padding(padding)) {
                if (selectedSurah == null) {
                    when (val state = uiState) {
                        is QuranUiState.Loading -> LoadingBox()
                        is QuranUiState.Success -> SurahList(state.surahs) { surah ->
                            selectedSurah = surah
                            viewModel.fetchSurahDetail(surah.id)
                        }
                        is QuranUiState.Error -> ErrorBox(state.message)
                    }
                } else {
                    when (val state = detailUiState) {
                        is SurahDetailUiState.Loading -> LoadingBox()
                        is SurahDetailUiState.Success -> VerseList(state.verses)
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
        CircularProgressIndicator(color = Color.White)
    }
}

@Composable
fun ErrorBox(message: String) {
    Box(Modifier.fillMaxSize(), Alignment.Center) {
        Text("Hata: $message", color = Color.White)
    }
}

@Composable
fun SurahList(surahs: List<Surah>, onSurahClick: (Surah) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(surahs) { surah ->
            SurahItem(surah, onSurahClick)
        }
    }
}

@Composable
fun SurahItem(surah: Surah, onClick: (Surah) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick(surah) },
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(8.dp),
                color = Color.White.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(surah.id.toString(), color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(surah.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(
                    text = "${surah.revelationPlace} • ${surah.verseCount} Ayet",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 12.sp
                )
            }
            if (surah.arabicName != null) {
                Text(
                    text = surah.arabicName,
                    color = Color(0xFFFFD700),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun VerseList(verses: List<Verse>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(verses) { verse ->
            VerseItem(verse)
        }
    }
}

@Composable
fun VerseItem(verse: Verse) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.size(32.dp),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFFFD700).copy(alpha = 0.2f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(verse.verseNumber.toString(), color = Color(0xFFFFD700), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.width(8.dp))
            HorizontalDivider(modifier = Modifier.weight(1f), color = Color.White.copy(alpha = 0.1f))
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = verse.content,
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.fillMaxWidth(),
            textAlign = androidx.compose.ui.text.style.TextAlign.Right
        )
        val translationText = verse.translations?.firstOrNull()?.text
        if (translationText != null) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = translationText,
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        }
    }
}
