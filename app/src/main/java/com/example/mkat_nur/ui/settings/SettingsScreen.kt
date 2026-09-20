package com.example.mkat_nur.ui.settings

import android.app.Activity
import android.content.Intent
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mkat_nur.R
import com.example.mkat_nur.util.AppConfig
import com.example.mkat_nur.viewmodel.PrayerViewModel
import com.example.mkat_nur.viewmodel.UpdateStatus

data class CustomSoundItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val resId: Int
)

data class EzanMakamItem(
    val prayerKey: String,
    val prayerName: String,
    val makam: String,
    val muezzin: String,
    val resId: Int
)

@OptIn(ExperimentalMaterial3Api::class)
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
    val widgetFontSize by viewModel.widgetFontSize.collectAsState()
    
    val updateStatus by viewModel.updateStatus.collectAsState()

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.checkForUpdates()
    }
    
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
    var isWidgetExpanded by remember { mutableStateOf(false) }
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

    val themeColors = com.example.mkat_nur.ui.theme.LocalAppThemeColors.current
    val bgColors = themeColors.gradientColors

    Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(bgColors))) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("AYARLAR", fontSize = 24.sp, fontWeight = FontWeight.Black, color = themeColors.textPrimary, modifier = Modifier.padding(vertical = 20.dp))

            // GÜNCELLEME KARTI
            SettingsCard(
                title = "Uygulama Güncelleme",
                icon = Icons.Default.SystemUpdate,
                isExpandable = false
            ) {
                Column {
                    when (val status = updateStatus) {
                        is UpdateStatus.Idle -> {
                            Text("Güncellemeler kontrol edilmedi.", color = themeColors.textSecondary, fontSize = 14.sp)
                        }
                        is UpdateStatus.Checking -> {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color(0xFF4CAF50), strokeWidth = 2.dp)
                                Spacer(Modifier.width(8.dp))
                                Text("Kontrol ediliyor...", color = themeColors.textSecondary, fontSize = 14.sp)
                            }
                        }
                        is UpdateStatus.UpToDate -> {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Uygulama güncel.", color = Color(0xFF4CAF50), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        is UpdateStatus.UpdateAvailable -> {
                            val release = status.release
                            Text("Yeni Sürüm: ${release.tagName}", color = themeColors.textPrimary, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(4.dp))
                            Text(release.body, color = themeColors.textSecondary, fontSize = 12.sp, maxLines = 3)
                            Spacer(Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    val downloadUrl = release.htmlUrl.ifEmpty { AppConfig.DOWNLOAD_URL }
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrl))
                                    context.startActivity(intent)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                            ) {
                                Icon(Icons.Default.Download, null, tint = Color.White)
                                Spacer(Modifier.width(8.dp))
                                Text("Şimdi Güncelle", color = Color.White)
                            }
                        }
                        is UpdateStatus.Error -> {
                            Text("Hata: ${status.message}", color = Color(0xFFF44336), fontSize = 12.sp)
                        }
                    }

                    if (updateStatus !is UpdateStatus.Checking && updateStatus !is UpdateStatus.UpdateAvailable) {
                        Spacer(Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = { viewModel.checkForUpdates() },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = themeColors.textPrimary),
                            border = ButtonDefaults.outlinedButtonBorder.copy(brush = SolidColor(themeColors.cardBorder))
                        ) {
                            Icon(Icons.Default.Refresh, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Güncellemeleri Denetle")
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))

            // GÖRÜNÜM AYARLARI (AÇILIR-KAPANIR)
            SettingsCard(
                title = "Görünüm ve Tema Ayarları", 
                icon = Icons.Default.Palette,
                isExpandable = true,
                isExpanded = isAppearanceExpanded,
                onExpandClick = { isAppearanceExpanded = !isAppearanceExpanded }
            ) {
                AnimatedVisibility(visible = isAppearanceExpanded, enter = expandVertically(), exit = shrinkVertically()) {
                    Column {
                        Text("Tema Modu:", color = themeColors.textSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val modes = listOf(
                                null to "Otomatik (Sistem)",
                                false to "Açık Mod",
                                true to "Koyu Mod"
                            )
                            modes.forEach { (modeVal, label) ->
                                FilterChip(
                                    selected = isDarkModeState == modeVal,
                                    onClick = { viewModel.toggleDarkMode(modeVal) },
                                    label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFF4CAF50),
                                        selectedLabelColor = Color.White,
                                        labelColor = themeColors.textPrimary
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        Spacer(Modifier.height(14.dp))
                        Text("Yazı Boyutu: ${fontSize.toInt()}sp", color = themeColors.textPrimary, fontSize = 14.sp)
                        Slider(
                            value = fontSize,
                            onValueChange = { viewModel.setFontSize(it) },
                            valueRange = 12f..24f,
                            steps = 6,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFF4CAF50),
                                activeTrackColor = Color(0xFF4CAF50),
                                inactiveTrackColor = themeColors.cardBorder
                            )
                        )

                        Spacer(Modifier.height(16.dp))
                        HorizontalDivider(color = themeColors.cardBorder)
                        Spacer(Modifier.height(16.dp))

                        // TEMA PALETİ SEÇİMİ
                        val currentThemeKey by viewModel.appThemeKey.collectAsState()
                        val appThemes = com.example.mkat_nur.ui.theme.AppTheme.entries

                        Text("Tema ve Renk Paleti:", color = themeColors.textPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(10.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            appThemes.forEach { appTheme ->
                                val isSelected = currentThemeKey == appTheme.key
                                val previewColors = appTheme.getActiveColors(isInDarkMode)

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) Color(0xFF4CAF50) else themeColors.cardBorder,
                                            shape = RoundedCornerShape(14.dp)
                                        )
                                        .clickable { viewModel.setAppThemeKey(appTheme.key) },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) Color(0xFF4CAF50).copy(0.12f) else themeColors.background.copy(0.5f)
                                    ),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy((-6).dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(22.dp)
                                                    .background(previewColors.background, CircleShape)
                                                    .border(1.dp, Color.White.copy(0.4f), CircleShape)
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .size(22.dp)
                                                    .background(previewColors.primary, CircleShape)
                                                    .border(1.dp, Color.White.copy(0.4f), CircleShape)
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .size(22.dp)
                                                    .background(previewColors.accent, CircleShape)
                                                    .border(1.dp, Color.White.copy(0.4f), CircleShape)
                                            )
                                        }

                                        Spacer(Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = appTheme.title,
                                                color = themeColors.textPrimary,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.5.sp
                                            )
                                            Text(
                                                text = appTheme.subtitle,
                                                color = themeColors.textSecondary,
                                                fontSize = 10.5.sp,
                                                lineHeight = 14.sp
                                            )
                                        }

                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = Color(0xFF4CAF50),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))
                        HorizontalDivider(color = themeColors.cardBorder)
                        Spacer(Modifier.height(16.dp))

                        Text("Bildirim Paneli Vurgu Rengi:", color = themeColors.textPrimary, fontSize = 14.sp)
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

            // WIDGET AYARLARI (AÇILIR-KAPANIR)
            SettingsCard(
                title = "Widget Ayarları",
                icon = Icons.Default.Widgets,
                isExpandable = true,
                isExpanded = isWidgetExpanded,
                onExpandClick = { isWidgetExpanded = !isWidgetExpanded }
            ) {
                AnimatedVisibility(visible = isWidgetExpanded, enter = expandVertically(), exit = shrinkVertically()) {
                    Column {
                        Text("Widget Şeffaflığı: %${(widgetTransparency * 100).toInt()}", color = themeColors.textPrimary, fontSize = 14.sp)
                        Slider(
                            value = widgetTransparency,
                            onValueChange = { viewModel.setWidgetTransparency(it) },
                            valueRange = 0f..1f,
                            colors = SliderDefaults.colors(thumbColor = Color(0xFF4CAF50), activeTrackColor = Color(0xFF4CAF50), inactiveTrackColor = themeColors.cardBorder)
                        )

                        Spacer(Modifier.height(16.dp))
                        Text("Widget Başlık Rengi:", color = themeColors.textPrimary, fontSize = 14.sp)
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
                        Text("Widget Metin Rengi:", color = themeColors.textPrimary, fontSize = 14.sp)
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
                        Text("Widget Yazı Boyutu:", color = themeColors.textPrimary, fontSize = 14.sp)
                        Spacer(Modifier.height(8.dp))
                        var isFontSizeExpanded by remember { mutableStateOf(false) }
                        val fontSizeOptions = listOf(
                            10f to "Çok Küçük",
                            12f to "Küçük",
                            14f to "Normal",
                            16f to "Büyük",
                            18f to "Çok Büyük"
                        )
                        val currentSizeLabel = fontSizeOptions.find { it.first == widgetFontSize }?.second ?: "Özel"

                        Box {
                            Button(
                                onClick = { isFontSizeExpanded = true },
                                colors = ButtonDefaults.buttonColors(containerColor = themeColors.background),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(currentSizeLabel, color = themeColors.textPrimary)
                                    Icon(Icons.Default.ArrowDropDown, null, tint = themeColors.textPrimary)
                                }
                            }
                            DropdownMenu(
                                expanded = isFontSizeExpanded,
                                onDismissRequest = { isFontSizeExpanded = false },
                                modifier = Modifier.background(themeColors.surface).fillMaxWidth(0.8f)
                            ) {
                                fontSizeOptions.forEach { (size, label) ->
                                    DropdownMenuItem(
                                        text = { Text(label, color = themeColors.textPrimary) },
                                        onClick = {
                                            viewModel.setWidgetFontSize(size)
                                            isFontSizeExpanded = false
                                        }
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
                        Text("Vakit Hatırlatıcı Zamanlaması:", color = themeColors.textSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("Vaktinde (0 dk) bildirimleri aşağıdaki vakit ses moduna göre (Ezan/Sistem/Sessiz) çalar. 15, 30, 45 dk erken bildirimler ise sadece kısa sistem bildirim sesi çalar.", color = themeColors.textSecondary.copy(alpha = 0.85f), fontSize = 10.5.sp, modifier = Modifier.padding(top = 2.dp, bottom = 6.dp), lineHeight = 15.sp)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            listOf(0, 15, 30, 45).forEach { mins ->
                                FilterChip(
                                    selected = reminderMinutes.contains(mins),
                                    onClick = { viewModel.toggleReminderMinute(mins) },
                                    label = { Text(if (mins == 0) "Vaktinde (0 dk)" else "$mins dk önce", color = if (reminderMinutes.contains(mins)) Color.White else themeColors.textPrimary, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF4CAF50))
                                )
                            }
                        }
                        
                        Spacer(Modifier.height(16.dp))
                        HorizontalDivider(color = themeColors.cardBorder)
                        Spacer(Modifier.height(16.dp))
                        
                        Text("Vakit Bildirimleri:", color = themeColors.textSecondary, fontSize = 12.sp)
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
                            Text("Tümünü Seç/Kaldır", color = Color(0xFF4CAF50), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Switch(
                                checked = allEnabled,
                                onCheckedChange = { viewModel.toggleAllNotifications(it) },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF4CAF50))
                            )
                        }
                        HorizontalDivider(color = themeColors.cardBorder, modifier = Modifier.padding(vertical = 4.dp))
                        
                        prayerNotifs.forEach { (name, isEnabled) ->
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(name, color = themeColors.textPrimary, fontSize = 14.sp)
                                Switch(
                                    checked = isEnabled,
                                    onCheckedChange = { viewModel.toggleNotification(name, it) },
                                    colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF4CAF50))
                                )
                            }
                        }

                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = {
                                val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                                    putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION)
                                    putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Sistem Bildirim Sesi Seç")
                                    putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, notificationSoundUri?.let { Uri.parse(it) })
                                }
                                soundPickerLauncher.launch(intent)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                        ) {
                            Icon(Icons.Default.MusicNote, null, tint = Color.White)
                            Spacer(Modifier.width(8.dp))
                            Text(if (notificationSoundUri == null) "Sistem Bildirim Sesi Seç" else "Sistem Sesini Değiştir", color = Color.White)
                        }

                        Spacer(Modifier.height(16.dp))
                        HorizontalDivider(color = themeColors.cardBorder)
                        Spacer(Modifier.height(16.dp))

                        // VAKİT BAZLI EZAN VE MAKAM AYARLARI (5 VAKİT EZAN VE ezan_bilgileri.txt)
                        Text("Vakit Bazlı Ezan ve Makam Ayarları:", color = themeColors.textPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(4.dp))
                        Text("5 vakit ezan sesini makam bilgileriyle dinleyin ve vakitlere özel ses modlarını ayarlayın.", color = themeColors.textSecondary, fontSize = 11.5.sp)
                        Spacer(Modifier.height(10.dp))

                        var activeAudioPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
                        var playingResId by remember { mutableStateOf<Int?>(null) }

                        DisposableEffect(Unit) {
                            onDispose {
                                activeAudioPlayer?.stop()
                                activeAudioPlayer?.release()
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
                                        val resId = resMap[key] ?: R.raw.sabah_ezani
                                        list.add(EzanMakamItem(key, parts[0], parts[1], parts[2], resId))
                                    }
                                }
                            } catch (_: Exception) {}

                            if (list.isEmpty()) {
                                list.addAll(listOf(
                                    EzanMakamItem("sabah", "Sabah Ezanı", "Sabâ Makamı", "Mekke-i Mükerreme Baş Müezzini", R.raw.sabah_ezani),
                                    EzanMakamItem("ogle", "Öğle Ezanı", "Râst Makamı", "Mescid-i Nebevi Müezzini", R.raw.ogle_ezani),
                                    EzanMakamItem("ikindi", "İkindi Ezanı", "Hicâz Makamı", "İstanbul Selatin Cami Müezzini", R.raw.ikindi_ezani),
                                    EzanMakamItem("aksam", "Akşam Ezanı", "Segâh Makamı", "Kudüs Mescid-i Aksa Müezzini", R.raw.aksam_ezani),
                                    EzanMakamItem("yatsi", "Yatsı Ezanı", "Uşşâk Makamı", "Kahire El-Ezher Müezzini", R.raw.yatsi_ezani)
                                ))
                            }
                            list
                        }

                        val prayerSoundModes by viewModel.prayerSoundModes.collectAsState()

                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                                                            color = Color(0xFF4CAF50),
                                                            fontSize = 10.5.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                }
                                                Text(item.muezzin, color = themeColors.textSecondary, fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
                                            }

                                            // Start / Stop Oynat/Durdur Butonu
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
                                                            activeAudioPlayer?.setOnCompletionListener {
                                                                playingResId = null
                                                            }
                                                            activeAudioPlayer?.start()
                                                            playingResId = item.resId
                                                        }
                                                    } catch (_: Exception) {}
                                                },
                                                modifier = Modifier.size(38.dp).background(
                                                    if (isPlaying) Color(0xFFF44336) else Color(0xFF4CAF50),
                                                    CircleShape
                                                )
                                            ) {
                                                Icon(
                                                    imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                                                    contentDescription = if (isPlaying) "Durdur" else "Oynat",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(20.dp)
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
                                            val modes = listOf(
                                                "ezan" to "Ezan Oku",
                                                "system" to "Sistem Sesi",
                                                "silent" to "Sessiz"
                                            )
                                            modes.forEach { (mKey, mLabel) ->
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
                        Text("Kayma Süresi:", color = themeColors.textSecondary, fontSize = 12.sp)
                        val durations = listOf(5f to "5 Sn", 10f to "10 Sn", 15f to "15 Sn", 30f to "30 Sn")
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            durations.forEach { (duration, label) ->
                                FilterChip(
                                    selected = slidingDuration == duration,
                                    onClick = { viewModel.setSlidingDuration(duration) },
                                    label = { Text(label, color = if (slidingDuration == duration) Color.White else themeColors.textPrimary, fontSize = 10.sp) },
                                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF4CAF50))
                                )
                            }
                        }

                        Spacer(Modifier.height(8.dp))
                        Text("Gösterilecek İçerikler:", color = themeColors.textSecondary, fontSize = 12.sp)
                        
                        val contentTypes = listOf("Ayet" to showAyet, "Hadis" to showHadis, "Vecize" to showVecize, "Esma" to showEsma)
                        contentTypes.forEach { (name, isEnabled) ->
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(name, color = themeColors.textPrimary, fontSize = 14.sp)
                                Checkbox(checked = isEnabled, onCheckedChange = { viewModel.toggleContentType(name, it) }, colors = CheckboxDefaults.colors(checkedColor = Color(0xFF4CAF50)))
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // KADIN ÖZEL
            SettingsCard(
                title = "Kadın Özel",
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
                                color = themeColors.textPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                "Özel günlerde bildirimleri ve vakit takiplerini düzenler.",
                                color = themeColors.textSecondary,
                                fontSize = 12.sp
                            )
                        }
                        Switch(
                            checked = isWomenSpecial,
                            onCheckedChange = { viewModel.toggleWomenSpecial(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF4CAF50))
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

                        Text("Genel Kaydırma (dk):", color = themeColors.textSecondary, fontSize = 12.sp)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                            IconButton(onClick = { viewModel.setTimeOffset(timeOffset - 1) }) { Icon(Icons.Default.Remove, null, tint = themeColors.textPrimary) }
                            Text("$timeOffset", color = themeColors.textPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            IconButton(onClick = { viewModel.setTimeOffset(timeOffset + 1) }) { Icon(Icons.Default.Add, null, tint = themeColors.textPrimary) }
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = themeColors.cardBorder)
                        
                        val prayerOffsets = listOf("İmsak" to imsakOffset, "Güneş" to sunriseOffset, "Öğle" to dhuhrOffset, "İkindi" to asrOffset, "Akşam" to maghribOffset, "Yatsı" to ishaOffset)
                        prayerOffsets.forEach { (name, offset) ->
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(name, color = themeColors.textPrimary, fontSize = 14.sp)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = { viewModel.setPrayerOffset(name, offset - 1) }, modifier = Modifier.size(28.dp)) { Icon(Icons.Default.Remove, null, tint = themeColors.textPrimary, modifier = Modifier.size(14.dp)) }
                                    Text("${if (offset > 0) "+" else ""}$offset", color = Color(0xFF4CAF50), fontSize = 14.sp, modifier = Modifier.padding(horizontal = 8.dp))
                                    IconButton(onClick = { viewModel.setPrayerOffset(name, offset + 1) }, modifier = Modifier.size(28.dp)) { Icon(Icons.Default.Add, null, tint = themeColors.textPrimary, modifier = Modifier.size(14.dp)) }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // SİSTEM
            var isLegalExpanded by remember { mutableStateOf(false) }
            var legalDetailType by remember { mutableStateOf<String?>(null) }

            SettingsCard(title = "Sistem", icon = Icons.Default.Settings) {
                Text("Otomatik Konum Güncelleme:", color = themeColors.textSecondary, fontSize = 12.sp)
                val intervals = listOf(0 to "Kapalı", 1 to "1 Sa", 6 to "6 Sa", 12 to "12 Sa", 24 to "24 Sa")
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    intervals.forEach { (valHrs, label) ->
                        FilterChip(
                            selected = autoLocationInterval == valHrs,
                            onClick = { viewModel.setAutoLocationInterval(valHrs) },
                            label = { Text(label, color = if (autoLocationInterval == valHrs) Color.White else themeColors.textPrimary, fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF4CAF50))
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
                Button(onClick = { viewModel.refreshLocation() }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))) {
                    Icon(Icons.Default.MyLocation, null, tint = Color.White); Spacer(Modifier.width(8.dp)); Text("Konumu Güncelle", color = Color.White)
                }
                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = themeColors.cardBorder)
                Spacer(Modifier.height(16.dp))
                
                InfoRow(label = "Proje Adı", value = AppConfig.PROJECT_NAME)
                InfoRow(label = "Versiyon", value = "v${AppConfig.VERSION_NAME} (${AppConfig.VERSION_CODE})")
                InfoRow(label = "Geliştirici", value = AppConfig.DEVELOPER)
                InfoRow(label = "Web Sitesi", value = AppConfig.WEBSITE_NAME)
                InfoRow(label = "İletişim E-Posta", value = AppConfig.CONTACT_EMAIL)
                InfoRow(label = "Yapım Yılı", value = AppConfig.BUILD_DATE)
                InfoRow(label = "Son Güncelleme", value = AppConfig.getAppLastUpdateTime(context))
            }

            Spacer(Modifier.height(16.dp))

            // İLETİŞİM VE DESTEK
            var isContactExpanded by remember { mutableStateOf(false) }
            var showContactDialog by remember { mutableStateOf(false) }

            SettingsCard(
                title = "İletişim ve Destek",
                icon = Icons.Default.Email,
                isExpandable = true,
                isExpanded = isContactExpanded,
                onExpandClick = { isContactExpanded = !isContactExpanded }
            ) {
                AnimatedVisibility(visible = isContactExpanded, enter = expandVertically(), exit = shrinkVertically()) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Görüş, öneri veya destek taleplerinizi doğrudan geliştiriciye iletebilirsiniz.",
                            color = themeColors.textSecondary,
                            fontSize = 12.5.sp,
                            lineHeight = 18.sp
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(themeColors.background.copy(0.5f), RoundedCornerShape(12.dp))
                                .clickable {
                                    val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                                        data = Uri.parse("mailto:${AppConfig.CONTACT_EMAIL}")
                                        putExtra(Intent.EXTRA_SUBJECT, "Mîkat-ı Nur - İletişim / Destek (v${AppConfig.VERSION_NAME})")
                                    }
                                    try { context.startActivity(Intent.createChooser(emailIntent, "E-Posta Gönder")) } catch (_: Exception) {}
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Mail, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(22.dp))
                            Spacer(Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("E-Posta Adresi", color = themeColors.textSecondary, fontSize = 11.sp)
                                Text(AppConfig.CONTACT_EMAIL, color = themeColors.textPrimary, fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                            }
                            Icon(Icons.Default.OpenInNew, null, tint = themeColors.textSecondary, modifier = Modifier.size(18.dp))
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(themeColors.background.copy(0.5f), RoundedCornerShape(12.dp))
                                .clickable {
                                    val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(AppConfig.WEBSITE_URL))
                                    try { context.startActivity(webIntent) } catch (_: Exception) {}
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Language, null, tint = themeColors.secondary, modifier = Modifier.size(22.dp))
                            Spacer(Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Web Sitesi", color = themeColors.textSecondary, fontSize = 11.sp)
                                Text(AppConfig.WEBSITE_NAME, color = themeColors.textPrimary, fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                            }
                            Icon(Icons.Default.OpenInNew, null, tint = themeColors.textSecondary, modifier = Modifier.size(18.dp))
                        }

                        Button(
                            onClick = { showContactDialog = true },
                            modifier = Modifier.fillMaxWidth().height(46.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                        ) {
                            Icon(Icons.Default.Send, null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("İletişim Formunu Aç", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }

            if (showContactDialog) {
                ContactDialog(
                    onDismiss = { showContactDialog = false }
                )
            }

            Spacer(Modifier.height(16.dp))

            // GİZLİLİK VE KVKK
            SettingsCard(
                title = "Gizlilik ve Yasal",
                icon = Icons.Default.Gavel,
                isExpandable = true,
                isExpanded = isLegalExpanded,
                onExpandClick = { isLegalExpanded = !isLegalExpanded }
            ) {
                AnimatedVisibility(visible = isLegalExpanded, enter = expandVertically(), exit = shrinkVertically()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        val prefs = remember { context.getSharedPreferences("mkat_nur_prefs", android.content.Context.MODE_PRIVATE) }
                        val isKvkkAccepted = prefs.getBoolean("is_kvkk_accepted", false)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(themeColors.background.copy(0.5f), RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isKvkkAccepted) Icons.Default.VerifiedUser else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (isKvkkAccepted) Color(0xFF4CAF50) else Color(0xFF4CAF50),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("KVKK Onay Durumu", color = themeColors.textPrimary, fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                                Text(
                                    if (isKvkkAccepted) "Aydınlatma metni onaylandı." else "Onay bekleniyor.",
                                    color = themeColors.textSecondary,
                                    fontSize = 11.5.sp
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = { legalDetailType = "kvkk" },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = themeColors.textPrimary),
                            border = ButtonDefaults.outlinedButtonBorder.copy(brush = SolidColor(themeColors.cardBorder))
                        ) {
                            Icon(Icons.Default.Description, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("KVKK Aydınlatma Metnini Oku", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { legalDetailType = "privacy" },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = themeColors.textPrimary),
                            border = ButtonDefaults.outlinedButtonBorder.copy(brush = SolidColor(themeColors.cardBorder))
                        ) {
                            Icon(Icons.Default.Shield, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Gizlilik ve KVKK Politikasını Oku", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            if (legalDetailType != null) {
                com.example.mkat_nur.ui.legal.KvkkDetailDialog(
                    type = legalDetailType!!,
                    onDismiss = { legalDetailType = null }
                )
            }

            Spacer(Modifier.height(24.dp))

            // UYGULAMAYI PAYLAŞ BUTONU
            Button(
                onClick = {
                    val shareText = "Mîkat-ı Nur uygulamasını buradan indirebilirsiniz:\n\n${AppConfig.DOWNLOAD_URL}\n\nSelam ve dua ile..."
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, shareText)
                    }
                    context.startActivity(Intent.createChooser(intent, "Uygulamayı Paylaş"))
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) {
                Icon(Icons.Default.Share, null, tint = Color.White)
                Spacer(Modifier.width(12.dp))
                Text("Uygulamayı Paylaş", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
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
    val themeColors = com.example.mkat_nur.ui.theme.LocalAppThemeColors.current
    Card(
        modifier = Modifier.fillMaxWidth().then(if(isExpandable) Modifier.clickable { onExpandClick() } else Modifier),
        colors = CardDefaults.cardColors(containerColor = themeColors.surface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, themeColors.cardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(12.dp))
                Text(title, color = themeColors.textPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.weight(1f))
                if (isExpandable) {
                    Icon(
                        if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        null, tint = themeColors.textSecondary
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
    val themeColors = com.example.mkat_nur.ui.theme.LocalAppThemeColors.current
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = themeColors.textSecondary, fontSize = 14.sp)
        Text(value, color = themeColors.textPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}
