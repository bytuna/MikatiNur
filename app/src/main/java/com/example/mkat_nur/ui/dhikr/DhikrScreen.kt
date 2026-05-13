package com.example.mkat_nur.ui.dhikr

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DhikrScreen() {
    val isInDarkMode = isSystemInDarkTheme()
    val bgColors = if (isInDarkMode) listOf(Color(0xFF121212), Color(0xFF1E1E1E))
    else listOf(Color(0xFF023E8A), Color(0xFF0077B6))

    var count by remember { mutableStateOf(0) }

    Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(bgColors))) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("ZİKİRMATİK", fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color.White)
            Spacer(Modifier.height(40.dp))

            // Sayac Kutusu
            Surface(
                color = Color.White.copy(alpha = 0.1f),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.size(200.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = count.toString(), fontSize = 64.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            Spacer(Modifier.height(40.dp))

            Button(
                onClick = { count++ },
                modifier = Modifier.size(120.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))
            ) {
                Text("ZİKİR", color = Color.White, fontWeight = FontWeight.Bold)
            }

            TextButton(onClick = { count = 0 }) {
                Text("Sıfırla", color = Color.White.copy(alpha = 0.6f))
            }
        }
    }
}