package com.example.mkat_nur.ui.profile

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.mkat_nur.ui.theme.LocalAppThemeColors
import com.google.firebase.auth.FirebaseUser

/**
 * Yuvarlak kullanıcı avatarı. Öncelik sırası: telefondan seçilen fotoğraf, hesabın fotoğrafı
 * (Google), yoksa baş harfler. [refreshKey] değişince yerel fotoğraf yeniden okunur.
 */
@Composable
fun UserAvatar(user: FirebaseUser, size: Dp, refreshKey: Any? = null, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val themeColors = LocalAppThemeColors.current
    val file = avatarFile(context, user.uid)
    val stamp = if (file.exists()) file.lastModified() else 0L
    val local = remember(user.uid, stamp, refreshKey) {
        if (stamp > 0L) BitmapFactory.decodeFile(file.absolutePath)?.asImageBitmap() else null
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(themeColors.accent.copy(alpha = 0.9f)),
        contentAlignment = Alignment.Center
    ) {
        when {
            local != null -> Image(local, "Profil resmi", Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            user.photoUrl != null -> AsyncImage(
                model = user.photoUrl, contentDescription = "Profil resmi",
                modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop
            )
            else -> Text(
                initialsOf(user.displayName, user.email),
                color = Color(0xFF1B263B), fontWeight = FontWeight.Bold,
                fontSize = (size.value * 0.36f).sp
            )
        }
    }
}
