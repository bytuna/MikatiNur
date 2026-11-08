package com.example.mkat_nur.ui.settings

import android.media.MediaPlayer
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.mkat_nur.R
import com.example.mkat_nur.ui.theme.LocalAppThemeColors
import com.example.mkat_nur.viewmodel.PrayerViewModel

/**
 * Vakit bazlı ezan / makam ayarları. Ayarlar ekranında yer kaplamasın diye ayrı pencerede açılır.
 * Pencere kapanınca çalan ezan örneği de durdurulur.
 */
@Composable
fun EzanMakamDialog(viewModel: PrayerViewModel, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val themeColors = LocalAppThemeColors.current

    var activeAudioPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var playingResId by remember { mutableStateOf<Int?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            try {
                activeAudioPlayer?.stop()
                activeAudioPlayer?.release()
            } catch (_: Exception) { }
            activeAudioPlayer = null
        }
    }

    // ezan_bilgileri.txt dosyasından bilgileri oku
    val ezanMakamList = remember(context) {
        val list = mutableListOf<EzanMakamItem>()
        try {
            val lines = context.resources.openRawResource(R.raw.ezan_bilgileri).bufferedReader().use { it.readLines() }
            val resMap = mapOf(
                "sabah" to R.raw.sabah_ezani,
                "ogle" to R.raw.ogle_ezani,
                "ikindi" to R.raw.ikindi_ezani,
                "aksam" to R.raw.aksam_ezani,
                "yatsi" to R.raw.yatsi_ezani
            )
            val keys = listOf("sabah", "ogle", "ikindi", "aksam", "yatsi")
            lines.forEachIndexed { index, line ->
                val parts = line.split(",").map { it.trim() }
                if (parts.size >= 3 && index < keys.size) {
                    val key = keys[index]
                    list.add(EzanMakamItem(key, parts[0], parts[1], parts[2], resMap[key] ?: R.raw.sabah_ezani))
                }
            }
        } catch (_: Exception) { }

        if (list.isEmpty()) {
            list.addAll(
                listOf(
                    EzanMakamItem("sabah", "Sabah Ezanı", "Sabâ Makamı", "Mekke-i Mükerreme Baş Müezzini", R.raw.sabah_ezani),
                    EzanMakamItem("ogle", "Öğle Ezanı", "Râst Makamı", "Mescid-i Nebevi Müezzini", R.raw.ogle_ezani),
                    EzanMakamItem("ikindi", "İkindi Ezanı", "Hicâz Makamı", "İstanbul Selatin Cami Müezzini", R.raw.ikindi_ezani),
                    EzanMakamItem("aksam", "Akşam Ezanı", "Segâh Makamı", "Kudüs Mescid-i Aksa Müezzini", R.raw.aksam_ezani),
                    EzanMakamItem("yatsi", "Yatsı Ezanı", "Uşşâk Makamı", "Kahire El-Ezher Müezzini", R.raw.yatsi_ezani)
                )
            )
        }
        list
    }

    val prayerSoundModes by viewModel.prayerSoundModes.collectAsState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.94f).fillMaxHeight(0.88f),
            shape = RoundedCornerShape(24.dp),
            color = themeColors.dialogSurface,
            border = BorderStroke(1.dp, themeColors.cardBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Vakit Bazlı Ezan ve Makam", color = themeColors.textPrimary, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        Text(
                            "Ezanları makam bilgileriyle dinleyin, her vakit için ses modunu seçin.",
                            color = themeColors.textSecondary, fontSize = 11.5.sp, modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, "Kapat", tint = themeColors.textPrimary)
                    }
                }

                Spacer(Modifier.height(10.dp))

                Column(
                    modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ezanMakamList.forEach { item ->
                        val currentMode = prayerSoundModes[item.prayerKey] ?: viewModel.getPrayerSoundMode(item.prayerKey)
                        val isPlaying = playingResId == item.resId

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = themeColors.background.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, themeColors.cardBorder)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(item.prayerName, color = themeColors.textPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Spacer(Modifier.width(8.dp))
                                            Surface(
                                                color = Color(0xFF4CAF50).copy(alpha = 0.18f),
                                                shape = RoundedCornerShape(6.dp),
                                                border = BorderStroke(1.dp, Color(0xFF4CAF50))
                                            ) {
                                                Text(
                                                    text = item.makam,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                    color = Color(0xFF4CAF50), fontSize = 10.5.sp, fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                        Text(item.muezzin, color = themeColors.textSecondary, fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
                                    }

                                    IconButton(
                                        onClick = {
                                            try {
                                                if (isPlaying) {
                                                    activeAudioPlayer?.stop()
                                                    activeAudioPlayer?.release()
                                                    activeAudioPlayer = null
                                                    playingResId = null
                                                } else {
                                                    activeAudioPlayer?.stop()
                                                    activeAudioPlayer?.release()
                                                    activeAudioPlayer = MediaPlayer.create(context, item.resId)
                                                    activeAudioPlayer?.setOnCompletionListener { playingResId = null }
                                                    activeAudioPlayer?.start()
                                                    playingResId = item.resId
                                                }
                                            } catch (_: Exception) { }
                                        },
                                        modifier = Modifier.size(38.dp).background(
                                            if (isPlaying) Color(0xFFF44336) else Color(0xFF4CAF50), CircleShape
                                        )
                                    ) {
                                        Icon(
                                            imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                                            contentDescription = if (isPlaying) "Durdur" else "Oynat",
                                            tint = Color.White, modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                Spacer(Modifier.height(10.dp))
                                HorizontalDivider(color = themeColors.cardBorder.copy(alpha = 0.5f))
                                Spacer(Modifier.height(8.dp))

                                Text("Vakit Ses Modu:", color = themeColors.textSecondary, fontSize = 11.sp)
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    listOf("ezan" to "Ezan Oku", "system" to "Sistem Sesi", "silent" to "Sessiz").forEach { (mKey, mLabel) ->
                                        FilterChip(
                                            selected = currentMode == mKey,
                                            onClick = { viewModel.setPrayerSoundMode(item.prayerKey, mKey) },
                                            label = { Text(mLabel, fontSize = 10.5.sp, fontWeight = FontWeight.Medium) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = Color(0xFF4CAF50),
                                                selectedLabelColor = Color.White,
                                                labelColor = themeColors.textPrimary
                                            ),
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { viewModel.resetPrayerSoundModes() },
                        modifier = Modifier.weight(1f)
                    ) { Text("Hepsini Sistem Sesi Yap", fontSize = 12.sp, color = themeColors.textPrimary) }
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) { Text("Tamam", color = Color.White) }
                }
            }
        }
    }
}
