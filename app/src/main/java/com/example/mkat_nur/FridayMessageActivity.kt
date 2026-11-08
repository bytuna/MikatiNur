package com.example.mkat_nur

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mkat_nur.util.AppConfig
import com.example.mkat_nur.util.ContentManager
import com.example.mkat_nur.util.ManasContent
import com.example.mkat_nur.util.ShareUtils
import com.example.mkat_nur.util.SocialLinks
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FridayMessageActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val contentManager = ContentManager(this)
        val fridayContent = contentManager.getFridayContent()

        setContent {
            MaterialTheme {
                FridayMessageScreen(fridayContent.ayet, fridayContent.hadis, fridayContent.vecize) {
                    finish()
                }
            }
        }
    }
}

private val Gold = Color(0xFFFFC857)

private fun openLink(context: Context, url: String) {
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

@Composable
fun FridayMessageScreen(ayet: ManasContent, hadis: ManasContent, vecize: ManasContent, onClose: () -> Unit) {
    val context = LocalContext.current
    val bgGradient = Brush.verticalGradient(
        listOf(Color(0xFF0A1424), Color(0xFF13233B), Color(0xFF0D1B2A))
    )
    val today = remember { SimpleDateFormat("d MMMM yyyy · EEEE", Locale("tr", "TR")).format(Date()) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgGradient)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(vertical = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.06f)),
            shape = RoundedCornerShape(28.dp),
            border = BorderStroke(1.dp, Gold.copy(alpha = 0.25f))
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 22.dp, vertical = 24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = AppConfig.PROJECT_NAME.uppercase(),
                    color = Color.White.copy(alpha = 0.45f),
                    fontSize = 11.sp,
                    letterSpacing = 3.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(Modifier.height(10.dp))

                Text(
                    text = "Hayırlı Cumalar",
                    color = Gold,
                    fontSize = 32.sp,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = today,
                    color = Color.White.copy(alpha = 0.55f),
                    fontSize = 12.sp
                )

                Spacer(Modifier.height(14.dp))
                HorizontalDivider(modifier = Modifier.width(48.dp), color = Gold.copy(alpha = 0.6f), thickness = 1.5.dp)
                Spacer(Modifier.height(20.dp))

                ContentSection("Günün Ayeti", ayet.text, ayet.source)
                SectionDivider()
                ContentSection("Günün Hadisi", hadis.text, hadis.source)
                SectionDivider()
                ContentSection("Günün Vecizesi", vecize.text, vecize.source)

                Spacer(Modifier.height(22.dp))

                Button(
                    onClick = onClose,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.10f)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Kapat", color = Color.White, fontWeight = FontWeight.SemiBold)
                }

                Spacer(Modifier.height(16.dp))

                // Sade alt bilgi: site ve Instagram (dokunulabilir)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = AppConfig.WEBSITE_NAME,
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 11.sp,
                        modifier = Modifier
                            .clickable { openLink(context, AppConfig.WEBSITE_URL) }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    )
                    val insta = SocialLinks.instagramUrl
                    if (insta != null) {
                        Text("•", color = Color.White.copy(alpha = 0.3f), fontSize = 11.sp)
                        Text(
                            text = "@${SocialLinks.INSTAGRAM_HANDLE}",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 11.sp,
                            modifier = Modifier
                                .clickable { openLink(context, insta) }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionDivider() {
    Spacer(Modifier.height(18.dp))
    HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
    Spacer(Modifier.height(18.dp))
}

@Composable
fun ContentSection(title: String, content: String, source: String) {
    val context = LocalContext.current
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = title.uppercase(),
            color = Gold.copy(alpha = 0.85f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = "“$content”",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            textAlign = TextAlign.Center,
            fontStyle = FontStyle.Italic,
            fontFamily = FontFamily.Serif,
            lineHeight = 24.sp
        )
        if (source.isNotEmpty()) {
            Text(
                text = source,
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
        Spacer(Modifier.height(6.dp))
        // Güzel görsel kart olarak paylaş (Instagram dahil)
        TextButton(onClick = { ShareUtils.shareInfoAsImage(context, "Hayırlı Cumalar", content, source) }) {
            Icon(Icons.Default.Share, null, tint = Gold, modifier = Modifier.size(15.dp))
            Spacer(Modifier.width(6.dp))
            Text("Paylaş", color = Gold, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}
