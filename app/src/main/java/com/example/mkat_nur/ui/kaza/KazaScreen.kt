package com.example.mkat_nur.ui.kaza

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mkat_nur.ui.theme.LocalAppThemeColors
import com.example.mkat_nur.viewmodel.KazaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KazaScreen(
    viewModel: KazaViewModel = viewModel(),
    onMenuClick: () -> Unit
) {
    val fajrDebt by viewModel.fajrDebt.collectAsState()
    val dhuhrDebt by viewModel.dhuhrDebt.collectAsState()
    val asrDebt by viewModel.asrDebt.collectAsState()
    val maghribDebt by viewModel.maghribDebt.collectAsState()
    val ishaDebt by viewModel.ishaDebt.collectAsState()
    val witrDebt by viewModel.witrDebt.collectAsState()

    var showResetAllDialog by remember { mutableStateOf(false) }

    val themeColors = LocalAppThemeColors.current
    val bgColors = themeColors.gradientColors

    Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(bgColors))) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Kaza Takibi", fontWeight = FontWeight.ExtraBold, color = themeColors.textPrimary) },
                    navigationIcon = {
                        IconButton(onClick = onMenuClick) {
                            Icon(Icons.Default.Menu, null, tint = themeColors.textPrimary)
                        }
                    },
                    actions = {
                        IconButton(onClick = { showResetAllDialog = true }) {
                            Icon(Icons.Default.DeleteSweep, "Tümünü Sıfırla", tint = themeColors.textSecondary)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { KazaItem("Sabah", fajrDebt, onKazaPrayed = { viewModel.updateDebt("fajr", -1) }, onSetDebt = { viewModel.setDebt("fajr", it) }) }
                item { KazaItem("Öğle", dhuhrDebt, onKazaPrayed = { viewModel.updateDebt("dhuhr", -1) }, onSetDebt = { viewModel.setDebt("dhuhr", it) }) }
                item { KazaItem("İkindi", asrDebt, onKazaPrayed = { viewModel.updateDebt("asr", -1) }, onSetDebt = { viewModel.setDebt("asr", it) }) }
                item { KazaItem("Akşam", maghribDebt, onKazaPrayed = { viewModel.updateDebt("maghrib", -1) }, onSetDebt = { viewModel.setDebt("maghrib", it) }) }
                item { KazaItem("Yatsı", ishaDebt, onKazaPrayed = { viewModel.updateDebt("isha", -1) }, onSetDebt = { viewModel.setDebt("isha", it) }) }
                item { KazaItem("Vitir", witrDebt, onKazaPrayed = { viewModel.updateDebt("witr", -1) }, onSetDebt = { viewModel.setDebt("witr", it) }) }
                item { Spacer(modifier = Modifier.height(20.dp)) }
            }
        }
    }

    if (showResetAllDialog) {
        AlertDialog(
            onDismissRequest = { showResetAllDialog = false },
            title = { Text("Tümünü Sıfırla", color = themeColors.textPrimary) },
            text = { Text("Tüm kaza borçlarınız sıfırlanacaktır. Bu işlem geri alınamaz. Emin misiniz?", color = themeColors.textSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.resetAll()
                    showResetAllDialog = false
                }) {
                    Text("EVET", color = Color(0xFFF44336), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetAllDialog = false }) {
                    Text("İPTAL", color = themeColors.textSecondary)
                }
            },
            containerColor = themeColors.surface
        )
    }
}

@Composable
fun KazaItem(name: String, debt: Int, onKazaPrayed: () -> Unit, onSetDebt: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    var editMode by remember { mutableStateOf(false) }
    var textValue by remember { mutableStateOf(debt.toString()) }
    var showResetDialog by remember { mutableStateOf(false) }

    val themeColors = LocalAppThemeColors.current

    LaunchedEffect(debt) {
        if (!editMode) textValue = debt.toString()
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(containerColor = themeColors.surface),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, themeColors.cardBorder)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(name, color = themeColors.textSecondary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text("Kalan Borç: $debt", color = themeColors.textPrimary, fontSize = 24.sp, fontWeight = FontWeight.Black)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { 
                        if (editMode) {
                            val newVal = textValue.toIntOrNull() ?: 0
                            onSetDebt(newVal)
                        }
                        expanded = true
                        editMode = !editMode 
                    }) {
                        Icon(if (editMode) Icons.Default.Check else Icons.Default.Edit, null, tint = themeColors.textSecondary)
                    }
                    
                    Button(
                        onClick = { if (debt > 0) onKazaPrayed() },
                        enabled = debt > 0,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("KILINDI", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            AnimatedVisibility(visible = expanded || editMode) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    if (editMode) {
                        OutlinedTextField(
                            value = textValue,
                            onValueChange = { textValue = it },
                            label = { Text("Toplam Borç Girin", color = themeColors.textSecondary) },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = themeColors.textPrimary,
                                unfocusedTextColor = themeColors.textPrimary,
                                focusedBorderColor = Color(0xFF4CAF50),
                                unfocusedBorderColor = themeColors.cardBorder
                            ),
                            trailingIcon = {
                                IconButton(onClick = {
                                    val newVal = textValue.toIntOrNull() ?: 0
                                    onSetDebt(newVal)
                                    editMode = false
                                }) {
                                    Icon(Icons.Default.Save, null, tint = Color(0xFF4CAF50))
                                }
                            }
                        )
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                ActionButton("+1") { onSetDebt(debt + 1) }
                                ActionButton("+10") { onSetDebt(debt + 10) }
                                ActionButton("+30") { onSetDebt(debt + 30) }
                            }
                            
                            IconButton(onClick = { showResetDialog = true }) {
                                Icon(Icons.Default.Refresh, "Sıfırla", tint = Color(0xFFF44336))
                            }
                        }
                    }
                }
            }
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("$name Sıfırla", color = themeColors.textPrimary) },
            text = { Text("$name vakti için tüm kaza borcunuzu sıfırlamak istediğinize emin misiniz?", color = themeColors.textSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    onSetDebt(0)
                    showResetDialog = false
                }) {
                    Text("SIFIRLA", color = Color(0xFFF44336), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("İPTAL", color = themeColors.textSecondary)
                }
            },
            containerColor = themeColors.surface
        )
    }
}

@Composable
fun ActionButton(text: String, onClick: () -> Unit) {
    val themeColors = LocalAppThemeColors.current
    TextButton(onClick = onClick) {
        Text(text, color = themeColors.textPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}
