package com.example.mkat_nur.ui.quran1

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mkat_nur.ui.theme.LocalAppThemeColors

/** Seçilebilir yazı tipleri: dosyası projede bulunmayanlar listede görünmez. */
private fun availableFonts(context: Context): List<Quran1FontOption> =
    Quran1Fonts.options.filter {
        context.resources.getIdentifier(it.resName, "font", context.packageName) != 0
    }

/** Yazı tipi önizlemesinde gösterilen Besmele-i Şerife. */
internal const val BISMILLAH = "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ"

/** Seçilen mushaf yazı tipini çözer; dosya yoksa ilk yazı tipine, o da yoksa serife düşer. */
internal fun resolveMushafFont(context: Context, fontKey: String): FontFamily {
    val option = Quran1Fonts.options.firstOrNull { it.key == fontKey } ?: Quran1Fonts.options.first()
    var id = context.resources.getIdentifier(option.resName, "font", context.packageName)
    if (id == 0) id = context.resources.getIdentifier(Quran1Fonts.options.first().resName, "font", context.packageName)
    return if (id != 0) FontFamily(Font(id)) else FontFamily.Serif
}

/** Hafız seçim penceresi: Kâbe imamları, Medine imamları ve diğer hafızlar gruplu listelenir. */
@Composable
fun Quran1ReciterDialog(onDismiss: () -> Unit) {
    val P = Quran1Palette
    val current by Quran1Settings.reciterId.collectAsState()
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = P.Paper,
        title = { Text("Okuyucu (Hafız) Seç", color = P.Ink, fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Quran1Reciters.groups.forEach { group ->
                    Text(
                        group, color = P.GoldDeep, fontSize = 12.sp, fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
                    )
                    Quran1Reciters.all.filter { it.group == group }.forEach { r ->
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable {
                                Quran1Settings.setReciterId(r.id)
                                onDismiss()
                            },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = current == r.id,
                                onClick = { Quran1Settings.setReciterId(r.id); onDismiss() },
                                colors = RadioButtonDefaults.colors(selectedColor = P.Emerald, unselectedColor = P.InkSoft)
                            )
                            Text(r.name, color = P.Ink, fontSize = 14.sp, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Kapat", color = P.Emerald, fontWeight = FontWeight.Bold) } }
    )
}

/**
 * "Kur'an-ı Kerim Ayarları". Hem Ayarlar ekranında hem okuma ekranındaki ayar penceresinde kullanılır.
 * Renkler verilmezse uygulama temasından alınır.
 */
@Composable
fun Quran1SettingsSection(
    modifier: Modifier = Modifier,
    textColor: Color = LocalAppThemeColors.current.textPrimary,
    secondaryColor: Color = LocalAppThemeColors.current.textSecondary,
    accentColor: Color = Color(0xFF4CAF50)
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) { Quran1Settings.init(context) }

    val mealli by Quran1Settings.mealli.collectAsState()
    val tajweed by Quran1Settings.tajweed.collectAsState()
    val fontSize by Quran1Settings.fontSize.collectAsState()
    val fontKey by Quran1Settings.fontKey.collectAsState()
    val autoScroll by Quran1Settings.autoScroll.collectAsState()
    val reciterId by Quran1Settings.reciterId.collectAsState()
    var showReciters by remember { mutableStateOf(false) }
    val fonts = remember { availableFonts(context) }

    if (showReciters) Quran1ReciterDialog(onDismiss = { showReciters = false })

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Okuyucu (hafız)
        Text("Okuyucu (Hafız)", color = secondaryColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        OutlinedButton(
            onClick = { showReciters = true },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                Quran1Reciters.byId(reciterId).name, color = textColor, fontSize = 14.sp,
                maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f)
            )
            Text("Değiştir", color = accentColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Text(
            "Kâbe imamlarından es-Sudeys, eş-Şureym, el-Muaykılî, ed-Devserî ve el-Cühenî dâhil 13 okuyucu. Okuma hızı, okuma ekranındaki alt çubuktan değiştirilir.",
            color = secondaryColor, fontSize = 11.sp, lineHeight = 15.sp
        )

        // Mealli / Mealsiz
        Text("Gösterim", color = secondaryColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            listOf(true to "Mealli", false to "Mealsiz").forEach { (value, label) ->
                FilterChip(
                    selected = mealli == value,
                    onClick = { Quran1Settings.setMealli(value) },
                    label = { Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = accentColor,
                        selectedLabelColor = Color.White,
                        labelColor = textColor
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Text(
            if (mealli) "Arapça metnin altında Türkçe okunuş ve Diyanet meali gösterilir."
            else "Yalnızca Arapça metin gösterilir.",
            color = secondaryColor, fontSize = 11.sp
        )

        // Tecvidli / düz
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Tecvidli Metin", color = textColor, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Text(
                    if (tajweed) "Tecvid kuralları renkli gösterilir." else "Düz Medine mushaf metni gösterilir.",
                    color = secondaryColor, fontSize = 11.sp
                )
            }
            Switch(
                checked = tajweed,
                onCheckedChange = { Quran1Settings.setTajweed(it) },
                colors = SwitchDefaults.colors(checkedThumbColor = accentColor, checkedTrackColor = accentColor.copy(alpha = 0.4f))
            )
        }

        // Ayet takibi
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Otomatik Kaydırma", color = textColor, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Text("Dinlerken okunan ayet ekranın ortasında tutulur.", color = secondaryColor, fontSize = 11.sp)
            }
            Switch(
                checked = autoScroll,
                onCheckedChange = { Quran1Settings.setAutoScroll(it) },
                colors = SwitchDefaults.colors(checkedThumbColor = accentColor, checkedTrackColor = accentColor.copy(alpha = 0.4f))
            )
        }

        // Yazı boyutu
        Text("Yazı Boyutu: ${fontSize.toInt()}", color = textColor, fontSize = 14.sp)
        Slider(
            value = fontSize,
            onValueChange = { Quran1Settings.setFontSize(it) },
            valueRange = 18f..48f,
            colors = SliderDefaults.colors(
                thumbColor = accentColor,
                activeTrackColor = accentColor,
                inactiveTrackColor = secondaryColor.copy(alpha = 0.3f)
            )
        )

        // Mushaf yazı tipi
        Text("Mushaf Yazı Tipi", color = secondaryColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        fonts.forEach { option ->
            val family = remember(option.key) { resolveMushafFont(context, option.key) }
            val selected = fontKey == option.key
            Surface(
                onClick = { Quran1Settings.setFontKey(option.key) },
                shape = RoundedCornerShape(12.dp),
                color = if (selected) accentColor.copy(alpha = 0.12f) else Color.Transparent,
                border = BorderStroke(1.dp, if (selected) accentColor else secondaryColor.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = selected,
                            onClick = { Quran1Settings.setFontKey(option.key) },
                            colors = RadioButtonDefaults.colors(selectedColor = accentColor, unselectedColor = secondaryColor)
                        )
                        Text(option.label, color = textColor, fontSize = 13.sp)
                    }
                    // Ön izleme: Besmele-i Şerife, seçili yazı boyutunda (taşmaması için 20-30 aralığına sıkıştırılır)
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                        Text(
                            BISMILLAH,
                            color = textColor,
                            fontFamily = family,
                            fontSize = fontSize.coerceIn(20f, 30f).sp,
                            lineHeight = (fontSize.coerceIn(20f, 30f) * 1.8f).sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
                        )
                    }
                }
            }
        }
        if (fonts.none { it.key == "uthmanic_hafs" }) {
            Text(
                "KFGQPC Uthmanic Script HAFS için yazı tipi dosyasını res/font/uthmanic_hafs.ttf olarak ekleyin; seçenek kendiliğinden görünür.",
                color = secondaryColor, fontSize = 10.5.sp, lineHeight = 14.sp
            )
        }

        OutlinedButton(
            onClick = { Quran1Settings.resetToDefaults() },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.RestartAlt, null, tint = textColor, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Varsayılana Geri Dön", color = textColor)
        }
    }
}
