package com.example.mkat_nur.ui.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Subject
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.mkat_nur.util.AppConfig

@Composable
fun ContactDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var subject by remember { mutableStateOf("Mîkat-ı Nur - Geri Bildirim") }
    var message by remember { mutableStateOf("") }
    var includeDeviceInfo by remember { mutableStateOf(true) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1B263B)),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "İletişime Geç",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4CAF50)
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

                Text(
                    text = "Görüş, öneri veya destek talebinizi aşağıdaki forma yazarak doğrudan geliştirici e-postasına iletebilirsiniz.",
                    fontSize = 12.5.sp,
                    color = Color.White.copy(alpha = 0.85f),
                    lineHeight = 17.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Konu / Başlık", color = Color.White.copy(0.7f)) },
                    leadingIcon = { Icon(Icons.Default.Subject, null, tint = Color(0xFF4CAF50)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF4CAF50),
                        unfocusedBorderColor = Color.White.copy(0.3f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("Mesajınız", color = Color.White.copy(0.7f)) },
                    minLines = 4,
                    maxLines = 8,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF4CAF50),
                        unfocusedBorderColor = Color.White.copy(0.3f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = includeDeviceInfo,
                        onCheckedChange = { includeDeviceInfo = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color(0xFF4CAF50),
                            uncheckedColor = Color.White.copy(0.6f)
                        )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Cihaz ve sürüm bilgilerini mesaja ekle",
                        color = Color.White.copy(0.8f),
                        fontSize = 11.5.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        sendEmailIntent(
                            context = context,
                            userSubject = subject,
                            userMessage = message,
                            includeDeviceInfo = includeDeviceInfo
                        )
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Icon(Icons.Default.Send, null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("E-Posta İle Gönder", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.5.sp)
                }
            }
        }
    }
}

private fun sendEmailIntent(
    context: Context,
    userSubject: String,
    userMessage: String,
    includeDeviceInfo: Boolean
) {
    val deviceInfo = if (includeDeviceInfo) {
        "\n\n------------------------------\n" +
                "Cihaz Bilgileri:\n" +
                "Uygulama: ${AppConfig.PROJECT_NAME} v${AppConfig.VERSION_NAME} (${AppConfig.VERSION_CODE})\n" +
                "Cihaz: ${Build.MANUFACTURER} ${Build.MODEL}\n" +
                "Android Sürümü: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})\n" +
                "------------------------------"
    } else ""

    val fullBody = userMessage + deviceInfo

    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("mailto:${AppConfig.CONTACT_EMAIL}")
        putExtra(Intent.EXTRA_SUBJECT, userSubject)
        putExtra(Intent.EXTRA_TEXT, fullBody)
    }

    try {
        context.startActivity(Intent.createChooser(intent, "E-Posta Gönder"))
    } catch (e: Exception) {
        // Fallback generic send intent
        val fallbackIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_EMAIL, arrayOf(AppConfig.CONTACT_EMAIL))
            putExtra(Intent.EXTRA_SUBJECT, userSubject)
            putExtra(Intent.EXTRA_TEXT, fullBody)
        }
        try {
            context.startActivity(Intent.createChooser(fallbackIntent, "E-Posta Gönder"))
        } catch (_: Exception) {}
    }
}
