package com.example.mkat_nur.ui.qibla

import android.app.Activity
import android.view.WindowManager
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mkat_nur.viewmodel.QiblaViewModel
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QiblaScreen(
    onBackClick: () -> Unit,
    viewModel: QiblaViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current

    DisposableEffect(Unit) {
        val activity = context as? Activity
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    val animatedCompassAngle by animateFloatAsState(
        targetValue = -state.compassAngle,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "compassRotation"
    )

    val diff = (state.compassAngle - state.qiblaAngle + 360) % 360
    val isFacingQibla = diff in 358.5..360.0 || diff in 0.0..1.5

    LaunchedEffect(isFacingQibla) {
        if (isFacingQibla) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(tween(1500), RepeatMode.Reverse),
        label = "glowAlpha"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mîkat-ı Kıble", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
                    }
                },
                actions = {
                    AccuracyIndicator(state.sensorAccuracy)
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1B263B),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Brush.verticalGradient(listOf(Color(0xFF1B263B), Color(0xFF0D1B2A)))),
            contentAlignment = Alignment.Center
        ) {
            // Arka Plan Partikülleri (Opsiyonel - Daha sonra eklenebilir)
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(color = Color(0xFFFFD700))
                    Text("Konum ve sensörler kalibre ediliyor...", color = Color.White)
                } else if (state.error != null) {
                    ErrorState(state.error!!)
                } else {
                    InfoCard(state)

                    // Pusula Alanı (Parallax efektli)
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(340.dp)
                            .graphicsLayer {
                                // Hafif 3D Tilt Efekti
                                rotationX = -state.pitch * 0.2f
                                rotationY = state.roll * 0.2f
                            }
                    ) {
                        // Dış Glow
                        if (isFacingQibla) {
                            Box(
                                modifier = Modifier
                                    .size(320.dp)
                                    .shadow(40.dp, CircleShape, spotColor = Color(0xFFFFD700))
                                    .background(Color(0xFFFFD700).copy(alpha = glowAlpha * 0.2f), CircleShape)
                            )
                        }

                        // Pusula Gövdesi
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(300.dp)
                                .shadow(20.dp, CircleShape)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        listOf(Color(0xFF1B263B), Color(0xFF0D1B2A))
                                    )
                                )
                        ) {
                            CompassBase(animatedCompassAngle, state.sunAzimuth)
                            QiblaNeedle(animatedCompassAngle + state.qiblaAngle, isFacingQibla)
                            
                            // Su Terazisi (Bubble Level) - Merkezde
                            BubbleLevel(state.pitch, state.roll)
                        }
                    }

                    AlignmentStatus(isFacingQibla, state.qiblaAngle)

                    // Sensör Durumu ve Uyarılar
                    if (state.sensorAccuracy < 3 || (state.pitch !in -10f..10f || state.roll !in -10f..10f)) {
                        CalibrationBox(state)
                    }
                }
            }
        }
    }
}

@Composable
fun BubbleLevel(pitch: Float, roll: Float) {
    // Telefonun eğimine göre hareket eden küçük bir kabarcık
    val bubbleX = (roll * 1.5f).coerceIn(-40f, 40f)
    val bubbleY = (-pitch * 1.5f).coerceIn(-40f, 40f)
    
    Box(
        modifier = Modifier
            .size(40.dp)
            .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        // Hedef halka
        Box(modifier = Modifier.size(8.dp).border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape))
        
        // Hareket eden kabarcık
        Box(
            modifier = Modifier
                .offset { IntOffset(bubbleX.dp.roundToPx(), bubbleY.dp.roundToPx()) }
                .size(10.dp)
                .background(
                    if (pitch in -3f..3f && roll in -3f..3f) Color(0xFF4CAF50) else Color.White.copy(alpha = 0.6f),
                    CircleShape
                )
        )
    }
}

@Composable
fun InfoCard(state: com.example.mkat_nur.viewmodel.QiblaState) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.07f)),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.padding(horizontal = 24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("MESAFA", color = Color(0xFFFFD700), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text("${"%.0f".format(state.distanceToKaaba)} km", color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
            }
            Box(modifier = Modifier.width(1.dp).height(30.dp).background(Color.White.copy(alpha = 0.1f)))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("AÇI", color = Color(0xFFFFD700), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text("${"%.1f".format(state.qiblaAngle)}°", color = Color.White, fontWeight = FontWeight.Black, fontSize = 20.sp)
            }
        }
    }
}

@Composable
fun CalibrationBox(state: com.example.mkat_nur.viewmodel.QiblaState) {
    val isNotFlat = state.pitch !in -10f..10f || state.roll !in -10f..10f
    
    Column(
        modifier = Modifier
            .padding(horizontal = 32.dp)
            .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (isNotFlat) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ScreenRotation, null, tint = Color(0xFF03A9F4), modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text("Cihazı düz tutun", color = Color(0xFF03A9F4), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
        if (state.sensorAccuracy < 3) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CompassCalibration, null, tint = Color(0xFFFFC107), modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text("Hassasiyeti artırmak için 8 çizin", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
            }
        }
    }
}

@Composable
fun AlignmentStatus(isFacingQibla: Boolean, qiblaAngle: Float) {
    val statusColor by animateColorAsState(
        targetValue = if (isFacingQibla) Color(0xFF4CAF50) else Color.Transparent,
        label = "statusColor"
    )

    Surface(
        color = statusColor,
        border = if (!isFacingQibla) BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)) else null,
        shape = CircleShape,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 32.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isFacingQibla) {
                Icon(Icons.Default.Verified, null, tint = Color.White, modifier = Modifier.size(28.dp))
                Spacer(Modifier.width(12.dp))
                Text("KIBLEYE YÖNELDİNİZ", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
            } else {
                Text("Kıble Yönü: ", color = Color.White.copy(alpha = 0.6f), fontSize = 16.sp)
                Text("${"%.1f".format(qiblaAngle)}°", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccuracyIndicator(accuracy: Int) {
    val (color, text) = when (accuracy) {
        3 -> Color(0xFF4CAF50) to "Yüksek Doğruluk"
        2 -> Color(0xFFFFC107) to "Orta Doğruluk"
        else -> Color(0xFFF44336) to "Düşük Doğruluk (Metal eşyalardan uzaklaşın)"
    }
    
    TooltipBox(
        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
        tooltip = { PlainTooltip { Text(text) } },
        state = rememberTooltipState()
    ) {
        Icon(
            Icons.Default.WifiTethering,
            contentDescription = text,
            tint = color,
            modifier = Modifier.padding(end = 16.dp).size(24.dp)
        )
    }
}

@Composable
fun ErrorState(error: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
        Icon(Icons.Default.GpsOff, null, tint = Color.Red, modifier = Modifier.size(80.dp))
        Spacer(Modifier.height(16.dp))
        Text(error, color = Color.White, textAlign = TextAlign.Center, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(24.dp))
        Button(onClick = { /* Refresh logic */ }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF415A77))) {
            Text("Tekrar Dene")
        }
    }
}

@Composable
fun CompassBase(rotation: Float, sunAzimuth: Float?) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.minDimension / 2 * 0.95f

        // İnce derece halkası
        drawCircle(color = Color(0xFF415A77).copy(alpha = 0.3f), radius = radius, center = center, style = Stroke(width = 1f))

        rotate(rotation) {
            // Güneş İndikatörü
            sunAzimuth?.let { az ->
                val sunRad = az * PI.toFloat() / 180f
                drawCircle(
                    color = Color(0xFFFFEB3B).copy(alpha = 0.6f),
                    radius = 12f,
                    center = Offset(center.x + (radius - 40f) * sin(sunRad), center.y - (radius - 40f) * cos(sunRad))
                )
            }

            for (i in 0 until 360) {
                val angleRad = i * PI.toFloat() / 180f
                val isMajor = i % 10 == 0
                val isNorth = i == 0
                
                val tickLength = if (isNorth) 35f else if (isMajor) 20f else 8f
                val tickWidth = if (isMajor) 4f else 1f
                
                val start = Offset(center.x + (radius - tickLength) * sin(angleRad), center.y - (radius - tickLength) * cos(angleRad))
                val end = Offset(center.x + radius * sin(angleRad), center.y - radius * cos(angleRad))
                
                drawLine(
                    color = if (isNorth) Color.Red else if (isMajor) Color.White else Color.White.copy(alpha = 0.3f),
                    start = start, end = end, strokeWidth = tickWidth
                )

                if (i % 30 == 0 && i != 0 && i % 90 != 0) {
                    val paint = android.graphics.Paint().apply {
                        color = android.graphics.Color.WHITE
                        alpha = 150
                        textSize = 22f
                        textAlign = android.graphics.Paint.Align.CENTER
                    }
                    drawContext.canvas.nativeCanvas.drawText(
                        i.toString(),
                        center.x + (radius - 55f) * sin(angleRad),
                        center.y - (radius - 55f) * cos(angleRad) + 8f,
                        paint
                    )
                }
            }

            // Ana Yönler (Büyük)
            val directions = listOf(0 to "K", 90 to "D", 180 to "G", 270 to "B")
            directions.forEach { (angle, label) ->
                val angleRad = angle * PI.toFloat() / 180f
                val paint = android.graphics.Paint().apply {
                    color = if (angle == 0) android.graphics.Color.RED else android.graphics.Color.WHITE
                    textSize = 48f
                    typeface = android.graphics.Typeface.DEFAULT_BOLD
                    textAlign = android.graphics.Paint.Align.CENTER
                }
                drawContext.canvas.nativeCanvas.drawText(
                    label,
                    center.x + (radius - 75f) * sin(angleRad),
                    center.y - (radius - 75f) * cos(angleRad) + 18f,
                    paint
                )
            }
        }
    }
}

@Composable
fun QiblaNeedle(rotation: Float, isFacingQibla: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "needle")
    val needleScale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = if (isFacingQibla) 1.05f else 1f,
        animationSpec = infiniteRepeatable(tween(500), RepeatMode.Reverse), label = "scale"
    )

    Canvas(modifier = Modifier.fillMaxSize().rotate(rotation).graphicsLayer {
        scaleX = needleScale
        scaleY = needleScale
    }) {
        val center = Offset(size.width / 2, size.height / 2)
        val length = size.minDimension / 2 * 0.8f
        
        // Modern İbre Tasarımı
        val path = Path().apply {
            moveTo(center.x, center.y - length) // Tepe
            lineTo(center.x - 22f, center.y + 20f) // Sol alt
            lineTo(center.x, center.y) // Merkez
            lineTo(center.x + 22f, center.y + 20f) // Sağ alt
            close()
        }
        
        drawPath(
            path = path,
            brush = Brush.verticalGradient(listOf(Color(0xFFFFD700), Color(0xFFFFA000))),
            style = Fill
        )

        // İbre Detayı (Gölge ve Derinlik)
        val shadowPath = Path().apply {
            moveTo(center.x, center.y - length)
            lineTo(center.x, center.y)
            lineTo(center.x + 22f, center.y + 20f)
            close()
        }
        drawPath(shadowPath, Color.Black.copy(alpha = 0.2f))

        // Kabe İkonu (En tepede)
        val kaabaSize = 34f
        val kaabaTop = center.y - length - 10f
        drawRect(Color(0xFF212121), Offset(center.x - kaabaSize/2, kaabaTop), Size(kaabaSize, kaabaSize))
        drawRect(Color(0xFFFFD700), Offset(center.x - kaabaSize/2, kaabaTop + kaabaSize/4), Size(kaabaSize, 6f))
    }
}
