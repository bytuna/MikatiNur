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
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.mkat_nur.R
import java.util.Locale

val UthmanTahaFontFamily = FontFamily(Font(R.font.uthman_taha))

data class LineBlock(val isOrtalanacak: Boolean, val lines: List<String>)

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
    var showInfoDialog by remember { mutableStateOf(false) }
    var showTefeulDialog by remember { mutableStateOf(false) }
    var currentTefeulQuote by remember { mutableStateOf("") }
    val fontSize by viewModel.fontSize.collectAsState()

    val tefeulQuotes = remember {
        try {
            context.assets.open("tefeul_dersleri.txt").bufferedReader().use { it.readText() }
                .split("===")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
        } catch (_: Exception) {
            listOf("Tefeül dersleri yüklenemedi.")
        }
    }

    LaunchedEffect(initialPrayer) {
        selectedPrayer = prayerKeys[initialPrayer] ?: prayerKeys[initialPrayer.lowercase(Locale.ROOT)] ?: "sabah"
    }

    val tesbihatContent = remember(selectedPrayer, language) {
        try {
            context.assets.open("${selectedPrayer}_$language.txt").bufferedReader().use { it.readText() }
        } catch (_: Exception) { "İçerik yüklenemedi." }
    }

    val annotatedTesbihatText = remember(tesbihatContent, language) {
        buildAnnotatedString {
            val blocks = groupLinesByAlignment(tesbihatContent)
            blocks.forEachIndexed { bIdx, block ->
                if (block.isOrtalanacak) {
                    withStyle(ParagraphStyle(textAlign = TextAlign.Center)) {
                        block.lines.forEachIndexed { lIdx, line ->
                            val cleanText = cleanOrtAFlags(line)
                            appendZikirStyled(cleanText)
                            if (lIdx < block.lines.lastIndex) {
                                append("\n")
                            }
                        }
                    }
                } else {
                    block.lines.forEachIndexed { lIdx, line ->
                        val cleanText = cleanOrtAFlags(line)
                        appendZikirStyled(cleanText)
                        if (lIdx < block.lines.lastIndex) {
                            append("\n")
                        }
                    }
                }
                if (bIdx < blocks.lastIndex) {
                    append("\n")
                }
            }
        }
    }

    if (showInfoDialog) {
        TesbihatInfoDialog(onDismiss = { showInfoDialog = false })
    }

    if (showTefeulDialog) {
        TefeulDialog(
            quote = currentTefeulQuote,
            onNewTefeul = {
                if (tefeulQuotes.isNotEmpty()) {
                    val otherQuotes = tefeulQuotes.filter { it != currentTefeulQuote }
                    currentTefeulQuote = if (otherQuotes.isNotEmpty()) otherQuotes.random() else tefeulQuotes.random()
                }
            },
            onDismiss = { showTefeulDialog = false }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF0D1B2A), Color(0xFF1B263B))))
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "TESBİHAT",
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                letterSpacing = 1.5.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            IconButton(
                                onClick = { showInfoDialog = true },
                                modifier = Modifier.size(26.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Tesbihat Hakkında Bilgi",
                                    tint = Color(0xFFFFD700)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Button(
                                onClick = {
                                    if (tefeulQuotes.isNotEmpty()) {
                                        currentTefeulQuote = tefeulQuotes.random()
                                    }
                                    showTefeulDialog = true
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFFFD700),
                                    contentColor = Color(0xFF1B263B)
                                ),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text(
                                    text = "Tefeül",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onMenuClick) {
                            Icon(Icons.Default.Menu, contentDescription = "Menü", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                val selectedTabIndex = listOf("sabah", "ogle", "ikindi", "aksam", "yatsi").indexOf(selectedPrayer)
                TabRow(
                    selectedTabIndex = if (selectedTabIndex >= 0) selectedTabIndex else 0,
                    containerColor = Color.Transparent,
                    contentColor = Color(0xFFFFD700),
                    divider = {},
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
                ) {
                    val prayers = listOf(
                        "sabah" to "SABAH",
                        "ogle" to "ÖĞLE",
                        "ikindi" to "İKİNDİ",
                        "aksam" to "AKŞAM",
                        "yatsi" to "YATSI"
                    )
                    prayers.forEach { (key, label) ->
                        Tab(
                            selected = selectedPrayer == key,
                            onClick = { selectedPrayer = key },
                            text = {
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    fontWeight = if (selectedPrayer == key) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .background(Color.White.copy(0.1f), RoundedCornerShape(12.dp))
                            .padding(4.dp)
                    ) {
                        listOf("tr" to "TR", "ar" to "AR").forEach { (code, label) ->
                            FilterChip(
                                selected = language == code,
                                onClick = { language = code },
                                label = { Text(label, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFFFD700),
                                    selectedLabelColor = Color(0xFF1B263B),
                                    labelColor = Color.White
                                ),
                                border = null,
                                modifier = Modifier.height(32.dp)
                            )
                        }
                    }
                    Slider(
                        value = fontSize,
                        onValueChange = { viewModel.setFontSize(it) },
                        valueRange = 12f..36f,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 16.dp, end = 8.dp),
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFFFFD700),
                            activeTrackColor = Color(0xFFFFD700)
                        )
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
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    val scrollState = rememberScrollState()
                    val fontFamily = if (language == "ar") UthmanTahaFontFamily else FontFamily.Default

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(18.dp)
                            .verticalScroll(scrollState)
                    ) {
                        Text(
                            text = annotatedTesbihatText,
                            fontFamily = fontFamily,
                            color = Color(0xFF212121),
                            fontSize = if (language == "ar") (fontSize * 1.22f).sp else fontSize.sp,
                            lineHeight = if (language == "ar") (fontSize * 1.5f).sp else (fontSize * 1.35f).sp,
                            textAlign = if (language == "ar") TextAlign.Right else TextAlign.Left,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TefeulDialog(
    quote: String,
    onNewTefeul: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1B263B)),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Tefeül-ü Hayır (Kısa Ders)",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFD700)
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Kapat",
                            tint = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = Color.White.copy(alpha = 0.15f)
                )

                val dialogScrollState = rememberScrollState()
                Box(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .heightIn(max = 340.dp)
                        .verticalScroll(dialogScrollState)
                        .padding(vertical = 8.dp)
                ) {
                    Text(
                        text = quote,
                        fontSize = 14.5.sp,
                        color = Color.White.copy(alpha = 0.95f),
                        textAlign = TextAlign.Start,
                        lineHeight = 22.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onNewTefeul,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFFD700))
                    ) {
                        Text("Başka Ders Çek", fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                    }
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Tamam", color = Color(0xFF1B263B), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

fun groupLinesByAlignment(content: String): List<LineBlock> {
    val result = mutableListOf<LineBlock>()
    val allLines = content.lines()

    var inOrtalamaBlock = false
    var currentIsOrtalanacak: Boolean? = null
    var currentLines = mutableListOf<String>()

    allLines.forEach { line ->
        val hasStartTag = line.contains("[ORTA]", ignoreCase = true) || line.contains("[CENTER]", ignoreCase = true)
        val hasEndTag = line.contains("[/ORTA]", ignoreCase = true) || line.contains("[/CENTER]", ignoreCase = true)

        if (hasStartTag) {
            inOrtalamaBlock = true
        }

        val isOrtalanacak = inOrtalamaBlock

        if (currentIsOrtalanacak == null) {
            currentIsOrtalanacak = isOrtalanacak
            currentLines.add(line)
        } else if (currentIsOrtalanacak == isOrtalanacak) {
            currentLines.add(line)
        } else {
            result.add(LineBlock(currentIsOrtalanacak, currentLines))
            currentIsOrtalanacak = isOrtalanacak
            currentLines = mutableListOf(line)
        }

        if (hasEndTag || line.trim().isEmpty()) {
            inOrtalamaBlock = false
        }
    }
    if (currentLines.isNotEmpty() && currentIsOrtalanacak != null) {
        result.add(LineBlock(currentIsOrtalanacak, currentLines))
    }
    return result
}

fun cleanOrtAFlags(text: String): String {
    return text.replace("[ORTA]", "", ignoreCase = true)
               .replace("[/ORTA]", "", ignoreCase = true)
               .replace("[CENTER]", "", ignoreCase = true)
               .replace("[/CENTER]", "", ignoreCase = true)
}

@Composable
fun TesbihatInfoDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1B263B)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Tesbihatın Hikmeti",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFD700)
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Kapat",
                            tint = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = Color.White.copy(alpha = 0.15f)
                )

                val dialogScrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .heightIn(max = 380.dp)
                        .verticalScroll(dialogScrollState)
                ) {
                    Text(
                        text = "Risale-i Nur Külliyatı'ndan Namaz Tesbihatı Hakikatleri",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF81D4FA),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "• Namazın Tohumları ve Çekirdekleri:\n" +
                                "Namaz tesbihatı, namaz içindeki ilâhî hakikatlerin ve zikirlerin bir hülâsası, bir tefsiridir. Farz namazlardan sonra çekilen 33 Sübhânallah, 33 Elhamdülillâh ve 33 Allâhu Ekber zikirleri, kılınan namazın manevi binasını tahkim eder.\n\n" +
                                "• Velayet-i Ahmediye (a.s.m.) Yolu:\n" +
                                "Risale-i Nur usulünce yapılan tesbihat, doğrudan doğruya Sünnet-i Seniyye'ye dayanan ve Asr-ı Saadet'ten süzülüp gelen pek kuvvetli bir velayet-i ahmediye (a.s.m.) evradıdır. Bu tesbihat, mü'minin manevi zırhı ve kalesidir.\n\n" +
                                "• İbadetin Mührü ve Meyvesi:\n" +
                                "Tesbihat, namaz ağacının meyvelerini toplamak gibidir. Namaz ile Rabbimizin huzuruna çıkan ruh, tesbihat ile O'nun azametini, hamdini ve kibriyasını kainata ilan eder.",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.9f),
                        lineHeight = 20.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Anladım",
                        color = Color(0xFF1B263B),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

fun AnnotatedString.Builder.appendZikirStyled(text: String) {
    val redColor = Color(0xFFC62828)     // Koyu Kırmızı (Cehennem / İsimler / Uyarı)
    val greenColor = Color(0xFF2E7D32)   // Zümrüt Yeşili (Cennet / Af / Mağfiret)
    val orangeColor = Color(0xFFE65100)  // Koyu Sıcak Turuncu (Fitne / Şer Sığınmaları)
    val magentaColor = Color(0xFF6A1B9A) // Koyu Mor/Lila (Nefis / Manevi Hastalıklar)
    val goldColor = Color(0xFFB78103)    // Koyu Amber/Altın (Ayet / Esma / Zikir)
    val blueColor = Color(0xFF1565C0)    // Derin Mavi (Salavat / Dua / Talimat)
    val cyanColor = Color(0xFF00838F)    // Koyu Camgöbeği (Zikir)

    val regex = Regex("\\[RENK:(.*?)\\](.*?)\\[/RENK]", setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.IGNORE_CASE))
    var lastIdx = 0

    val matches = regex.findAll(text).toList()

    if (matches.isEmpty()) {
        appendUnTaggedText(text)
    } else {
        matches.forEach { match ->
            if (match.range.first > lastIdx) {
                val unTaggedText = text.substring(lastIdx, match.range.first)
                appendUnTaggedText(unTaggedText)
            }

            val kategori = match.groupValues[1].lowercase().trim()
            val icerik = match.groupValues[2]

            val seciliRenk = when (kategori) {
                "kırmızı", "kirmizi", "ates", "ateş", "red" -> redColor
                "yeşil", "yesil", "cennet", "green"         -> greenColor
                "turuncu", "fitne", "orange"               -> orangeColor
                "lila", "magenta", "nefis", "mor"          -> magentaColor
                "altın", "altin", "ayet", "esma", "gold"   -> goldColor
                "mavi", "blue", "dua", "talimat", "salavat" -> blueColor
                "zikir"                                    -> cyanColor
                "ilahi"                                    -> greenColor
                else                                       -> Color(0xFF212121)
            }

            withStyle(SpanStyle(color = seciliRenk, fontWeight = FontWeight.Bold)) {
                append(icerik)
            }

            lastIdx = match.range.last + 1
        }
        if (lastIdx < text.length) {
            appendUnTaggedText(text.substring(lastIdx))
        }
    }
}

fun AnnotatedString.Builder.appendUnTaggedText(text: String) {
    val titleColor = Color(0xFF0D1B2A)
    val defaultColor = Color(0xFF212121)

    text.lines().forEachIndexed { index, line ->
        if (index > 0) append("\n")
        val isTitle = (line.contains("Sûresi") || line.contains("Duası") ||
                line.contains("Salâvat") || line.contains("Fatiha") ||
                line.contains("Âyetü’l Kürsî") || line.contains("İsm-i A’zâm") ||
                (line.length in 1..45 && (line.contains("Namazı") || line.contains("okunur") || line.contains("Tesbihatı"))))

        if (isTitle) {
            withStyle(SpanStyle(color = titleColor, fontWeight = FontWeight.Black)) {
                append(line)
            }
        } else {
            withStyle(SpanStyle(color = defaultColor)) {
                append(line)
            }
        }
    }
}
