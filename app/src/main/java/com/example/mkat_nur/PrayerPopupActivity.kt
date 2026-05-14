package com.example.mkat_nur

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class PrayerPopupActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val type = intent.getStringExtra("TYPE") ?: "İçerik"
        val content = intent.getStringExtra("CONTENT") ?: "Namaz uykudan hayırlıdır."
        val source = intent.getStringExtra("SOURCE") ?: ""
        val prayerName = intent.getStringExtra("PRAYER_NAME") ?: "Vakit"

        setContent {
            MaterialTheme {
                PopupScreen(type, content, source, prayerName) {
                    finish()
                }
            }
        }
    }
}

@Composable
fun PopupScreen(type: String, content: String, source: String, prayerName: String, onClose: () -> Unit) {
    val bgGradient = Brush.verticalGradient(
        listOf(Color(0xFF0D1B2A), Color(0xFF1B263B))
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgGradient)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
            shape = RoundedCornerShape(32.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_mosque),
                    contentDescription = null,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .padding(bottom = 16.dp),
                    contentScale = ContentScale.Crop
                )
                Surface(
                    color = Color(0xFFFF9800),
                    shape = RoundedCornerShape(50.dp)
                ) {
                    Text(
                        text = prayerName.uppercase() + " VAKTİ",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        color = Color.Black,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.height(24.dp))

                Text(
                    text = type,
                    color = Color(0xFFFF9800),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    text = if (content.startsWith("“")) content else "“$content”",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    lineHeight = 36.sp,
                    fontStyle = FontStyle.Italic
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    text = source,
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(40.dp))

                Button(
                    onClick = onClose,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Close, null, tint = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text("KAPAT", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
