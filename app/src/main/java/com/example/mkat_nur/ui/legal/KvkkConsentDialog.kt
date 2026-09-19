package com.example.mkat_nur.ui.legal

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun KvkkConsentDialog(
    onAccepted: () -> Unit
) {
    var isChecked by remember { mutableStateOf(false) }
    var detailType by remember { mutableStateOf<String?>(null) } // "kvkk" or "privacy"

    Dialog(
        onDismissRequest = { /* First-launch consent is required */ },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.90f)
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1B263B)),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                val scrollState = rememberScrollState()

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(scrollState),
                    horizontalAlignment = Alignment.Start
                ) {
                    // Title
                    Text(
                        text = "KVKK Aydınlatma Metni",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFD700),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Uygulamayı kullanmadan önce kişisel verilerinizin nasıl işlendiği hakkında sizi bilgilendiriyoruz.",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.85f),
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Section 1
                    LegalSectionItem(
                        title = "Hangi veriler işlenebilir?",
                        description = "Hesap oluşturursanız e-posta adresiniz ve isteğe bağlı adınız; uygulamayı kullandığınızda seçtiğiniz tercihler, namaz vakitleri konum verisi ve gerekli teknik güvenlik kayıtları işlenebilir."
                    )

                    // Section 2
                    LegalSectionItem(
                        title = "Neden işlenir?",
                        description = "Veriler hesap güvenliği, namaz vakitlerinin hassas hesaplanması, uygulama özelliklerinin sunulması, tercihlerin uygulanması ve yasal yükümlülüklerin yerine getirilmesi amacıyla kullanılır."
                    )

                    // Section 3
                    LegalSectionItem(
                        title = "Kimlerle paylaşılabilir?",
                        description = "Hizmetin çalışması için gerekli altyapı, e-posta, mağaza/abonelik ve uygulamada etkinleştirdiğiniz üçüncü taraf hizmet sağlayıcılarıyla (Google Firebase altyapısı), ilgili amaçla sınırlı olarak paylaşılabilir. Reklam şirketlerine veya veri simsarlarına asla satılmaz."
                    )

                    // Section 4
                    LegalSectionItem(
                        title = "Haklarınız",
                        description = "Verilerinize ilişkin bilgi isteme, düzeltme, silme ve diğer KVKK haklarınız için Ayarlar alanını veya uygulamadaki destek kanalını kullanabilirsiniz."
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Highlighted Callout Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF2C3E50))
                            .border(1.dp, Color(0xFFFFD700).copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                            .padding(14.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = Color(0xFFFFD700),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Açık rıza ayrı bir tercihtir",
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFFD700)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Konum verisi ezan vakitlerini doğru hesaplamak için kullanılır. İsteğe bağlı hesap ve bildirim tercihlerinizi dilediğiniz an Ayarlar alanından değiştirebilirsiniz.",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.9f),
                                lineHeight = 17.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Buttons to view full documents
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { detailType = "kvkk" },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFFFD700))
                            ),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFFD700)),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Gavel, null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Tam Metni Oku", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        OutlinedButton(
                            onClick = { detailType = "privacy" },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF81D4FA))
                            ),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF81D4FA)),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Shield, null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Gizlilik Politikası", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                HorizontalDivider(color = Color.White.copy(alpha = 0.15f))

                Spacer(modifier = Modifier.height(10.dp))

                // Checkbox Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isChecked = !isChecked }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isChecked,
                        onCheckedChange = { isChecked = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color(0xFFFFD700),
                            uncheckedColor = Color.White.copy(0.6f),
                            checkmarkColor = Color(0xFF1B263B)
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Aydınlatma metnini ve Gizlilik Politikasını okudum, bilgi edindim.",
                        fontSize = 12.sp,
                        color = Color.White,
                        lineHeight = 16.sp,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Accept Button
                Button(
                    onClick = onAccepted,
                    enabled = isChecked,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFD700),
                        disabledContainerColor = Color.White.copy(alpha = 0.2f),
                        contentColor = Color(0xFF1B263B),
                        disabledContentColor = Color.White.copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(
                        text = "Anladım, Kabul Ediyorum",
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    if (detailType != null) {
        KvkkDetailDialog(
            type = detailType!!,
            onDismiss = { detailType = null }
        )
    }
}

@Composable
private fun LegalSectionItem(
    title: String,
    description: String
) {
    Column(modifier = Modifier.padding(bottom = 12.dp)) {
        Text(
            text = title,
            fontSize = 14.5.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFFFD700)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = description,
            fontSize = 12.5.sp,
            color = Color.White.copy(alpha = 0.9f),
            lineHeight = 17.5.sp
        )
    }
}

@Composable
fun KvkkDetailDialog(
    type: String, // "kvkk" or "privacy"
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val fileName = if (type == "kvkk") "kvkk_aydinlatma.txt" else "gizlilik_politikasi.txt"
    val title = if (type == "kvkk") "KVKK Aydınlatma Metni" else "Gizlilik ve KVKK Politikası"

    val fullText = remember(fileName) {
        try {
            context.assets.open(fileName).bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            "İçerik yüklenemedi."
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(8.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1B263B)),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFD700)
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Kapat",
                            tint = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 10.dp),
                    color = Color.White.copy(alpha = 0.15f)
                )

                val scrollState = rememberScrollState()
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(scrollState)
                        .padding(vertical = 4.dp)
                ) {
                    Text(
                        text = fullText,
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.9f),
                        lineHeight = 19.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Kapat",
                        color = Color(0xFF1B263B),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
