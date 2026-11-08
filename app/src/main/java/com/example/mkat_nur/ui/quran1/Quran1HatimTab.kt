package com.example.mkat_nur.ui.quran1

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mkat_nur.util.QuranUtils

private val P = Quran1Palette

/** Hatim sekmesi: başlangıç cüzü seçimi, 30 cüz dağıtımı, tamamlama, paylaşma ve hatim duası. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun Quran1HatimTab(onOpenJuz: (Int) -> Unit) {
    val context = LocalContext.current
    val startJuz by Quran1HatimStore.startJuz.collectAsState()
    val completed by Quran1HatimStore.completed.collectAsState()
    val assignments by Quran1HatimStore.assignments.collectAsState()
    val active by Quran1HatimStore.active.collectAsState()

    var selectedStart by remember { mutableIntStateOf(startJuz) }
    var showDelete by remember { mutableStateOf(false) }
    var showDua by remember { mutableStateOf(false) }
    var editingJuz by remember { mutableStateOf<Int?>(null) }
    var personInput by remember { mutableStateOf("") }

    val previewSequence = remember(selectedStart) { Quran1HatimStore.sequence(selectedStart) }
    val completedCount = completed.size
    val percent = ((completedCount / 30f) * 100).toInt()

    // 30 cüz tamamlanınca hatim duasını göster
    LaunchedEffect(completedCount) { if (completedCount == 30) showDua = true }

    if (showDua) HatimDuasiDialog(onDismiss = { showDua = false })

    editingJuz?.let { jNum ->
        AlertDialog(
            onDismissRequest = { editingJuz = null },
            title = { Text("$jNum. Cüz Okuyucusu / Kişi", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = P.Ink) },
            text = {
                Column {
                    Text("Bu cüzü okuyacak kişinin ismini yazın veya temizleyin:", fontSize = 12.sp, color = P.InkSoft)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = personInput,
                        onValueChange = { personInput = it },
                        placeholder = { Text("Örn: Ahmet Bey / Ayşe Hanım") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { Quran1HatimStore.assign(jNum, personInput); editingJuz = null },
                    colors = ButtonDefaults.buttonColors(containerColor = P.Emerald)
                ) { Text("KAYDET", color = P.Paper) }
            },
            dismissButton = { TextButton(onClick = { editingJuz = null }) { Text("İPTAL", color = P.InkSoft) } },
            containerColor = P.Paper,
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (showDelete) {
        AlertDialog(
            onDismissRequest = { showDelete = false },
            title = { Text("Hatimi Sil", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = P.Burgundy) },
            text = { Text("Mevcut hatiminizi ve kaydettiğiniz tüm ilerlemeyi silmek istediğinize emin misiniz? Bu işlem geri alınamaz.", fontSize = 13.sp, color = P.Ink) },
            confirmButton = {
                Button(
                    onClick = { showDelete = false; Quran1HatimStore.reset() },
                    colors = ButtonDefaults.buttonColors(containerColor = P.Burgundy),
                    shape = RoundedCornerShape(8.dp)
                ) { Text("HATİMİ SİL", color = Color.White, fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(onClick = { showDelete = false }) { Text("İPTAL", color = P.InkSoft) } },
            containerColor = P.Paper,
            shape = RoundedCornerShape(16.dp)
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (!active) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = P.Paper),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.5.dp, P.GoldDeep)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Yeni Hatim Başlat", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = P.Ink)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Hatme başlayacağınız cüzü seçin. Seçtiğiniz cüzden başlayarak 30 cüz döngüsel sırayla tamamlanır.",
                            fontSize = 12.sp, color = P.InkSoft
                        )
                        Spacer(Modifier.height(16.dp))
                        Text("Başlangıç Cüzü: $selectedStart. Cüz", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = P.GoldDeep)
                        Slider(
                            value = selectedStart.toFloat(),
                            onValueChange = { selectedStart = it.toInt().coerceIn(1, 30) },
                            valueRange = 1f..30f,
                            steps = 28,
                            colors = SliderDefaults.colors(thumbColor = P.Emerald, activeTrackColor = P.Emerald)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text("Veya numaratörden seçin:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = P.GoldDeep)
                        Spacer(Modifier.height(8.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            (1..30).forEach { num ->
                                val selected = selectedStart == num
                                Surface(
                                    modifier = Modifier.size(36.dp).clickable { selectedStart = num },
                                    shape = CircleShape,
                                    color = if (selected) P.Emerald else P.Gold.copy(alpha = 0.2f),
                                    border = BorderStroke(1.dp, if (selected) P.Emerald else P.Gold.copy(alpha = 0.5f))
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("$num", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (selected) P.Paper else P.Ink)
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        val preview = previewSequence.take(5).joinToString(" ➔ ") + " ... ➔ " + previewSequence.takeLast(3).joinToString(" ➔ ")
                        Text("Sıralama Düzeni: $preview", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = P.Ink)
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = { Quran1HatimStore.start(selectedStart) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = P.Emerald),
                            shape = RoundedCornerShape(12.dp)
                        ) { Text("HATİMİ BAŞLAT", fontWeight = FontWeight.Bold, color = P.Paper) }
                    }
                }
            }
        } else {
            val sequence = Quran1HatimStore.sequence(startJuz)
            val nextJuz = sequence.firstOrNull { !completed.contains(it) } ?: sequence.first()

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = P.Paper),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.5.dp, P.GoldDeep)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Aktif Hatim İlerlemesi", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = P.Ink)
                                Text("Başlangıç: $startJuz. Cüz", fontSize = 11.sp, color = P.InkSoft)
                            }
                            Text("$completedCount / 30 Cüz (%$percent)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = P.GoldDeep)
                        }
                        Spacer(Modifier.height(12.dp))
                        LinearProgressIndicator(
                            progress = completedCount / 30f,
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                            color = P.Emerald,
                            trackColor = P.PaperDark
                        )
                        Spacer(Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { onOpenJuz(nextJuz) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = P.Emerald),
                                shape = RoundedCornerShape(12.dp)
                            ) { Text("Sıradaki: $nextJuz. Cüzü Oku", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = P.Paper, maxLines = 1) }
                            OutlinedButton(
                                onClick = { showDelete = true },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = P.Burgundy),
                                border = BorderStroke(1.dp, P.Burgundy),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Delete, null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Sil", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, Quran1HatimStore.shareText())
                                }
                                context.startActivity(Intent.createChooser(intent, "Hatim Listesini Paylaş"))
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = P.EmeraldSoft),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Share, null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("HATİM LİSTESİNİ PAYLAŞ (WHATSAPP)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                        }
                        if (completedCount == 30) {
                            Spacer(Modifier.height(8.dp))
                            Button(
                                onClick = { showDua = true },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = P.Gold),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.AutoAwesome, null, tint = P.Ink)
                                Spacer(Modifier.width(8.dp))
                                Text("HATİM DUASINI OKU", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = P.Ink)
                            }
                        }
                    }
                }
            }

            item { Text("Cüz Dağıtımı ve Tamamlama Durumu", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = P.Gold) }

            itemsIndexed(sequence, key = { _, juz -> juz }) { idx, juz ->
                val done = completed.contains(juz)
                val person = assignments[juz]
                val range = QuranUtils.getJuzPageRange(juz)
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { onOpenJuz(juz) },
                    colors = CardDefaults.cardColors(containerColor = if (done) Color(0xFFE4EFE3) else P.Paper),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, if (done) P.EmeraldSoft else P.Gold.copy(alpha = 0.5f))
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(modifier = Modifier.size(32.dp), shape = CircleShape, color = if (done) P.EmeraldSoft else P.Emerald) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("#${idx + 1}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("$juz. Cüz", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = P.Ink)
                            Text("Sayfa ${range.first} – ${range.second}", fontSize = 11.sp, color = P.InkSoft)
                            Text(
                                text = if (!person.isNullOrBlank()) "👤 $person" else "🟩 [Boşta - Dokun & Kişi Ata]",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (!person.isNullOrBlank()) P.GoldDeep else P.EmeraldSoft,
                                modifier = Modifier.clickable {
                                    editingJuz = juz
                                    personInput = person ?: ""
                                }
                            )
                        }
                        Checkbox(
                            checked = done,
                            onCheckedChange = { Quran1HatimStore.toggleCompleted(juz) },
                            colors = CheckboxDefaults.colors(checkedColor = P.EmeraldSoft)
                        )
                        TextButton(onClick = { onOpenJuz(juz) }, contentPadding = PaddingValues(horizontal = 8.dp)) {
                            Text("OKU", fontWeight = FontWeight.Bold, color = P.Emerald)
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}
