package com.example.mkat_nur.ui.quran1

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mkat_nur.ui.theme.LocalAppThemeColors

/** Seçilebilir yazı tipleri: dosyası projede bulunmayanlar listede görünmez. */
private fun availableFonts(context: Context): List<Quran1FontOption> =
    Quran1Fonts.options.filter {
        context.resources.getIdentifier(it.resName, "font", context.packageName) != 0
    }

/**
 * "Kur'an-ı Kerim 1 Ayarları". Hem Ayarlar ekranında hem okuma ekranındaki ayar penceresinde kullanılır.
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
    val fonts = remember { availableFonts(context) }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = fontKey == option.key,
                    onClick = { Quran1Settings.setFontKey(option.key) },
                    colors = RadioButtonDefaults.colors(selectedColor = accentColor, unselectedColor = secondaryColor)
                )
                Text(option.label, color = textColor, fontSize = 13.sp)
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
