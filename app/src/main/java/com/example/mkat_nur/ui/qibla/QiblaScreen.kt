package com.example.mkat_nur.ui.qibla

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mkat_nur.viewmodel.PrayerViewModel

@Composable
fun QiblaScreen(viewModel: PrayerViewModel) {
    val isInDarkMode = isSystemInDarkTheme()
    val bgColors = if (isInDarkMode) listOf(Color(0xFF121212), Color(0xFF1E1E1E))
    else listOf(Color(0xFF023E8A), Color(0xFF0077B6))

    Box(
        modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(bgColors)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("KIBLE PUSULASI", fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color.White)
            Spacer(Modifier.height(40.dp))

            // Pusula görseli veya mekanizması buraya gelecek
            Text("Pusula Yükleniyor...", color = Color.White.copy(alpha = 0.6f))
        }
    }
}