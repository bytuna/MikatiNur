package com.example.mkat_nur

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mkat_nur.util.AppConfig
import com.example.mkat_nur.util.ContentManager
import com.example.mkat_nur.util.ManasContent

class FridayMessageActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val contentManager = ContentManager(this)
        val fridayContent = contentManager.getFridayContent()

        setContent {
            MaterialTheme {
                FridayMessageScreen(fridayContent.ayet, fridayContent.hadis, fridayContent.vecize) {
                    finish()
                }
            }
        }
    }
}

@Composable
fun FridayMessageScreen(ayet: ManasContent, hadis: ManasContent, vecize: ManasContent, onClose: () -> Unit) {
    val bgGradient = Brush.verticalGradient(
        listOf(Color(0xFF0D1B2A), Color(0xFF1B263B))
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgGradient)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(vertical = 24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
            shape = RoundedCornerShape(24.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "HAYIRLI CUMALAR",
                    color = Color(0xFFFF9800),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(16.dp))

                ContentSection("Günün Ayeti", ayet.text, ayet.source)
                Spacer(Modifier.height(16.dp))
                ContentSection("Günün Hadisi", hadis.text, hadis.source)
                Spacer(Modifier.height(16.dp))
                ContentSection("Günün Vecizesi", vecize.text, vecize.source)

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = onClose,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Close, null, tint = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text("KAPAT", color = Color.White, fontWeight = FontWeight.Bold)
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    text = "${AppConfig.PROJECT_NAME} v${AppConfig.VERSION_NAME}\nDeveloped by ${AppConfig.DEVELOPER}",
                    color = Color.White.copy(alpha = 0.3f),
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 14.sp
                )
            }
        }
    }
}

@Composable
fun ContentSection(title: String, content: String, source: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            color = Color(0xFFFF9800).copy(alpha = 0.2f),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = title,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                color = Color(0xFFFF9800),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = "“$content”",
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            fontStyle = FontStyle.Italic,
            lineHeight = 22.sp
        )
        if (source.isNotEmpty()) {
            Text(
                text = source,
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
