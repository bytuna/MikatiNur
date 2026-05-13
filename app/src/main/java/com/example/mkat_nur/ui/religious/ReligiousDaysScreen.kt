package com.example.mkat_nur.ui.religious

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ReligiousDaysScreen() {
    val isInDarkMode = isSystemInDarkTheme()
    val bgColors = if (isInDarkMode) listOf(Color(0xFF121212), Color(0xFF1E1E1E))
    else listOf(Color(0xFF023E8A), Color(0xFF0077B6))

    Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(bgColors))) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text("DİNİ GÜNLER 2026", fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color.White)
                Spacer(Modifier.height(10.dp))
            }

            // Örnek Günler (Burayı kendi veri yapına göre çoğaltabilirsin)
            val günler = listOf("Ramazan Başlangıcı" to "1 Mart Pazar", "Kadir Gecesi" to "26 Mart Perşembe", "Ramazan Bayramı" to "30 Mart Pazartesi")

            günler.forEach { (ad, tarih) ->
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth().padding(20.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(ad, color = Color.White, fontWeight = FontWeight.Bold)
                            Text(tarih, color = Color.White.copy(alpha = 0.7f))
                        }
                    }
                }
            }
        }
    }
}