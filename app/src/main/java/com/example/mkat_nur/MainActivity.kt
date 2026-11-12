package com.example.mkat_nur

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.*
import com.example.mkat_nur.R
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import coil.compose.AsyncImage
import com.example.mkat_nur.ui.auth.AuthDialog
import com.example.mkat_nur.ui.imsakiye.ImsakiyeScreen
import com.example.mkat_nur.ui.kaza.KazaScreen
import com.example.mkat_nur.ui.prayer.PrayerTimesScreen
import com.example.mkat_nur.ui.profile.ProfileScreen
import com.example.mkat_nur.ui.quran1.Quran1Screen
import com.example.mkat_nur.ui.profile.UserAvatar
import com.example.mkat_nur.ui.dhikr.DhikrScreen
import com.example.mkat_nur.util.FullScreenIntentHelper
import com.example.mkat_nur.util.StatsStore
import com.example.mkat_nur.ui.qibla.QiblaScreen
import com.example.mkat_nur.ui.religious.ReligiousDaysScreen
import com.example.mkat_nur.ui.religious.WomenSpecialScreen
import com.example.mkat_nur.ui.kutuphane.RisaleKutuphaneScreen
import com.example.mkat_nur.ui.settings.SettingsScreen
import com.example.mkat_nur.ui.share.ShareCardScreen
import com.example.mkat_nur.util.AppConfig
import com.example.mkat_nur.viewmodel.AuthViewModel
import com.example.mkat_nur.viewmodel.PrayerViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.entries.all { it.value }
        if (granted) {
            // İzinler verildi
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        com.example.mkat_nur.service.EzanPlayerManager.stopEzan()
        StatsStore.recordAppOpen(this)
        com.example.mkat_nur.util.FridayScheduler.scheduleNext(this)
        
        val permissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(android.Manifest.permission.POST_NOTIFICATIONS)
        }
        permissions.add(android.Manifest.permission.ACCESS_FINE_LOCATION)
        permissions.add(android.Manifest.permission.ACCESS_COARSE_LOCATION)

        requestPermissionLauncher.launch(permissions.toTypedArray())

        setContent {
            val prayerViewModel: PrayerViewModel = viewModel()
            val authViewModel: AuthViewModel = viewModel()

            val isDarkModeState by prayerViewModel.isDarkMode.collectAsState()
            val isInDarkMode = isDarkModeState ?: androidx.compose.foundation.isSystemInDarkTheme()

            val appThemeKey by prayerViewModel.appThemeKey.collectAsState()
            val activeTheme = remember(appThemeKey) { com.example.mkat_nur.ui.theme.AppTheme.fromKey(appThemeKey) }

            com.example.mkat_nur.ui.theme.MîkatıNurTheme(
                appTheme = activeTheme,
                isDark = isInDarkMode
            ) {
                MkatNurApp(prayerViewModel, authViewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        com.example.mkat_nur.service.EzanPlayerManager.stopEzan()
        StatsStore.recordAppOpen(this)
    }
}

@Composable
fun MkatNurApp(
    viewModel: PrayerViewModel,
    authViewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val currentUser by authViewModel.currentUser.collectAsState()
    var showAuthDialog by remember { mutableStateOf(false) }

    val prefs = remember { context.getSharedPreferences("mkat_nur_prefs", android.content.Context.MODE_PRIVATE) }
    var isKvkkAccepted by remember { mutableStateOf(prefs.getBoolean("is_kvkk_accepted", false)) }

    if (!isKvkkAccepted) {
        com.example.mkat_nur.ui.legal.KvkkConsentDialog(
            onAccepted = {
                prefs.edit()
                    .putBoolean("is_kvkk_accepted", true)
                    .putLong("kvkk_accepted_date", System.currentTimeMillis())
                    .apply()
                isKvkkAccepted = true
            }
        )
    }

    // Android 14+: "Tam ekran bildirimler" izni kapalıysa kullanıcıya bir kez açıklayıp ayara götür
    var showFullScreenDialog by remember { mutableStateOf(false) }
    LaunchedEffect(isKvkkAccepted) {
        if (isKvkkAccepted && FullScreenIntentHelper.shouldAsk(context)) showFullScreenDialog = true
    }
    if (showFullScreenDialog) {
        AlertDialog(
            onDismissRequest = {
                FullScreenIntentHelper.markAsked(context)
                showFullScreenDialog = false
            },
            title = { Text("Kilit ekranı kartları") },
            text = {
                Text(
                    "Ezan vakti ve Cuma mesajı kartının telefon kilitliyken ekranda açılabilmesi için " +
                        "“Tam ekran bildirimler” iznini vermeniz gerekir. İzin vermezseniz bildirimler " +
                        "yine gelir; dokununca kart açılır."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    FullScreenIntentHelper.markAsked(context)
                    showFullScreenDialog = false
                    FullScreenIntentHelper.openSettings(context)
                }) { Text("Ayarı aç") }
            },
            dismissButton = {
                TextButton(onClick = {
                    FullScreenIntentHelper.markAsked(context)
                    showFullScreenDialog = false
                }) { Text("Şimdi değil") }
            }
        )
    }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            val account = task.getResult(ApiException::class.java)
            val idToken = account?.idToken
            if (!idToken.isNullOrEmpty()) {
                authViewModel.signInWithGoogleToken(idToken)
            } else {
                authViewModel.setError("Google Token alınamadı. Firebase Console'da Google Sign-In aktifleştirilmeli ve SHA-1 eklenmelidir.")
            }
        } catch (e: ApiException) {
            Log.e("Auth", "Google Sign In Error: statusCode=${e.statusCode}, message=${e.message}")
            val errorMsg = when (e.statusCode) {
                10 -> "Google Giriş Hatası (10: DEVELOPER_ERROR). Firebase Console'da SHA-1 sertifika parmak izi ve Google Sign-In aktif edilmelidir."
                12500 -> "Google Giriş Hatası (12500). Lütfen Firebase Console'da Google Sign-In etkinleştirilip yeni google-services.json indirin."
                12501 -> "Google giriş işlemi iptal edildi."
                7 -> "İnternet bağlantısı hatası."
                else -> "Google Giriş Hatası (${e.statusCode}): ${e.message}"
            }
            authViewModel.setError(errorMsg)
        } catch (e: Exception) {
            Log.e("Auth", "Google Sign In Exception: ${e.message}")
            authViewModel.setError("Google Giriş Hatası: ${e.message}")
        }
    }

    val webClientId = androidx.compose.ui.res.stringResource(R.string.default_web_client_id)

    if (showAuthDialog) {
        AuthDialog(
            authViewModel = authViewModel,
            onGoogleSignInClick = {
                val client = authViewModel.getGoogleSignInClient(context, webClientId)
                googleSignInLauncher.launch(client.signInIntent)
            },
            onDismiss = { showAuthDialog = false }
        )
    }

    // Geçerli rotayı takip et
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val gesturesEnabled = drawerState.isOpen || (
        currentRoute != "risale" &&
        currentRoute != "qibla_map"
    )

    // Geri tuşu ile menüyü kapatma
    BackHandler(enabled = drawerState.isOpen) {
        scope.launch { drawerState.close() }
    }

    val isWomenSpecialMode by viewModel.isWomenSpecial.collectAsState()

    val themeColors = com.example.mkat_nur.ui.theme.LocalAppThemeColors.current

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = gesturesEnabled,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = themeColors.drawerBackground,
                modifier = Modifier.width(300.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    Spacer(Modifier.height(16.dp))
                    Image(
                        painter = painterResource(id = R.drawable.ic_launcher_mosque),
                        contentDescription = null,
                        modifier = Modifier
                            .size(80.dp)
                            .padding(4.dp)
                            .clip(CircleShape)
                            .align(Alignment.CenterHorizontally),
                        contentScale = ContentScale.Crop
                    )
                    Text(
                        "MÎKAT-I NUR",
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 12.dp),
                        style = MaterialTheme.typography.titleLarge,
                        color = themeColors.drawerTextColor,
                        textAlign = TextAlign.Center
                    )

                    // KULLANICI GİRİŞİ / HESAP KARTI
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        colors = CardDefaults.cardColors(containerColor = themeColors.drawerTextColor.copy(alpha = 0.12f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        if (currentUser != null) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val openProfile = {
                                    scope.launch { drawerState.close() }
                                    navController.navigate("profile") { launchSingleTop = true }
                                }
                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { openProfile() },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    currentUser?.let { UserAvatar(it, 40.dp, refreshKey = drawerState.isOpen) }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = currentUser?.displayName?.takeIf { it.isNotBlank() } ?: "Kullanıcı",
                                            color = themeColors.drawerTextColor,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.5.sp,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = currentUser?.email ?: "",
                                            color = themeColors.drawerTextColor.copy(alpha = 0.7f),
                                            fontSize = 11.sp,
                                            maxLines = 1
                                        )
                                    }
                                }
                                IconButton(
                                    onClick = { authViewModel.signOut() },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ExitToApp,
                                        contentDescription = "Çıkış Yap",
                                        tint = Color(0xFFFF5252)
                                    )
                                }
                            }
                        } else {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showAuthDialog = true }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = null,
                                    tint = themeColors.accent,
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Giriş Yap / Kaydol",
                                        color = themeColors.drawerTextColor,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "Hesabınıza erişmek için tıklayın",
                                        color = themeColors.drawerTextColor.copy(alpha = 0.65f),
                                        fontSize = 11.sp
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = themeColors.drawerTextColor.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(6.dp))
                    HorizontalDivider(color = themeColors.drawerTextColor.copy(alpha = 0.2f))

                    NavigationDrawerItem(
                        label = { Text("Namaz Vakitleri", color = themeColors.drawerTextColor) },
                        selected = false,
                        icon = { Icon(Icons.Default.AccessTime, null, tint = themeColors.drawerTextColor) },
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate("prayer_times")
                        },
                        colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
                    )
                    NavigationDrawerItem(
                        label = { Text("İmsakiye", color = themeColors.drawerTextColor) },
                        selected = false,
                        icon = { Icon(Icons.Default.TableChart, null, tint = themeColors.drawerTextColor) },
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate("imsakiye")
                        },
                        colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
                    )
                    NavigationDrawerItem(
                        label = { Text("Kur'an-ı Kerim", color = themeColors.drawerTextColor) },
                        selected = false,
                        icon = { Icon(Icons.Default.MenuBook, null, tint = themeColors.drawerTextColor) },
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate("quran")
                        },
                        colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
                    )
                    NavigationDrawerItem(
                        label = { Text("Risale-i Nur", color = themeColors.drawerTextColor) },
                        selected = false,
                        icon = { Icon(Icons.Default.AutoStories, null, tint = themeColors.drawerTextColor) },
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate("risale")
                        },
                        colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
                    )
                    NavigationDrawerItem(
                        label = { Text("Tesbihat", color = themeColors.drawerTextColor) },
                        selected = false,
                        icon = { Icon(Icons.Default.Favorite, null, tint = themeColors.drawerTextColor) },
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate("tesbihat")
                        },
                        colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
                    )
                    NavigationDrawerItem(
                        label = { Text("Zikirmatik", color = themeColors.drawerTextColor) },
                        selected = false,
                        icon = { Icon(Icons.Default.TouchApp, null, tint = themeColors.drawerTextColor) },
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate("zikirmatik")
                        },
                        colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
                    )
                    NavigationDrawerItem(
                        label = { Text("Kıble Bulucu", color = themeColors.drawerTextColor) },
                        selected = false,
                        icon = { Icon(Icons.Default.Explore, null, tint = themeColors.drawerTextColor) },
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate("qibla")
                        },
                        colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
                    )
                    NavigationDrawerItem(
                        label = { Text("Kaza Takibi", color = themeColors.drawerTextColor) },
                        selected = false,
                        icon = { Icon(Icons.Default.History, null, tint = themeColors.drawerTextColor) },
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate("kaza_takibi")
                        },
                        colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
                    )
                    NavigationDrawerItem(
                        label = { Text("Dini Günler", color = themeColors.drawerTextColor) },
                        selected = false,
                        icon = { Icon(Icons.Default.Event, null, tint = themeColors.drawerTextColor) },
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate("religious_days")
                        },
                        colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
                    )

                    if (isWomenSpecialMode) {
                        NavigationDrawerItem(
                            label = { Text("Kadın Özel", color = themeColors.drawerTextColor) },
                            selected = false,
                            icon = { Icon(Icons.Default.Woman, null, tint = themeColors.drawerTextColor) },
                            onClick = {
                                scope.launch { drawerState.close() }
                                navController.navigate("women_special")
                            },
                            colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
                        )
                    }

                    HorizontalDivider(color = themeColors.drawerTextColor.copy(alpha = 0.2f))
                    NavigationDrawerItem(
                        label = { Text("Ayarlar", color = themeColors.drawerTextColor) },
                        selected = false,
                        icon = { Icon(Icons.Default.Settings, null, tint = themeColors.drawerTextColor) },
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate("settings")
                        },
                        colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
                    )

                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(color = themeColors.drawerTextColor.copy(alpha = 0.15f))

                    // INSTAGRAM TAKİP KARTI
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                scope.launch { drawerState.close() }
                                val webUri = Uri.parse("https://www.instagram.com/mikati_nur/")
                                val instagramIntent = Intent(Intent.ACTION_VIEW, webUri).apply {
                                    setPackage("com.instagram.android")
                                }
                                try {
                                    context.startActivity(instagramIntent)
                                } catch (_: Exception) {
                                    context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
                                }
                            }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Instagram",
                            tint = Color(0xFFE1306C),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Instagram'da Takip Edin",
                                color = themeColors.drawerTextColor.copy(alpha = 0.7f),
                                fontSize = 11.sp
                            )
                            Text(
                                text = "@mikati_nur",
                                color = themeColors.drawerTextColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                            contentDescription = null,
                            tint = themeColors.drawerTextColor.copy(alpha = 0.5f),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Text(
                        text = "${AppConfig.PROJECT_NAME} v${AppConfig.VERSION_NAME}\nDeveloped by ${AppConfig.DEVELOPER}",
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = themeColors.drawerTextColor.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    ) {
        NavHost(navController = navController, startDestination = "prayer_times") {
            composable("prayer_times") {
                PrayerTimesScreen(
                    viewModel = viewModel,
                    onMenuClick = { scope.launch { drawerState.open() } },
                    onPrayerClick = { prayer -> navController.navigate("tesbihat?prayer=$prayer") }
                )
            }
            composable("imsakiye") {
                ImsakiyeScreen(
                    viewModel = viewModel,
                    onMenuClick = { scope.launch { drawerState.open() } }
                )
            }
            composable("kaza_takibi") {
                KazaScreen(
                    onMenuClick = { scope.launch { drawerState.open() } }
                )
            }
            composable("qibla") {
                QiblaScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable("settings") {
                SettingsScreen(viewModel = viewModel)
            }
            composable("quran") {
                Quran1Screen(
                    onMenuClick = { scope.launch { drawerState.open() } }
                )
            }
            composable("women_special") {
                WomenSpecialScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable("religious_days") {
                ReligiousDaysScreen(
                    onMenuClick = { scope.launch { drawerState.open() } }
                )
            }
            composable("risale") {
                RisaleKutuphaneScreen()
            }
            composable("tesbihat?prayer={prayer}",
                arguments = listOf(navArgument("prayer") { 
                    type = NavType.StringType
                    defaultValue = "sabah" 
                })
            ) { backStackEntry ->
                val prayer = backStackEntry.arguments?.getString("prayer") ?: "sabah"
                com.example.mkat_nur.ui.tesbihat.TesbihatScreen(
                    viewModel = viewModel,
                    initialPrayer = prayer,
                    onMenuClick = { scope.launch { drawerState.open() } }
                )
            }
            composable("zikirmatik") {
                DhikrScreen(onMenuClick = { scope.launch { drawerState.open() } })
            }
            composable("profile") {
                ProfileScreen(
                    authViewModel = authViewModel,
                    onBackClick = { navController.popBackStack() },
                    onOpenZikirmatik = { navController.navigate("zikirmatik") },
                    onOpenKaza = { navController.navigate("kaza_takibi") },
                    onOpenQuran = { navController.navigate("quran") },
                    onSignedOut = { navController.popBackStack("prayer_times", inclusive = false) }
                )
            }
            composable("share_card") {
                ShareCardScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}
