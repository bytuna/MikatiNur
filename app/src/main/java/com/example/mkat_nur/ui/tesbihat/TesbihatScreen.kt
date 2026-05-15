package com.example.mkat_nur.ui.tesbihat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TesbihatScreen(
    viewModel: com.example.mkat_nur.viewmodel.PrayerViewModel,
    initialPrayer: String = "sabah",
    onMenuClick: () -> Unit
) {
    val context = LocalContext.current
    val prayerKeys = mapOf(
        "imsak" to "sabah", "sabah" to "sabah", "güneş" to "sabah",
        "öğle" to "ogle", "ikindi" to "ikindi", "akşam" to "aksam", "yatsı" to "yatsi",
        "İmsak" to "sabah", "Sabah" to "sabah", "Güneş" to "sabah",
        "Öğle" to "ogle", "İkindi" to "ikindi", "Akşam" to "aksam", "Yatsı" to "yatsi"
    )
    
    var selectedPrayer by remember { 
        mutableStateOf(prayerKeys[initialPrayer] ?: prayerKeys[initialPrayer.lowercase(Locale.ROOT)] ?: "sabah") 
    }
    var language by remember { mutableStateOf("tr") }
    val fontSize by viewModel.fontSize.collectAsState()
    
    LaunchedEffect(initialPrayer) {
        selectedPrayer = prayerKeys[initialPrayer] ?: prayerKeys[initialPrayer.lowercase(Locale.ROOT)] ?: "sabah"
    }
    
    val tesbihatContent = remember(selectedPrayer, language) {
        try {
            context.assets.open("${selectedPrayer}_${language}.txt").bufferedReader().use { it.readText() }
        } catch (e: Exception) { "İçerik yüklenemedi." }
    }

    Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF0D1B2A), Color(0xFF1B263B))))) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("TESBİHAT", fontWeight = FontWeight.Black, color = Color.White, letterSpacing = 2.sp) },
                    navigationIcon = {
                        IconButton(onClick = onMenuClick) { Icon(Icons.Default.Menu, null, tint = Color.White) }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { padding ->
            Column(modifier = Modifier.padding(padding).fillMaxSize()) {
                ScrollableTabRow(
                    selectedTabIndex = listOf("sabah", "ogle", "ikindi", "aksam", "yatsi").indexOf(selectedPrayer),
                    containerColor = Color.Transparent,
                    contentColor = Color(0xFFFFD700),
                    edgePadding = 16.dp,
                    divider = {}
                ) {
                    val prayers = listOf("sabah" to "SABAH", "ogle" to "ÖĞLE", "ikindi" to "İKİNDİ", "aksam" to "AKŞAM", "yatsi" to "YATSI")
                    prayers.forEach { (key, label) ->
                        Tab(
                            selected = selectedPrayer == key,
                            onClick = { selectedPrayer = key },
                            text = { Text(label, fontWeight = if (selectedPrayer == key) FontWeight.Bold else FontWeight.Normal) }
                        )
                    }
                }

                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Row(Modifier.background(Color.White.copy(0.1f), RoundedCornerShape(12.dp)).padding(4.dp)) {
                        listOf("tr" to "TR", "ar" to "AR").forEach { (code, label) ->
                            FilterChip(
                                selected = language == code,
                                onClick = { language = code },
                                label = { Text(label, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFFFD700), 
                                    selectedLabelColor = Color(0xFF1B263B), // Siyah yerine koyu lacivert (daha yumuşak)
                                    labelColor = Color.White
                                ),
                                border = null, modifier = Modifier.height(32.dp)
                            )
                        }
                    }
                    Slider(
                        value = fontSize,
                        onValueChange = { viewModel.setFontSize(it) },
                        valueRange = 12f..36f,
                        modifier = Modifier.weight(1f).padding(start = 16.dp, end = 8.dp),
                        colors = SliderDefaults.colors(thumbColor = Color(0xFFFFD700), activeTrackColor = Color(0xFFFFD700))
                    )
                    IconButton(
                        onClick = { viewModel.setFontSize(16f) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SettingsBackupRestore,
                            contentDescription = "Varsayılan Boyut",
                            tint = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }

                Card(
                    modifier = Modifier.fillMaxSize().padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.03f)),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    val scrollState = rememberScrollState()
                    Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(scrollState)) {
                        // Metni satır satır analiz edip işleyen bölüm
                        tesbihatContent.lines().forEach { line ->
                            val trimmed = line.trim()
                            if (trimmed.isNotEmpty()) {
                                SmartTesbihatLine(trimmed, language, fontSize)
                            } else {
                                Spacer(Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SmartTesbihatLine(text: String, lang: String, baseSize: Float) {
    // 1. TESPİT MANTIĞI
    val isInstruction = text.let {
        it.startsWith("Denilir") || it.contains("okunur") || it.contains("defa") ||
        it.contains("tamamlanır") || it.contains("sonra") || it.contains("edilir") ||
        it.contains("geçilir") || it.contains("devam edilir") || it.contains("çekilir") ||
        it.endsWith(":")
    }

    val isTitle = !isInstruction && (
        text.contains("Sûresi") || text.contains("Duası") || 
        text.contains("Salâvat") || text.contains("Fatiha") ||
        text.contains("Âyetü’l Kürsî") || text.contains("İsm-i A’zâm") ||
        (text.length < 40 && (text.contains("Namazı") || text.contains("Tesbihatı")))
    )

    // 2. STİL BELİRLEME
    val style = when {
        isInstruction -> SpanStyle(color = Color(0xFFFFB74D), fontStyle = FontStyle.Italic, fontWeight = FontWeight.Medium)
        isTitle -> SpanStyle(color = Color(0xFFFFD700), fontWeight = FontWeight.Black)
        else -> SpanStyle(color = Color.White)
    }

    // 3. ANNOTATED STRING OLUŞTURMA
    val finalAnnotatedString = buildAnnotatedString {
        if (lang == "tr" && !isInstruction && !isTitle) {
            // Özel Zikir Boyama Mantığı
            appendZikirStyled(text)
        } else {
            withStyle(style) { append(text) }
        }
    }

    Text(
        text = finalAnnotatedString,
        color = Color.White, // Varsayılan rengi beyaz yaparak siyah metin sorununu çözüyoruz
        fontSize = if (isInstruction) (baseSize * 0.85f).sp else if (isTitle) (baseSize * 1.15f).sp else baseSize.sp,
        lineHeight = (baseSize * 1.45f).sp,
        textAlign = when {
            isInstruction || isTitle -> TextAlign.Center
            lang == "ar" -> TextAlign.Right
            else -> TextAlign.Left
        },
        modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
    )
}

fun AnnotatedString.Builder.appendZikirStyled(text: String) {
    val lila = Color(0xFFCE93D8)
    val pink = Color(0xFFF48FB1)
    val gold = Color(0xFFFFF59D)
    val blue = Color(0xFF81D4FA)
    val lightGreen = Color(0xFFA5D6A7)
    val deepGreen = Color(0xFF81C784)
    val cyan = Color(0xFF4DD0E1)

    when {
        // Allahümme ecirnâ
        text.contains("Allahümme ecirnâ") -> {
            val parts = text.split("Allahümme ecirnâ")
            parts.forEachIndexed { i, p ->
                append(p)
                if (i < parts.size - 1) {
                    withStyle(SpanStyle(color = blue, fontWeight = FontWeight.Bold)) { append("Allahümme ecirnâ") }
                }
            }
        }
        // Sübhaneke
        text.contains("Sübḥâneke yâ") -> {
            val regex = Regex("(Sübḥâneke yâ.*?teʿâleyte yâ)(.*?)(ecirnâ mine’n-nâr bi.*?)(yâ Raḥmân.*)")
            val match = regex.find(text)
            if (match != null) {
                withStyle(SpanStyle(color = lila, fontWeight = FontWeight.Bold)) { append(match.groupValues[1]) }
                append(match.groupValues[2])
                withStyle(SpanStyle(color = pink, fontWeight = FontWeight.Bold)) { append(match.groupValues[3]) }
                withStyle(SpanStyle(color = gold, fontWeight = FontWeight.Bold)) { append(match.groupValues[4]) }
            } else { append(text) }
        }
        // Ya Allah
        text.contains("yâ Allah") -> {
            val regex = Regex("(Yâ\\s+)(.*?)(\\s+yâ Allah)")
            var lastIdx = 0
            regex.findAll(text).forEach { m ->
                append(text.substring(lastIdx, m.range.first))
                withStyle(SpanStyle(color = cyan, fontWeight = FontWeight.Bold)) { append(m.groupValues[1]) }
                append(m.groupValues[2])
                withStyle(SpanStyle(color = cyan, fontWeight = FontWeight.Bold)) { append(m.groupValues[3]) }
                lastIdx = m.range.last + 1
            }
            append(text.substring(lastIdx))
        }
        // Salavat
        text.contains("Allahümme šalli") -> {
            val p1 = "Allahümme šalli"
            val p2 = "seyyidinâ Muḥammedin"
            val p3 = "ve ʿalâ âli seyyidinâ Muḥammed"
            
            var current = text
            // Basit ama etkili parçalama
            if (current.contains(p1)) {
                val before = current.substringBefore(p1)
                append(before)
                withStyle(SpanStyle(color = lightGreen, fontWeight = FontWeight.Bold)) { append(p1) }
                current = current.substringAfter(p1)
                
                if (current.contains(p2)) {
                    append(current.substringBefore(p2))
                    append(p2) // İsim Beyaz
                    current = current.substringAfter(p2)
                    
                    if (current.contains(p3)) {
                        append(current.substringBefore(p3))
                        withStyle(SpanStyle(color = deepGreen, fontWeight = FontWeight.Bold)) { append(p3) }
                        append(current.substringAfter(p3))
                    } else append(current)
                } else append(current)
            } else append(text)
        }
        else -> append(text)
    }
}
