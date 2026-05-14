package com.example.mkat_nur.ui.settings

import android.app.Activity
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mkat_nur.util.AppConfig
import com.example.mkat_nur.viewmodel.PrayerViewModel

@Composable
fun SettingsScreen(viewModel: PrayerViewModel) {
    val isDarkModeState by viewModel.isDarkMode.collectAsState()
    val reminderMinutes by viewModel.reminderMinutes.collectAsState()
    val fontSize by viewModel.fontSize.collectAsState()
    val timeOffset by viewModel.timeOffset.collectAsState()
    val notificationSoundUri by viewModel.notificationSoundUri.collectAsState()
    val highlightColorInt by viewModel.highlightColor.collectAsState()
    val slidingDuration by viewModel.slidingDuration.collectAsState()
    val showAyet by viewModel.showAyet.collectAsState()
    val showHadis by viewModel.showHadis.collectAsState()
    val showVecize by viewModel.showVecize.collectAsState()
    val showEsma by viewModel.showEsma.collectAsState()
    val autoLocationInterval by viewModel.autoLocationInterval.collectAsState()
    val isWomenSpecial by viewModel.isWomenSpecial.collectAsState()
    val widgetTransparency by viewModel.widgetTransparency.collectAsState()
    val widgetTitleColor by viewModel.widgetTitleColor.collectAsState()
    val widgetTextColor by viewModel.widgetTextColor.collectAsState()
    
    val context = LocalContext.current
    
    // Vakit bildirim durumları
    val notifyImsak by viewModel.notifyImsak.collectAsState()
    val notifySunrise by viewModel.notifySunrise.collectAsState()
    val notifyDhuhr by viewModel.notifyDhuhr.collectAsState()
    val notifyAsr by viewModel.notifyAsr.collectAsState()
    val notifyMaghrib by viewModel.notifyMaghrib.collectAsState()
    val notifyIsha by viewModel.notifyIsha.collectAsState()
    val notifyKerahat by viewModel.notifyKerahat.collectAsState()

    val isInDarkMode = isDarkModeState ?: false
    var isAppearanceExpanded by remember { mutableStateOf(false) }
    var isNotifExpanded by remember { mutableStateOf(false) }
    var isOffsetExpanded by remember { mutableStateOf(false) }
    var isCardExpanded by remember { mutableStateOf(false) }
    var isSpecialExpanded by remember { mutableStateOf(false) }

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

            // GÖRÜNÜM AYARLARI (AÇILIR-KAPANIR)
            SettingsCard(
                title = "Görünüm Ayarları", 
                icon = Icons.Default.Palette,
                isExpandable = true,
                isExpanded = isAppearanceExpanded,
                onExpandClick = { isAppearanceExpanded = !isAppearanceExpanded }
            ) {
                AnimatedVisibility(visible = isAppearanceExpanded, enter = expandVertically(), exit = shrinkVertically()) {
                    Column {
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
                        Text("Widget Şeffaflığı: %${(widgetTransparency * 100).toInt()}", color = Color.White, fontSize = 14.sp)
                        Slider(
                            value = widgetTransparency,
                            onValueChange = { viewModel.setWidgetTransparency(it) },
                            valueRange = 0f..1f,
                            colors = SliderDefaults.colors(thumbColor = Color(0xFFFF9800), activeTrackColor = Color(0xFFFF9800))
                        )

                        Spacer(Modifier.height(16.dp))
                        Text("Widget Başlık Rengi:", color = Color.White, fontSize = 14.sp)
                        Spacer(Modifier.height(8.dp))
                        val widgetColorOptions = listOf(
                            0xFFFFD700.toInt(), 0xFFFFFFFF.toInt(), 0xFF000000.toInt(), 
                            0xFFF44336.toInt(), 0xFFE91E63.toInt(), 0xFF9C27B0.toInt(), 
                            0xFF673AB7.toInt(), 0xFF3F51B5.toInt(), 0xFF2196F3.toInt(), 
                            0xFF03A9F4.toInt(), 0xFF00BCD4.toInt(), 0xFF009688.toInt(), 
                            0xFF4CAF50.toInt(), 0xFF8BC34A.toInt(), 0xFFFFEB3B.toInt(), 
                            0xFFFFC107.toInt(), 0xFFFF9800.toInt(), 0xFF795548.toInt()
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            widgetColorOptions.forEach { colorInt ->
                                val isSelected = widgetTitleColor == colorInt
                                Box(
                                    modifier = Modifier.size(34.dp).background(Color(colorInt), CircleShape)
                                        .clickable { viewModel.setWidgetTitleColor(colorInt) }.padding(4.dp)
                                ) {
                                    if (isSelected) Icon(
                                        Icons.Default.Check, 
                                        null, 
                                        tint = if (colorInt == 0xFFFFFFFF.toInt() || colorInt == 0xFFFFEB3B.toInt()) Color.Black else Color.White, 
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))
                        Text("Widget Metin Rengi:", color = Color.White, fontSize = 14.sp)
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            widgetColorOptions.forEach { colorInt ->
                                val isSelected = widgetTextColor == colorInt
                                Box(
                                    modifier = Modifier.size(34.dp).background(Color(colorInt), CircleShape)
                                        .clickable { viewModel.setWidgetTextColor(colorInt) }.padding(4.dp)
                                ) {
                                    if (isSelected) Icon(
                                        Icons.Default.Check, 
                                        null, 
                                        tint = if (colorInt == 0xFFFFFFFF.toInt() || colorInt == 0xFFFFEB3B.toInt()) Color.Black else Color.White, 
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))
                        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                        Spacer(Modifier.height(16.dp))

                        Text("Uygulama Vurgu Rengi:", color = Color.White, fontSize = 14.sp)
                        Spacer(Modifier.height(8.dp))
                        val allColorOptions = listOf(
                            0xFFFFD700.toInt(), 0xFFFFFFFF.toInt(), 0xFF000000.toInt(), 
                            0xFFF44336.toInt(), 0xFFE91E63.toInt(), 0xFF9C27B0.toInt(), 
                            0xFF673AB7.toInt(), 0xFF3F51B5.toInt(), 0xFF2196F3.toInt(), 
                            0xFF03A9F4.toInt(), 0xFF00BCD4.toInt(), 0xFF009688.toInt(), 
                            0xFF4CAF50.toInt(), 0xFF8BC34A.toInt(), 0xFFFFEB3B.toInt(), 
                            0xFFFFC107.toInt(), 0xFFFF9800.toInt(), 0xFF795548.toInt()
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            allColorOptions.forEach { colorInt ->
                                val isSelected = highlightColorInt == colorInt
                                Box(
                                    modifier = Modifier.size(38.dp).background(Color(colorInt), CircleShape)
                                        .clickable { viewModel.setHighlightColor(colorInt) }.padding(4.dp)
                                ) {
                                    if (isSelected) Icon(
                                        Icons.Default.Check, 
                                        null, 
                                        tint = if (colorInt == 0xFFFFFFFF.toInt() || colorInt == 0xFFFFEB3B.toInt()) Color.Black else Color.White, 
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // BİLDİRİM VE SES AYARLARI (AÇILIR-KAPANIR)
            SettingsCard(
                title = "Bildirim ve Ses Ayarları", 
                icon = Icons.Default.NotificationsActive,
                isExpandable = true,
                isExpanded = isNotifExpanded,
                onExpandClick = { isNotifExpanded = !isNotifExpanded }
            ) {
                AnimatedVisibility(visible = isNotifExpanded, enter = expandVertically(), exit = shrinkVertically()) {
                    Column {
                        Text("Vakit Hatırlatıcı (dk):", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            listOf(0, 15, 30, 45).forEach { mins ->
                                FilterChip(
                                    selected = reminderMinutes.contains(mins),
                                    onClick = { viewModel.toggleReminderMinute(mins) },
                                    label = { Text(if (mins == 0) "Vaktinde" else "$mins dk", color = Color.White) },
                                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFFFF9800))
                                )
                            }
                        }
                        
                        Spacer(Modifier.height(16.dp))
                        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                        Spacer(Modifier.height(16.dp))
                        
                        Text("Vakit Bildirimleri:", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                        val prayerNotifs = listOf(
                            "İmsak" to notifyImsak,
                            "Güneş" to notifySunrise,
                            "Öğle" to notifyDhuhr,
                            "İkindi" to notifyAsr,
                            "Akşam" to notifyMaghrib,
                            "Yatsı" to notifyIsha,
                            "Kerahat" to notifyKerahat
                        )
                        
                        val allEnabled = prayerNotifs.all { it.second }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Tümünü Seç/Kaldır", color = Color(0xFFFF9800), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Switch(
                                checked = allEnabled,
                                onCheckedChange = { viewModel.toggleAllNotifications(it) },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFFFF9800))
                            )
                        }
                        HorizontalDivider(color = Color.White.copy(alpha = 0.05f), modifier = Modifier.padding(vertical = 4.dp))
                        
                        prayerNotifs.forEach { (name, isEnabled) ->
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(name, color = Color.White, fontSize = 14.sp)
                                Switch(
                                    checked = isEnabled,
                                    onCheckedChange = { viewModel.toggleNotification(name, it) },
                                    colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFFFF9800))
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
                            Text(if (notificationSoundUri == null) "Varsayılan Bildirim Sesi" else "Sesi Değiştir", color = Color.White)
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // İÇERİK KARTI AYARLARI (AÇILIR-KAPANIR)
            SettingsCard(
                title = "İçerik Kartı Ayarları",
                icon = Icons.Default.ViewCarousel,
                isExpandable = true,
                isExpanded = isCardExpanded,
                onExpandClick = { isCardExpanded = !isCardExpanded }
            ) {
                AnimatedVisibility(visible = isCardExpanded, enter = expandVertically(), exit = shrinkVertically()) {
                    Column {
                        Text("Kayma Süresi: ${slidingDuration.toInt()} sn", color = Color.White, fontSize = 14.sp)
                        Slider(
                            value = slidingDuration,
                            onValueChange = { viewModel.setSlidingDuration(it) },
                            valueRange = 1f..10f,
                            steps = 9,
                            colors = SliderDefaults.colors(thumbColor = Color(0xFFFF9800), activeTrackColor = Color(0xFFFF9800))
                        )

                        Spacer(Modifier.height(8.dp))
                        Text("Gösterilecek İçerikler:", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                        
                        val contentTypes = listOf("Ayet" to showAyet, "Hadis" to showHadis, "Vecize" to showVecize, "Esma" to showEsma)
                        contentTypes.forEach { (name, isEnabled) ->
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(name, color = Color.White, fontSize = 14.sp)
                                Checkbox(checked = isEnabled, onCheckedChange = { viewModel.toggleContentType(name, it) }, colors = CheckboxDefaults.colors(checkedColor = Color(0xFFFF9800)))
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ÖZEL AYARLAR
            SettingsCard(
                title = "Özel Ayarlar",
                icon = Icons.Default.AutoAwesome,
                isExpandable = true,
                isExpanded = isSpecialExpanded,
                onExpandClick = { isSpecialExpanded = !isSpecialExpanded }
            ) {
                AnimatedVisibility(
                    visible = isSpecialExpanded,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Kadın Özel Modu",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                "Özel günlerde bildirimleri ve vakit takiplerini düzenler.",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 12.sp
                            )
                        }
                        Switch(
                            checked = isWomenSpecial,
                            onCheckedChange = { viewModel.toggleWomenSpecial(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFFFF9800))
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            SettingsCard(
                title = "Vakit Düzeltme", 
                icon = Icons.Default.Update,
                isExpandable = true,
                isExpanded = isOffsetExpanded,
                onExpandClick = { isOffsetExpanded = !isOffsetExpanded }
            ) {
                AnimatedVisibility(visible = isOffsetExpanded, enter = expandVertically(), exit = shrinkVertically()) {
                    Column {
                        val imsakOffset by viewModel.imsakOffset.collectAsState()
                        val sunriseOffset by viewModel.sunriseOffset.collectAsState()
                        val dhuhrOffset by viewModel.dhuhrOffset.collectAsState()
                        val asrOffset by viewModel.asrOffset.collectAsState()
                        val maghribOffset by viewModel.maghribOffset.collectAsState()
                        val ishaOffset by viewModel.ishaOffset.collectAsState()

                        Text("Genel Kaydırma (dk):", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                            IconButton(onClick = { viewModel.setTimeOffset(timeOffset - 1) }) { Icon(Icons.Default.Remove, null, tint = Color.White) }
                            Text("$timeOffset", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            IconButton(onClick = { viewModel.setTimeOffset(timeOffset + 1) }) { Icon(Icons.Default.Add, null, tint = Color.White) }
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.White.copy(alpha = 0.1f))
                        
                        val prayerOffsets = listOf("İmsak" to imsakOffset, "Güneş" to sunriseOffset, "Öğle" to dhuhrOffset, "İkindi" to asrOffset, "Akşam" to maghribOffset, "Yatsı" to ishaOffset)
                        prayerOffsets.forEach { (name, offset) ->
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(name, color = Color.White, fontSize = 14.sp)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = { viewModel.setPrayerOffset(name, offset - 1) }, modifier = Modifier.size(28.dp)) { Icon(Icons.Default.Remove, null, tint = Color.White, modifier = Modifier.size(14.dp)) }
                                    Text("${if (offset > 0) "+" else ""}$offset", color = Color(0xFFFF9800), fontSize = 14.sp, modifier = Modifier.padding(horizontal = 8.dp))
                                    IconButton(onClick = { viewModel.setPrayerOffset(name, offset + 1) }, modifier = Modifier.size(28.dp)) { Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(14.dp)) }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // SİSTEM
            SettingsCard(title = "Sistem", icon = Icons.Default.Settings) {
                Text("Otomatik Konum Güncelleme:", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                val intervals = listOf(0 to "Kapalı", 1 to "1 Sa", 6 to "6 Sa", 12 to "12 Sa", 24 to "24 Sa")
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    intervals.forEach { (valHrs, label) ->
                        FilterChip(
                            selected = autoLocationInterval == valHrs,
                            onClick = { viewModel.setAutoLocationInterval(valHrs) },
                            label = { Text(label, color = Color.White, fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF4CAF50))
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
                Button(onClick = { viewModel.refreshLocation() }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))) {
                    Icon(Icons.Default.MyLocation, null, tint = Color.White); Spacer(Modifier.width(8.dp)); Text("Konumu Güncelle", color = Color.White)
                }
                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                Spacer(Modifier.height(16.dp))
                
                InfoRow(label = "Proje Adı", value = AppConfig.PROJECT_NAME)
                InfoRow(label = "Versiyon", value = "v${AppConfig.VERSION_NAME} (${AppConfig.VERSION_CODE})")
                InfoRow(label = "Geliştirici", value = AppConfig.DEVELOPER)
                InfoRow(label = "Yapım Yılı", value = AppConfig.BUILD_DATE)
                InfoRow(label = "Son Güncelleme", value = AppConfig.getAppLastUpdateTime(context))
            }
            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
fun SettingsCard(
    title: String, 
    icon: ImageVector, 
    isExpandable: Boolean = false,
    isExpanded: Boolean = false,
    onExpandClick: () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().then(if(isExpandable) Modifier.clickable { onExpandClick() } else Modifier),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = Color(0xFFFF9800), modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(12.dp))
                Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.weight(1f))
                if (isExpandable) {
                    Icon(
                        if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        null, tint = Color.White.copy(alpha = 0.5f)
                    )
                }
            }
            if (!isExpandable || isExpanded) {
                Spacer(Modifier.height(16.dp))
                content()
            }
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
