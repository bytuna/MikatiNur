package com.example.mkat_nur.ui.religious

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WomenSpecialScreen(onBackClick: () -> Unit) {
    val bgColors = listOf(Color(0xFF4A148C), Color(0xFF7B1FA2)) // Purple theme for women special

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Kadın Özel", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color(0xFF4A148C))
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Brush.verticalGradient(bgColors))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                InfoCard(
                    title = "Özel Haller (Hayız ve Nifas)",
                    content = "Kadınlara mahsus özel hallerde (adet ve lohusalık) namaz kılınmaz ve oruç tutulmaz. Bu günlerde kılınmayan namazlar kaza edilmez, ancak tutulmayan Ramazan oruçları kaza edilir."
                )
                
                Spacer(Modifier.height(16.dp))
                
                InfoCard(
                    title = "İbadet ve Zikirler",
                    content = "Bu dönemlerde namaz kılınmasa da; dua edilebilir, zikir çekilebilir (Kelime-i Tevhid, Salavat, Esmaül Hüsna), tesbihat yapılabilir ve dini kitaplar okunabilir. Kur'an-ı Kerim'e el sürmeden dinlenebilir veya ezberden dua ayetleri okunabilir."
                )

                Spacer(Modifier.height(16.dp))

                InfoCard(
                    title = "Temizlik ve Gusül",
                    content = "Özel hal sona erdiğinde gusül abdesti alınması farzdır. Gusül ile birlikte ibadetlere tekrar başlanır."
                )

                Spacer(Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = Color.White)
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "Ayarlar kısmından 'Kadın Özel Modu'nu açarak bu dönemlerde vakit bildirimlerini geçici olarak sessize alabilirsiniz.",
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InfoCard(title: String, content: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                color = Color(0xFFFFCC80),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = content,
                color = Color.White,
                fontSize = 15.sp,
                lineHeight = 22.sp
            )
        }
    }
}
