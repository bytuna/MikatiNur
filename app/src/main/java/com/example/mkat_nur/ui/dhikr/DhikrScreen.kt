package com.example.mkat_nur.ui.dhikr

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mkat_nur.ui.theme.LocalAppThemeColors
import com.example.mkat_nur.util.StatsStore

/**
 * Zikirmatik. Sayaç ve toplam zikir sayısı bu telefonda saklanır (uygulama kapansa da kaybolmaz).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DhikrScreen(onMenuClick: () -> Unit = {}) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val themeColors = LocalAppThemeColors.current

    var count by remember { mutableIntStateOf(StatsStore.zikirCurrent(context)) }
    var total by remember { mutableLongStateOf(StatsStore.zikirTotal(context)) }
    var showResetConfirm by remember { mutableStateOf(false) }

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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
            )

            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    color = themeColors.surface,
                    shape = RoundedCornerShape(28.dp),
                    modifier = Modifier.size(width = 240.dp, height = 170.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = count.toString(),
                            fontSize = 72.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColors.textPrimary
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))
                Text(
                    text = "Toplam: $total",
                    color = themeColors.textSecondary,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(36.dp))

                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .background(themeColors.accent, CircleShape)
                        .clickable {
                            count = StatsStore.zikirIncrement(context)
                            total = StatsStore.zikirTotal(context)
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "ZİKİR",
                        color = androidx.compose.ui.graphics.Color(0xFF1B263B),
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }

                Spacer(Modifier.height(16.dp))

                TextButton(onClick = { if (count > 0) showResetConfirm = true }) {
                    Text("Sayacı sıfırla", color = themeColors.textSecondary)
                }
            }
        }
    }

    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            containerColor = themeColors.dialogSurface,
            title = { Text("Sayaç sıfırlansın mı?", color = themeColors.textPrimary) },
            text = { Text("Toplam zikir sayınız korunur; yalnızca ekrandaki sayaç sıfırlanır.", color = themeColors.textSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    StatsStore.zikirResetCurrent(context)
                    count = 0
                    showResetConfirm = false
                }) { Text("Sıfırla", color = themeColors.accent) }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirm = false }) { Text("Vazgeç", color = themeColors.textSecondary) }
            }
        )
    }
}
