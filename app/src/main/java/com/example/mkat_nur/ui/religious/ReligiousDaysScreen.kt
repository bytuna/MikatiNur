package com.example.mkat_nur.ui.religious

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mkat_nur.ui.theme.LocalAppThemeColors
import com.example.mkat_nur.viewmodel.ReligiousDaysUiState
import com.example.mkat_nur.viewmodel.ReligiousDaysViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReligiousDaysScreen(
    viewModel: ReligiousDaysViewModel = viewModel(),
    onMenuClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    LaunchedEffect(Unit) {
        viewModel.loadReligiousDays(context)
    }

    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    val listState = rememberLazyListState()

    val themeColors = LocalAppThemeColors.current
    val bgColors = themeColors.gradientColors

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "DİNİ GÜNLER $currentYear",
                        fontWeight = FontWeight.Black,
                        color = themeColors.textPrimary,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Default.Menu, contentDescription = "Menü", tint = themeColors.textPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = Color.Transparent
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(bgColors))
                .padding(padding)
        ) {
            when (val state = uiState) {
                is ReligiousDaysUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = themeColors.primary
                    )
                }
                is ReligiousDaysUiState.Success -> {
                    LaunchedEffect(state.nextDayIndex) {
                        if (state.nextDayIndex != -1) {
                            listState.animateScrollToItem(state.nextDayIndex)
                        }
                    }

                    Column(modifier = Modifier.fillMaxSize()) {
                        // Bilgi Çubuğu (Veri Kaynağı ve Güncellik)
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            color = themeColors.surface,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, themeColors.cardBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (state.isFromApi) Icons.Default.CloudDone else Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = if (state.isFromApi) Color(0xFF4CAF50) else themeColors.accent,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = if (state.isFromApi) "Diyanet API (Canlı)" else "Resmi Takvim (Onaylı)",
                                            color = themeColors.textPrimary,
                                            fontSize = 13.5.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Veriler güncel ve doğrulanmıştır.",
                                            color = themeColors.textSecondary,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                                
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = state.lastUpdate,
                                            color = themeColors.textSecondary,
                                            fontSize = 10.sp
                                        )
                                    }
                                    Spacer(Modifier.width(6.dp))
                                    IconButton(
                                        onClick = { viewModel.loadReligiousDays(context) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Refresh,
                                            contentDescription = "Güncelle",
                                            tint = themeColors.textSecondary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }

                        LazyColumn(
                            state = listState,
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            itemsIndexed(state.days) { index, day ->
                                val isNext = index == state.nextDayIndex
                                
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .then(
                                            if (isNext) Modifier.border(2.dp, themeColors.accent, RoundedCornerShape(16.dp))
                                            else Modifier
                                        ),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isNext) themeColors.accent.copy(alpha = 0.15f) 
                                                       else themeColors.surface
                                    ),
                                    shape = RoundedCornerShape(16.dp),
                                    border = BorderStroke(if (isNext) 2.dp else 1.dp, if (isNext) themeColors.accent else themeColors.cardBorder)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp)
                                    ) {
                                        val adText = day.ad.orEmpty()
                                        val tarihText = day.tarih.orEmpty()
                                        val hicriText = day.hicriTarih.orEmpty()

                                        if (isNext) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(bottom = 8.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.Star, 
                                                    contentDescription = null, 
                                                    tint = themeColors.accent,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(Modifier.width(4.dp))
                                                Text(
                                                    "Sıradaki Önemli Gün",
                                                    color = themeColors.accent,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = adText,
                                                color = if (isNext) themeColors.accent else themeColors.textPrimary,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 17.sp,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(if (isNext) themeColors.accent else themeColors.background)
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                val tagTextColor = if (isNext) {
                                                    if (themeColors.accent == Color(0xFFFFD700) || themeColors.accent == Color(0xFFFBBF24)) Color(0xFF0D1B2A) else Color.White
                                                } else themeColors.textPrimary
                                                Text(
                                                    text = tarihText,
                                                    color = tagTextColor,
                                                    fontWeight = FontWeight.Black,
                                                    fontSize = 13.sp
                                                )
                                            }
                                        }
                                        if (hicriText.isNotEmpty()) {
                                            Spacer(Modifier.height(6.dp))
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    Icons.Default.Event, 
                                                    null, 
                                                    tint = themeColors.textSecondary,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(Modifier.width(4.dp))
                                                Text(
                                                    text = hicriText,
                                                    color = themeColors.textSecondary,
                                                    fontSize = 13.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                is ReligiousDaysUiState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Info, null, tint = themeColors.textPrimary, modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(16.dp))
                        Text(text = state.message, color = themeColors.textPrimary)
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = { viewModel.loadReligiousDays(context) }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))) {
                            Text("Tekrar Dene", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}
