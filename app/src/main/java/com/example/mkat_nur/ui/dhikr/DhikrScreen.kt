package com.example.mkat_nur.ui.dhikr

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mkat_nur.ui.theme.LocalAppThemeColors
import com.example.mkat_nur.util.StatsStore

private val TARGET_CHOICES = listOf(0, 7, 33, 99, 100, 500, 1000)

/**
 * Zikirmatik: hazır zikirler (Arapça, Türkçe anlam, ne için çekildiği), kendi zikrini yazma/yapıştırma,
 * hedefli sayaç, tur sayısı ve toplam. Her zikrin sayacı ayrı tutulur ve telefonda saklanır.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DhikrScreen(onMenuClick: () -> Unit = {}) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val themeColors = LocalAppThemeColors.current

    var customList by remember { mutableStateOf(DhikrStore.customList(context)) }
    val allZikirs = remember(customList) { DhikrCatalog.builtIn + customList }
    var selectedId by remember { mutableStateOf(DhikrStore.selectedId(context)) }
    val selected = allZikirs.firstOrNull { it.id == selectedId } ?: allZikirs.first()

    var count by remember(selected.id) { mutableIntStateOf(DhikrStore.count(context, selected.id)) }
    var target by remember(selected.id) { mutableIntStateOf(DhikrStore.target(context, selected)) }
    var total by remember { mutableLongStateOf(StatsStore.zikirTotal(context)) }
    var week by remember { mutableIntStateOf(StatsStore.zikirLast7Days(context)) }

    var showPicker by remember { mutableStateOf(false) }
    var showResetCurrent by remember { mutableStateOf(false) }
    var showResetTotal by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Zikir?>(null) }
    var showEditor by remember { mutableStateOf(false) }
    var showNote by remember(selected.id) { mutableStateOf(false) }

    LaunchedEffect(selected.id) {
        // Profildeki ve eski sayaç da seçili zikrin sayısını göstersin
        StatsStore.zikirSetCurrent(context, count)
    }

    val round = if (target > 0) count / target else 0
    val inRound = if (target > 0) count % target else count
    val progress = if (target > 0) {
        if (count > 0 && inRound == 0) 1f else inRound.toFloat() / target
    } else 0f

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(themeColors.gradientColors))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = { Text("Zikirmatik", color = themeColors.textPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Default.Menu, contentDescription = "Menü", tint = themeColors.textPrimary)
                    }
                },
                actions = {
                    TextButton(onClick = { showPicker = true }) {
                        Text("Zikirler", color = themeColors.accent, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )

            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Seçili zikir kartı
                Surface(color = themeColors.surface, shape = RoundedCornerShape(24.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(selected.title, color = themeColors.accent, fontWeight = FontWeight.Bold, fontSize = 15.sp, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(8.dp))
                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                            Text(
                                selected.arabic,
                                color = themeColors.textPrimary,
                                fontSize = if (selected.arabic.length > 60) 20.sp else 28.sp,
                                lineHeight = if (selected.arabic.length > 60) 34.sp else 44.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        if (selected.meaning.isNotBlank()) {
                            Spacer(Modifier.height(8.dp))
                            Text(selected.meaning, color = themeColors.textSecondary, fontSize = 13.sp, textAlign = TextAlign.Center)
                        }
                        if (selected.purpose.isNotBlank()) {
                            TextButton(onClick = { showNote = !showNote }) {
                                Text(if (showNote) "Notu gizle" else "Ne için çekilir?", color = themeColors.accent, fontSize = 12.sp)
                            }
                            if (showNote) {
                                Text(selected.purpose, color = themeColors.textPrimary, fontSize = 13.sp, lineHeight = 19.sp, textAlign = TextAlign.Start)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(18.dp))

                // Sayaç: ilerleme halkası + dokunma düğmesi
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(210.dp)) {
                    CircularProgressIndicator(
                        progress = if (target > 0) progress else 0f,
                        modifier = Modifier.fillMaxSize(),
                        color = themeColors.accent,
                        trackColor = themeColors.textSecondary.copy(alpha = 0.2f),
                        strokeWidth = 8.dp
                    )
                    Box(
                        modifier = Modifier
                            .size(176.dp)
                            .background(themeColors.accent, CircleShape)
                            .clickable {
                                val n = DhikrStore.increment(context, selected.id)
                                StatsStore.zikirIncrement(context)
                                StatsStore.zikirSetCurrent(context, n)
                                count = n
                                total = StatsStore.zikirTotal(context)
                                week = StatsStore.zikirLast7Days(context)
                                val hitTarget = target > 0 && n % target == 0
                                haptic.performHapticFeedback(if (hitTarget) HapticFeedbackType.LongPress else HapticFeedbackType.TextHandleMove)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                if (target > 0) inRoundDisplay(count, target).toString() else count.toString(),
                                fontSize = 56.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1B263B)
                            )
                            Text(
                                if (target > 0) "/ $target" else "dokun",
                                fontSize = 14.sp, color = Color(0xFF1B263B).copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))
                Text(
                    buildString {
                        append("Bu zikir: $count")
                        if (target > 0) append("  •  Tamamlanan tur: $round")
                    },
                    color = themeColors.textPrimary, fontSize = 13.sp, textAlign = TextAlign.Center
                )
                Text(
                    "Toplam: $total  •  Son 7 gün: $week",
                    color = themeColors.textSecondary, fontSize = 12.sp, textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(8.dp))

                // Hedef seçimi
                Text("Hedef", color = themeColors.textSecondary, fontSize = 11.sp)
                val choices = (TARGET_CHOICES + target).distinct().sorted()
                LazyRowChips(choices, target, themeColors.accent, themeColors.textPrimary) { v ->
                    target = v
                    DhikrStore.setTarget(context, selected.id, v)
                }

                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { if (count > 0) showResetCurrent = true }, shape = RoundedCornerShape(12.dp)) {
                        Text("Bu zikri sıfırla", color = themeColors.textPrimary, fontSize = 12.sp)
                    }
                    OutlinedButton(onClick = { showResetTotal = true }, shape = RoundedCornerShape(12.dp)) {
                        Text("Toplamı sıfırla", color = themeColors.textPrimary, fontSize = 12.sp)
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }

    // ---- Zikir seçici
    if (showPicker) {
        AlertDialog(
            onDismissRequest = { showPicker = false },
            containerColor = themeColors.dialogSurface,
            title = { Text("Zikir Seç", color = themeColors.textPrimary, fontWeight = FontWeight.Bold) },
            text = {
                LazyColumn(modifier = Modifier.heightIn(max = 420.dp)) {
                    item {
                        TextButton(onClick = { editing = null; showEditor = true; showPicker = false }) {
                            Icon(Icons.Default.Add, null, tint = themeColors.accent)
                            Spacer(Modifier.width(6.dp))
                            Text("Kendi zikrimi yaz / yapıştır", color = themeColors.accent, fontWeight = FontWeight.Bold)
                        }
                    }
                    if (customList.isNotEmpty()) {
                        item { Text("Zikirlerim", color = themeColors.textSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                        items(customList, key = { it.id }) { z ->
                            ZikirRow(z, z.id == selected.id, DhikrStore.count(context, z.id), themeColors.textPrimary, themeColors.textSecondary, themeColors.accent,
                                onClick = { selectedId = z.id; DhikrStore.setSelectedId(context, z.id); showPicker = false },
                                trailing = {
                                    IconButton(onClick = { editing = z; showEditor = true; showPicker = false }) {
                                        Icon(Icons.Default.Edit, "Düzenle", tint = themeColors.textSecondary)
                                    }
                                    IconButton(onClick = {
                                        DhikrStore.deleteCustom(context, z.id)
                                        customList = DhikrStore.customList(context)
                                        if (selectedId == z.id) {
                                            selectedId = DhikrCatalog.builtIn.first().id
                                            DhikrStore.setSelectedId(context, selectedId)
                                        }
                                    }) { Icon(Icons.Default.Delete, "Sil", tint = themeColors.textSecondary) }
                                })
                        }
                    }
                    item { Text("Hazır Zikirler", color = themeColors.textSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp)) }
                    items(DhikrCatalog.builtIn, key = { it.id }) { z ->
                        ZikirRow(z, z.id == selected.id, DhikrStore.count(context, z.id), themeColors.textPrimary, themeColors.textSecondary, themeColors.accent,
                            onClick = { selectedId = z.id; DhikrStore.setSelectedId(context, z.id); showPicker = false })
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showPicker = false }) { Text("Kapat", color = themeColors.accent) } }
        )
    }

    // ---- Kendi zikrim ekleme / düzenleme
    if (showEditor) {
        val base = editing
        var title by remember(base) { mutableStateOf(base?.title ?: "") }
        var text by remember(base) { mutableStateOf(base?.arabic ?: "") }
        var meaning by remember(base) { mutableStateOf(base?.meaning ?: "") }
        var note by remember(base) { mutableStateOf(base?.purpose ?: "") }
        var targetText by remember(base) { mutableStateOf((base?.defaultTarget ?: 33).toString()) }
        AlertDialog(
            onDismissRequest = { showEditor = false },
            containerColor = themeColors.dialogSurface,
            title = { Text(if (base == null) "Kendi Zikrim" else "Zikri Düzenle", color = themeColors.textPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Ad (isteğe bağlı)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(
                        value = text, onValueChange = { text = it },
                        label = { Text("Zikir metni (yapıştırabilirsiniz)") },
                        minLines = 3, maxLines = 8, modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(value = meaning, onValueChange = { meaning = it }, label = { Text("Anlamı (isteğe bağlı)") }, minLines = 2, maxLines = 5, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("Not: ne için çekiyorum? (isteğe bağlı)") }, minLines = 2, maxLines = 5, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(
                        value = targetText, onValueChange = { targetText = it.filter(Char::isDigit).take(6) },
                        label = { Text("Hedef (0 = sınırsız)") }, singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    enabled = text.isNotBlank(),
                    onClick = {
                        val z = Zikir(
                            id = base?.id ?: DhikrStore.newCustomId(),
                            title = title.trim().ifBlank { text.trim().lines().first().take(30) },
                            arabic = text.trim().take(2000),
                            meaning = meaning.trim().take(1000),
                            purpose = note.trim().take(1000),
                            defaultTarget = targetText.toIntOrNull() ?: 0,
                            custom = true
                        )
                        DhikrStore.upsertCustom(context, z)
                        customList = DhikrStore.customList(context)
                        selectedId = z.id
                        DhikrStore.setSelectedId(context, z.id)
                        showEditor = false
                    }
                ) { Text("Kaydet", color = themeColors.accent, fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(onClick = { showEditor = false }) { Text("Vazgeç", color = themeColors.textSecondary) } }
        )
    }

    if (showResetCurrent) {
        AlertDialog(
            onDismissRequest = { showResetCurrent = false },
            containerColor = themeColors.dialogSurface,
            title = { Text("Bu zikir sıfırlansın mı?", color = themeColors.textPrimary) },
            text = { Text("Yalnızca \"${selected.title}\" sayacı sıfırlanır. Toplam zikir sayınız korunur.", color = themeColors.textSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    DhikrStore.setCount(context, selected.id, 0)
                    StatsStore.zikirSetCurrent(context, 0)
                    count = 0
                    showResetCurrent = false
                }) { Text("Sıfırla", color = themeColors.accent) }
            },
            dismissButton = { TextButton(onClick = { showResetCurrent = false }) { Text("Vazgeç", color = themeColors.textSecondary) } }
        )
    }

    if (showResetTotal) {
        AlertDialog(
            onDismissRequest = { showResetTotal = false },
            containerColor = themeColors.dialogSurface,
            title = { Text("Toplam sıfırlansın mı?", color = themeColors.textPrimary) },
            text = { Text("Toplam zikir sayısı ve tüm zikirlerin sayaçları sıfırlanır. Bu işlem geri alınamaz.", color = themeColors.textSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    StatsStore.zikirResetTotal(context)
                    DhikrStore.clearAllCounts(context)
                    count = 0
                    total = 0L
                    showResetTotal = false
                }) { Text("Tümünü sıfırla", color = themeColors.accent) }
            },
            dismissButton = { TextButton(onClick = { showResetTotal = false }) { Text("Vazgeç", color = themeColors.textSecondary) } }
        )
    }
}

/** Halka dolduğunda "33 / 33" görünsün, sonraki basışta yeni tur "1 / 33" ile başlasın. */
private fun inRoundDisplay(count: Int, target: Int): Int {
    if (count == 0) return 0
    val r = count % target
    return if (r == 0) target else r
}

@Composable
private fun LazyRowChips(choices: List<Int>, current: Int, accent: Color, text: Color, onPick: (Int) -> Unit) {
    androidx.compose.foundation.lazy.LazyRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(choices) { v ->
            FilterChip(
                selected = current == v,
                onClick = { onPick(v) },
                label = { Text(if (v == 0) "Sınırsız" else v.toString(), fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = accent, selectedLabelColor = Color(0xFF1B263B), labelColor = text)
            )
        }
    }
}

@Composable
private fun ZikirRow(
    z: Zikir,
    isSelected: Boolean,
    count: Int,
    textColor: Color,
    secondary: Color,
    accent: Color,
    onClick: () -> Unit,
    trailing: @Composable RowScope.() -> Unit = {}
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = isSelected, onClick = onClick, colors = RadioButtonDefaults.colors(selectedColor = accent, unselectedColor = secondary))
        Column(modifier = Modifier.weight(1f)) {
            Text(z.title, color = textColor, fontSize = 14.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
            Text(
                z.meaning.ifBlank { z.arabic }.take(70) + if (count > 0) "  •  $count" else "",
                color = secondary, fontSize = 11.sp, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
        trailing()
    }
}
