package com.example.mkat_nur.ui.settings

import android.app.Activity
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mkat_nur.viewmodel.PrayerViewModel

@Composable
fun SettingsScreen(viewModel: PrayerViewModel) {
    val context = LocalContext.current
    val isDarkModeState by viewModel.isDarkMode.collectAsState()
    val reminderMinutes by viewModel.reminderMinutes.collectAsState()
    val fontSize by viewModel.fontSize.collectAsState()
    val dailyContentType by viewModel.dailyContentType.collectAsState()
    val timeOffset by viewModel.timeOffset.collectAsState()
    val notificationSoundUri by viewModel.notificationSoundUri.collectAsState()
    val highlightColorInt by viewModel.highlightColor.collectAsState()

    val isInDarkMode = isDarkModeState ?: false

    // Ses seçici başlatıcısı
    val soundPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.getParcelableExtra<Uri>(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            viewModel.setNotificationSound(uri?.toString())
        }
    }

    val bgColors = if (isInDarkMode) listOf(Color(0xFF121212), Color(0xFF1E1E1E))
    else listOf(Color(0xFF023E8A), Color(0xFF0077B6))

    Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(bgColors))) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("AYARLAR", fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color.White, modifier = Modifier.padding(vertical = 20.dp))

            // GÖRÜNÜM AYARLARI
            SettingsCard(title = "Görünüm", icon = Icons.Default.Palette) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Koyu Mod", color = Color.White)
                    Switch(checked = isInDarkMode, onCheckedChange = { viewModel.toggleDarkMode(it) })
                }
                Spacer(Modifier.height(16.dp))
                Text("Yazı Boyutu: ${fontSize.toInt()}sp", color = Color.White, fontSize = 14.sp)
                Slider(
                    value = fontSize,
                    onValueChange = { viewModel.setFontSize(it) },
                    valueRange = 12f..24f,
                    steps = 6,
                    colors = SliderDefaults.colors(thumbColor = Color(0xFFFF9800), activeTrackColor = Color(0xFFFF9800))
                )

                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                Spacer(Modifier.height(16.dp))

                Text("Bildirim Vurgu Rengi:", color = Color.White, fontSize = 14.sp)
                Spacer(Modifier.height(8.dp))
                val colorOptions = listOf(
                    0xFFFF9800.toInt(), // Turuncu (Varsayılan)
                    0xFF4CAF50.toInt(), // Yeşil
                    0xFF2196F3.toInt(), // Mavi
                    0xFFE91E63.toInt(), // Pembe
                    0xFF9C27B0.toInt(), // Mor
                    0xFFF44336.toInt()  // Kırmızı
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    colorOptions.forEach { colorInt ->
                        val isSelected = highlightColorInt == colorInt
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(colorInt), CircleShape)
                                .clickable { viewModel.setHighlightColor(colorInt) }
                                .padding(4.dp)
                        ) {
                            if (isSelected) {
                                Icon(
                                    Icons.Default.Check,
                                    null,
                                    tint = Color.White,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // HATIRLATICI VE SES AYARLARI
            SettingsCard(title = "Bildirim ve Ses", icon = Icons.Default.NotificationsActive) {
                Text("Vakit Hatırlatıcı (dk):", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    listOf(0, 15, 30, 45).forEach { mins ->
                        FilterChip(
                            selected = reminderMinutes == mins,
                            onClick = { viewModel.setReminderMinutes(mins) },
                            label = { Text(if (mins == 0) "Vaktinde" else "$mins dk", color = Color.White) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFFFF9800))
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = {
                        val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                            putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION)
                            putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Bildirim Sesi Seç")
                            putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, notificationSoundUri?.let { Uri.parse(it) })
                        }
                        soundPickerLauncher.launch(intent)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f))
                ) {
                    Icon(Icons.Default.MusicNote, null, tint = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text(if (notificationSoundUri == null) "Varsayılan Ses" else "Sesi Değiştir", color = Color.White)
                }
            }

            Spacer(Modifier.height(16.dp))

            // VAKİT DÜZELTME
            val imsakOffset by viewModel.imsakOffset.collectAsState()
            val sunriseOffset by viewModel.sunriseOffset.collectAsState()
            val dhuhrOffset by viewModel.dhuhrOffset.collectAsState()
            val asrOffset by viewModel.asrOffset.collectAsState()
            val maghribOffset by viewModel.maghribOffset.collectAsState()
            val ishaOffset by viewModel.ishaOffset.collectAsState()

            SettingsCard(title = "Vakit Düzeltme", icon = Icons.Default.Update) {
                Text("Genel Kaydırma (Tüm Vakitler):", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                    IconButton(onClick = { viewModel.setTimeOffset(timeOffset - 1) }) { Icon(Icons.Default.Remove, null, tint = Color.White) }
                    Text("$timeOffset dk", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp))
                    IconButton(onClick = { viewModel.setTimeOffset(timeOffset + 1) }) { Icon(Icons.Default.Add, null, tint = Color.White) }
                }
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.White.copy(alpha = 0.1f))
                
                Text("Ayrı Ayrı Vakit Düzeltme:", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))
                
                val prayerOffsets = listOf(
                    "İmsak" to imsakOffset,
                    "Güneş" to sunriseOffset,
                    "Öğle" to dhuhrOffset,
                    "İkindi" to asrOffset,
                    "Akşam" to maghribOffset,
                    "Yatsı" to ishaOffset
                )
                
                prayerOffsets.forEach { (name, offset) ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(name, color = Color.White, fontSize = 14.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { viewModel.setPrayerOffset(name, offset - 1) }, modifier = Modifier.size(32.dp)) { 
                                Icon(Icons.Default.Remove, null, tint = Color.White, modifier = Modifier.size(16.dp)) 
                            }
                            Text("${if (offset > 0) "+" else ""}$offset", color = Color(0xFFFF9800), fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp))
                            IconButton(onClick = { viewModel.setPrayerOffset(name, offset + 1) }, modifier = Modifier.size(32.dp)) { 
                                Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(16.dp)) 
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ANA EKRAN İÇERİĞİ
            SettingsCard(title = "Günün İçeriği", icon = Icons.Default.MenuBook) {
                Text("Ana ekranda ne görünsün?", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                val contentTypes = listOf("Ayet", "Hadis", "Vecize")
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    contentTypes.forEach { type ->
                        FilterChip(
                            selected = dailyContentType == type,
                            onClick = { viewModel.setDailyContentType(type) },
                            label = { Text(type, color = Color.White) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFFFF9800))
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // KONUM VE BİLGİ
            SettingsCard(title = "Sistem", icon = Icons.Default.Settings) {
                Button(
                    onClick = { viewModel.refreshLocation() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) {
                    Icon(Icons.Default.MyLocation, null, tint = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text("Konumu Güncelle", color = Color.White)
                }
                Spacer(Modifier.height(16.dp))
                InfoRow(label = "Versiyon", value = "1.0.5")
                InfoRow(label = "Geliştirici", value = "Mîkat-ı Nur Team")
            }
            
            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
fun SettingsCard(title: String, icon: ImageVector, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = Color(0xFFFF9800), modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(12.dp))
                Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Spacer(Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Color.White.copy(alpha = 0.6f), fontSize = 14.sp)
        Text(value, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}
