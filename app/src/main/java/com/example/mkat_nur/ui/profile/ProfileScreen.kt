package com.example.mkat_nur.ui.profile

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.mkat_nur.R
import com.example.mkat_nur.network.VakitSource
import com.example.mkat_nur.ui.theme.LocalAppThemeColors
import com.example.mkat_nur.util.BackupManager
import com.example.mkat_nur.util.FullScreenIntentHelper
import com.example.mkat_nur.util.StatsStore
import com.example.mkat_nur.viewmodel.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ------------------------------------------------------------------ Avatar yardımcıları

internal fun avatarFile(context: Context, uid: String) = File(context.filesDir, "avatar_$uid.jpg")

/** Seçilen fotoğrafı kare kırpıp 512 px'e küçülterek yalnızca bu telefona kaydeder. */
private fun saveAvatar(context: Context, uid: String, uri: Uri): Boolean {
    return try {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        var sample = 1
        while (bounds.outWidth / sample > 1500 || bounds.outHeight / sample > 1500) sample *= 2
        val opts = BitmapFactory.Options().apply { inSampleSize = sample }
        var bmp = context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opts) }
            ?: return false

        val rotation = try {
            context.contentResolver.openInputStream(uri)?.use {
                when (android.media.ExifInterface(it).getAttributeInt(
                    android.media.ExifInterface.TAG_ORIENTATION, android.media.ExifInterface.ORIENTATION_NORMAL
                )) {
                    android.media.ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                    android.media.ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                    android.media.ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                    else -> 0f
                }
            } ?: 0f
        } catch (_: Exception) { 0f }
        if (rotation != 0f) {
            bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, Matrix().apply { postRotate(rotation) }, true)
        }

        val side = minOf(bmp.width, bmp.height)
        val square = Bitmap.createBitmap(bmp, (bmp.width - side) / 2, (bmp.height - side) / 2, side, side)
        val scaled = Bitmap.createScaledBitmap(square, 512, 512, true)
        avatarFile(context, uid).outputStream().use { scaled.compress(Bitmap.CompressFormat.JPEG, 88, it) }
        true
    } catch (e: Exception) {
        false
    }
}

internal fun initialsOf(name: String?, email: String?): String {
    val source = name?.trim().takeUnless { it.isNullOrEmpty() } ?: email?.substringBefore("@")?.trim().orEmpty()
    val parts = source.split(" ", ".", "_", "-").filter { it.isNotBlank() }
    val trLocale = Locale("tr", "TR")
    return when {
        parts.isEmpty() -> "?"
        parts.size == 1 -> parts[0].take(2).uppercase(trLocale)
        else -> (parts.first().take(1) + parts.last().take(1)).uppercase(trLocale)
    }
}

// ------------------------------------------------------------------ Ekran

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel,
    onBackClick: () -> Unit,
    onOpenZikirmatik: () -> Unit,
    onOpenKaza: () -> Unit,
    onOpenQuran: () -> Unit,
    onSignedOut: () -> Unit
) {
    val context = LocalContext.current
    val themeColors = LocalAppThemeColors.current
    val scope = rememberCoroutineScope()
    val user by authViewModel.currentUser.collectAsState()
    val prefs = remember { context.getSharedPreferences("mkat_nur_prefs", Context.MODE_PRIVATE) }

    fun toast(msg: String) = Toast.makeText(context, msg, Toast.LENGTH_LONG).show()

    // Hesap yoksa (çıkış / silme sonrası) ekrandan ayrıl
    LaunchedEffect(user) { if (user == null) onSignedOut() }
    val u = user ?: return

    var avatarVersion by remember { mutableIntStateOf(0) }
    val avatarPath = remember(avatarVersion, u.uid) {
        avatarFile(context, u.uid).takeIf { it.exists() }?.absolutePath
    }

    var showNameDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showSignOutDialog by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf(false) }
    var exportInfo by remember { mutableStateOf<BackupManager.ExportResult?>(null) }
    var restoreInfo by remember { mutableStateOf<Int?>(null) }
    var busy by remember { mutableStateOf(false) }
    var refreshKey by remember { mutableIntStateOf(0) }

    // ---- Fotoğraf seçici
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            scope.launch {
                val ok = withContext(Dispatchers.IO) { saveAvatar(context, u.uid, uri) }
                if (ok) avatarVersion++ else toast("Fotoğraf kaydedilemedi.")
            }
        }
    }

    // ---- İçe aktarma seçici (Belgeler/MikatNur klasöründe açılır)
    val importPicker = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val uri = result.data?.data
        if (result.resultCode == Activity.RESULT_OK && uri != null) {
            scope.launch {
                busy = true
                try {
                    val res = withContext(Dispatchers.IO) { BackupManager.restore(context, uri) }
                    res.kazaForCloud?.let { BackupManager.pushKazaToCloud(it) }
                    restoreInfo = res.restoredCount
                } catch (e: BackupManager.InvalidBackupException) {
                    toast(e.message ?: "Yedek okunamadı.")
                } catch (e: Exception) {
                    toast("Yedek geri yüklenemedi.")
                } finally {
                    busy = false
                }
            }
        }
    }

    // ---- Google kullanıcısı için hesap silmeden önce yeniden doğrulama
    val webClientId = androidx.compose.ui.res.stringResource(R.string.default_web_client_id)
    val googleReauth = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        try {
            val account = GoogleSignIn.getSignedInAccountFromIntent(result.data).getResult(ApiException::class.java)
            val token = account?.idToken
            if (token.isNullOrEmpty()) {
                toast("Google doğrulaması alınamadı.")
            } else {
                deleting = true
                authViewModel.deleteAccount(null, token) { ok, msg ->
                    deleting = false
                    toast(msg)
                    if (ok) StatsStore.clearProfileStats(context)
                }
            }
        } catch (e: Exception) {
            toast("Google doğrulaması iptal edildi.")
        }
    }

    val streak = remember(refreshKey) { StatsStore.streak(context) }
    val kazaWeek = remember(refreshKey) { StatsStore.kazaPrayedLast7Days(context) }
    val kazaLeft = remember(refreshKey) { StatsStore.kazaRemaining(context) }
    val zikirWeek = remember(refreshKey) { StatsStore.zikirLast7Days(context) }
    val zikirTotal = remember(refreshKey) { StatsStore.zikirTotal(context) }
    val quranPos = remember(refreshKey) { StatsStore.quranPosition(context) }
    val cityName = remember(refreshKey) { prefs.getString("selected_city_name", "İstanbul") ?: "İstanbul" }
    val cityId = remember(refreshKey) { prefs.getString("selected_city_id", "9541") ?: "9541" }
    val sourceName = if (VakitSource.isUmmAlQura(cityId)) "Ümmü'l-Kurâ (Hesaplama)" else "Diyanet İşleri Başkanlığı"

    val joined = remember(u.uid) {
        u.metadata?.creationTimestamp?.takeIf { it > 0 }?.let {
            SimpleDateFormat("d MMMM yyyy", Locale("tr", "TR")).format(Date(it))
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(themeColors.gradientColors))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = { Text("Profilim", color = themeColors.textPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Geri", tint = themeColors.textPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // ---------------- Üst kısım
                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(modifier = Modifier.size(104.dp)) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(themeColors.accent.copy(alpha = 0.9f))
                                .clickable { photoPicker.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            when {
                                avatarPath != null -> {
                                    val bmp = remember(avatarPath, avatarVersion) {
                                        BitmapFactory.decodeFile(avatarPath)?.asImageBitmap()
                                    }
                                    if (bmp != null) {
                                        Image(bmp, "Profil fotoğrafı", Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                    } else {
                                        Text(initialsOf(u.displayName, u.email), fontSize = 34.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1B263B))
                                    }
                                }
                                u.photoUrl != null -> AsyncImage(
                                    model = u.photoUrl, contentDescription = "Profil fotoğrafı",
                                    modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop
                                )
                                else -> Text(
                                    initialsOf(u.displayName, u.email),
                                    fontSize = 34.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1B263B)
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(themeColors.dialogSurface)
                                .clickable { photoPicker.launch("image/*") },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.PhotoCamera, "Fotoğraf seç", tint = themeColors.accent, modifier = Modifier.size(17.dp))
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(
                        u.displayName?.takeIf { it.isNotBlank() } ?: "Kullanıcı",
                        color = themeColors.textPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold
                    )
                    if (!u.email.isNullOrBlank()) {
                        Text(u.email!!, color = themeColors.textSecondary, fontSize = 13.sp)
                    }
                    if (joined != null) {
                        Spacer(Modifier.height(2.dp))
                        Text("Üyelik: $joined", color = themeColors.textSecondary, fontSize = 12.sp)
                    }
                    if (avatarPath != null) {
                        TextButton(onClick = {
                            File(avatarPath).delete(); avatarVersion++
                        }) { Text("Fotoğrafı kaldır", color = themeColors.textSecondary, fontSize = 12.sp) }
                    }
                }

                // ---------------- Son 7 günün özeti
                SectionCard("Son 7 günün özeti", Icons.Default.Insights) {
                    SummaryRow(
                        icon = Icons.Default.History,
                        title = "Kaza namazı",
                        value = if (kazaWeek > 0) "$kazaWeek vakit kıldınız" else "Henüz kayıt yok",
                        sub = if (kazaLeft > 0) "Kalan borç: $kazaLeft vakit" else "Kayıtlı kaza borcu yok",
                        onClick = onOpenKaza
                    )
                    SummaryRow(
                        icon = Icons.Default.Favorite,
                        title = "Zikir",
                        value = "$zikirWeek zikir",
                        sub = "Toplam: $zikirTotal",
                        onClick = onOpenZikirmatik
                    )
                    SummaryRow(
                        icon = Icons.Default.MenuBook,
                        title = "Kur'an-ı Kerim",
                        value = quranPos?.let { "${it.juz}. Cüz · Sayfa ${it.page}" } ?: "Henüz kalınan yer yok",
                        sub = if (quranPos != null) "Kaldığınız yerden devam edebilirsiniz" else "Cüzler sekmesinden okumaya başlayın",
                        onClick = onOpenQuran
                    )
                }

                // ---------------- Kullanım serisi
                SectionCard("Birlikte geçen günler", Icons.Default.Spa) {
                    val line = when {
                        streak.current >= 2 -> "${streak.current} gündür birlikteyiz"
                        streak.totalDays > 1 -> "Tekrar hoş geldiniz"
                        else -> "Hoş geldiniz"
                    }
                    val note = when {
                        streak.current >= 7 -> "Güzel bir istikrar. Allah kabul etsin."
                        streak.current >= 2 -> "Her gün küçük bir adım."
                        streak.totalDays > 1 -> "Her yeni gün yeni bir başlangıçtır."
                        else -> "Mîkat-ı Nur yanınızda."
                    }
                    Text(line, color = themeColors.textPrimary, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                    Text(note, color = themeColors.textSecondary, fontSize = 13.sp)
                    if (streak.totalDays > 0) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "En uzun seri: ${maxOf(streak.best, streak.current)} gün · Toplam ${streak.totalDays} gün",
                            color = themeColors.textSecondary, fontSize = 12.sp
                        )
                    }
                }

                // ---------------- Konum ve kaynak
                SectionCard("Konum ve hesaplama", Icons.Default.LocationOn) {
                    InfoLine("Seçili konum", cityName)
                    InfoLine("Vakit kaynağı", sourceName)
                }

                // ---------------- Kilit ekranı izni (yalnızca Android 14+ ve izin yoksa)
                if (!FullScreenIntentHelper.canUse(context)) {
                    SectionCard("Kilit ekranı kartları", Icons.Default.Lock) {
                        Text(
                            "Ezan vakti ve Cuma kartının kilit ekranında kendiliğinden açılması için " +
                                "“Tam ekran bildirimler” izni gerekir. İzin verilmezse bildirim yine gelir, dokununca açılır.",
                            color = themeColors.textSecondary, fontSize = 13.sp
                        )
                        TextButton(onClick = {
                            FullScreenIntentHelper.openSettings(context)
                        }) { Text("İzin ayarını aç", color = themeColors.accent) }
                    }
                }

                // ---------------- Yedekle ve geri yükle
                SectionCard("Yedekle ve geri yükle", Icons.Default.Backup) {
                    Text(
                        "Ayarlarınız, konumunuz, kaza borçlarınız, zikir sayılarınız ve Kur'an yer imleriniz " +
                            "bir dosyaya kaydedilir. Dosya Belgeler/MikatNur klasörüne yazılır.",
                        color = themeColors.textSecondary, fontSize = 13.sp
                    )
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(
                            enabled = !busy,
                            modifier = Modifier.weight(1f),
                            border = BorderStroke(1.dp, themeColors.accent.copy(alpha = 0.6f)),
                            onClick = {
                                scope.launch {
                                    busy = true
                                    try {
                                        exportInfo = withContext(Dispatchers.IO) { BackupManager.export(context) }
                                    } catch (e: Exception) {
                                        toast("Yedek kaydedilemedi: ${e.message ?: "bilinmeyen hata"}")
                                    } finally { busy = false }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Upload, null, tint = themeColors.accent, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Dışa Aktar", color = themeColors.textPrimary)
                        }
                        OutlinedButton(
                            enabled = !busy,
                            modifier = Modifier.weight(1f),
                            border = BorderStroke(1.dp, themeColors.accent.copy(alpha = 0.6f)),
                            onClick = { importPicker.launch(BackupManager.pickerIntent()) }
                        ) {
                            Icon(Icons.Default.Download, null, tint = themeColors.accent, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("İçe Aktar", color = themeColors.textPrimary)
                        }
                    }
                }

                // ---------------- Hesap
                SectionCard("Hesap", Icons.Default.ManageAccounts) {
                    ActionRow(Icons.Default.Edit, "Adı değiştir") { showNameDialog = true }
                    if (authViewModel.isPasswordUser()) {
                        ActionRow(Icons.Default.LockReset, "Şifre sıfırla") {
                            authViewModel.sendPasswordReset { _, msg -> toast(msg) }
                        }
                    }
                    ActionRow(Icons.Default.Logout, "Çıkış yap") { showSignOutDialog = true }
                    ActionRow(Icons.Default.DeleteForever, "Hesabı sil", danger = true) { showDeleteDialog = true }
                }

                Spacer(Modifier.height(24.dp))
            }
        }

        if (busy || deleting) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.35f)),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator(color = themeColors.accent) }
        }
    }

    // ================================================================= Diyaloglar

    if (showNameDialog) {
        var name by remember { mutableStateOf(u.displayName ?: "") }
        AlertDialog(
            onDismissRequest = { showNameDialog = false },
            containerColor = themeColors.dialogSurface,
            title = { Text("Adı değiştir", color = themeColors.textPrimary) },
            text = {
                OutlinedTextField(
                    value = name, onValueChange = { if (it.length <= 40) name = it },
                    singleLine = true, label = { Text("Ad Soyad") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    authViewModel.updateDisplayName(name) { ok, msg ->
                        toast(msg)
                        if (ok) showNameDialog = false
                    }
                }) { Text("Kaydet", color = themeColors.accent) }
            },
            dismissButton = { TextButton(onClick = { showNameDialog = false }) { Text("Vazgeç", color = themeColors.textSecondary) } }
        )
    }

    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            containerColor = themeColors.dialogSurface,
            title = { Text("Çıkış yapılsın mı?", color = themeColors.textPrimary) },
            text = { Text("Verileriniz hesabınızda ve bu telefonda durmaya devam eder.", color = themeColors.textSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    showSignOutDialog = false
                    authViewModel.signOut()
                }) { Text("Çıkış yap", color = themeColors.accent) }
            },
            dismissButton = { TextButton(onClick = { showSignOutDialog = false }) { Text("Vazgeç", color = themeColors.textSecondary) } }
        )
    }

    if (showDeleteDialog) {
        var password by remember { mutableStateOf("") }
        val needsPassword = authViewModel.isPasswordUser()
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = themeColors.dialogSurface,
            title = { Text("Hesap silinsin mi?", color = Color(0xFFFF5252)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Hesabınız ve bulutta saklanan kaza verileriniz kalıcı olarak silinir. " +
                            "Bu işlem geri alınamaz. Bu telefondaki ayarlar ve kayıtlar silinmez; isterseniz uygulamayı kaldırarak onları da silebilirsiniz.",
                        color = themeColors.textSecondary, fontSize = 13.sp
                    )
                    if (needsPassword) {
                        OutlinedTextField(
                            value = password, onValueChange = { password = it },
                            singleLine = true, label = { Text("Şifreniz (doğrulama için)") },
                            visualTransformation = PasswordVisualTransformation()
                        )
                    } else {
                        Text("Devam ettiğinizde Google ile yeniden doğrulama istenir.", color = themeColors.textSecondary, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (needsPassword) {
                        if (password.isEmpty()) { toast("Lütfen şifrenizi girin."); return@TextButton }
                        showDeleteDialog = false
                        deleting = true
                        authViewModel.deleteAccount(password, null) { ok, msg ->
                            deleting = false
                            toast(msg)
                            if (ok) StatsStore.clearProfileStats(context)
                        }
                    } else {
                        showDeleteDialog = false
                        googleReauth.launch(authViewModel.getGoogleSignInClient(context, webClientId).signInIntent)
                    }
                }) { Text("Hesabı sil", color = Color(0xFFFF5252)) }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Vazgeç", color = themeColors.textSecondary) } }
        )
    }

    exportInfo?.let { info ->
        AlertDialog(
            onDismissRequest = { exportInfo = null },
            containerColor = themeColors.dialogSurface,
            title = { Text("Yedek kaydedildi", color = themeColors.textPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Dosya şuraya kaydedildi:", color = themeColors.textSecondary, fontSize = 13.sp)
                    Text(info.displayPath, color = themeColors.accent, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Text("Dosya Yöneticisi → Belgeler → MikatNur", color = themeColors.textSecondary, fontSize = 12.sp)
                }
            },
            confirmButton = { TextButton(onClick = { exportInfo = null }) { Text("Tamam", color = themeColors.accent) } }
        )
    }

    restoreInfo?.let { n ->
        AlertDialog(
            onDismissRequest = { },
            containerColor = themeColors.dialogSurface,
            title = { Text("Yedek geri yüklendi", color = themeColors.textPrimary) },
            text = {
                Text(
                    "$n kayıt geri yüklendi. Değişikliklerin her yerde geçerli olması için uygulama yeniden başlatılacak.",
                    color = themeColors.textSecondary, fontSize = 13.sp
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        // Bulut yazımına kısa bir süre tanı, sonra yeniden başlat
                        kotlinx.coroutines.delay(1200)
                        BackupManager.restartApp(context)
                    }
                    restoreInfo = null
                }) { Text("Yeniden başlat", color = themeColors.accent) }
            }
        )
    }
}

// ------------------------------------------------------------------ Küçük bileşenler

@Composable
private fun SectionCard(title: String, icon: ImageVector, content: @Composable ColumnScope.() -> Unit) {
    val themeColors = LocalAppThemeColors.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = themeColors.surface),
        border = BorderStroke(1.dp, themeColors.cardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = themeColors.accent, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(title, color = themeColors.textPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
            content()
        }
    }
}

@Composable
private fun SummaryRow(icon: ImageVector, title: String, value: String, sub: String, onClick: () -> Unit) {
    val themeColors = LocalAppThemeColors.current
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).clickable(onClick = onClick).padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = themeColors.textSecondary, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = themeColors.textSecondary, fontSize = 12.sp)
            Text(value, color = themeColors.textPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Text(sub, color = themeColors.textSecondary, fontSize = 12.sp)
        }
        Icon(Icons.Default.ChevronRight, null, tint = themeColors.textSecondary.copy(alpha = 0.6f))
    }
}

@Composable
private fun InfoLine(label: String, value: String) {
    val themeColors = LocalAppThemeColors.current
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = themeColors.textSecondary, fontSize = 13.sp)
        Text(value, color = themeColors.textPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun ActionRow(icon: ImageVector, text: String, danger: Boolean = false, onClick: () -> Unit) {
    val themeColors = LocalAppThemeColors.current
    val color = if (danger) Color(0xFFFF5252) else themeColors.textPrimary
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).clickable(onClick = onClick).padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = color, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(12.dp))
        Text(text, color = color, fontSize = 15.sp)
    }
}
