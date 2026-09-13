package com.example.mkat_nur

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
import androidx.compose.material.icons.filled.*
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
import com.example.mkat_nur.ui.qibla.QiblaScreen
import com.example.mkat_nur.ui.quran.QuranScreen
import com.example.mkat_nur.ui.religious.ReligiousDaysScreen
import com.example.mkat_nur.ui.religious.WomenSpecialScreen
import com.example.mkat_nur.ui.risale.RisaleWebViewScreen
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
        
        val permissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(android.Manifest.permission.POST_NOTIFICATIONS)
        }
        permissions.add(android.Manifest.permission.ACCESS_FINE_LOCATION)
        permissions.add(android.Manifest.permission.ACCESS_COARSE_LOCATION)

        requestPermissionLauncher.launch(permissions.toTypedArray())

        setContent {
            MaterialTheme {
                val prayerViewModel: PrayerViewModel = viewModel()
                val authViewModel: AuthViewModel = viewModel()
                MkatNurApp(prayerViewModel, authViewModel)
            }
        }
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

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            val account = task.getResult(ApiException::class.java)
            account?.idToken?.let { token ->
                authViewModel.signInWithGoogleToken(token)
            }
        } catch (e: Exception) {
            Log.e("Auth", "Google Sign In Error: ${e.message}")
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
        currentRoute != "qibla_map" && 
        currentRoute?.startsWith("risale_reader") != true
    )

    // Geri tuşu ile menüyü kapatma
    BackHandler(enabled = drawerState.isOpen) {
        scope.launch { drawerState.close() }
    }

    val isWomenSpecialMode by viewModel.isWomenSpecial.collectAsState()

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = gesturesEnabled,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Color(0xFF1B263B),
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
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )

                    // KULLANICI GİRİŞİ / HESAP KARTI
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.12f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        if (currentUser != null) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (!currentUser?.photoUrl?.toString().isNullOrEmpty()) {
                                    AsyncImage(
                                        model = currentUser?.photoUrl,
                                        contentDescription = "Profil Resmi",
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.AccountCircle,
                                        contentDescription = null,
                                        tint = Color(0xFFFFD700),
                                        modifier = Modifier.size(40.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = currentUser?.displayName ?: "Kullanıcı",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.5.sp,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = currentUser?.email ?: "",
                                        color = Color.White.copy(alpha = 0.7f),
                                        fontSize = 11.sp,
                                        maxLines = 1
                                    )
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
                                    tint = Color(0xFFFFD700),
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Giriş Yap / Kaydol",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "Hesabınıza erişmek için tıklayın",
                                        color = Color.White.copy(alpha = 0.65f),
                                        fontSize = 11.sp
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(6.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.2f))

                    NavigationDrawerItem(
                        label = { Text("Namaz Vakitleri", color = Color.White) },
                        selected = false,
                        icon = { Icon(Icons.Default.AccessTime, null, tint = Color.White) },
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate("prayer_times")
                        },
                        colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
                    )
                    NavigationDrawerItem(
                        label = { Text("İmsakiye", color = Color.White) },
                        selected = false,
                        icon = { Icon(Icons.Default.TableChart, null, tint = Color.White) },
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate("imsakiye")
                        },
                        colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
                    )
                    NavigationDrawerItem(
                        label = { Text("Kur'an-ı Kerim", color = Color.White) },
                        selected = false,
                        icon = { Icon(Icons.Default.MenuBook, null, tint = Color.White) },
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate("quran")
                        },
                        colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
                    )
                    NavigationDrawerItem(
                        label = { Text("Risale-i Nur", color = Color.White) },
                        selected = false,
                        icon = { Icon(Icons.Default.AutoStories, null, tint = Color.White) },
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate("risale")
                        },
                        colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
                    )
                    NavigationDrawerItem(
                        label = { Text("Tesbihat", color = Color.White) },
                        selected = false,
                        icon = { Icon(Icons.Default.Favorite, null, tint = Color.White) },
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate("tesbihat")
                        },
                        colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
                    )
                    NavigationDrawerItem(
                        label = { Text("Kıble Bulucu", color = Color.White) },
                        selected = false,
                        icon = { Icon(Icons.Default.Explore, null, tint = Color.White) },
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate("qibla")
                        },
                        colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
                    )
                    NavigationDrawerItem(
                        label = { Text("Kaza Takibi", color = Color.White) },
                        selected = false,
                        icon = { Icon(Icons.Default.History, null, tint = Color.White) },
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate("kaza_takibi")
                        },
                        colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
                    )
                    NavigationDrawerItem(
                        label = { Text("Dini Günler", color = Color.White) },
                        selected = false,
                        icon = { Icon(Icons.Default.Event, null, tint = Color.White) },
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate("religious_days")
                        },
                        colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
                    )

                    if (isWomenSpecialMode) {
                        NavigationDrawerItem(
                            label = { Text("Kadın Özel", color = Color.White) },
                            selected = false,
                            icon = { Icon(Icons.Default.Woman, null, tint = Color.White) },
                            onClick = {
                                scope.launch { drawerState.close() }
                                navController.navigate("women_special")
                            },
                            colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
                        )
                    }

                    HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
                    NavigationDrawerItem(
                        label = { Text("Ayarlar", color = Color.White) },
                        selected = false,
                        icon = { Icon(Icons.Default.Settings, null, tint = Color.White) },
                        onClick = {
                            scope.launch { drawerState.close() }
                            navController.navigate("settings")
                        },
                        colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
                    )

                    Spacer(Modifier.height(24.dp))
                    
                    Text(
                        text = "${AppConfig.PROJECT_NAME} v${AppConfig.VERSION_NAME}\nDeveloped by ${AppConfig.DEVELOPER}",
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.5f),
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
                QuranScreen(
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
                RisaleWebViewScreen(
                    bookId = null,
                    onBackClick = { navController.popBackStack() },
                    onMenuClick = { scope.launch { drawerState.open() } }
                )
            }
            composable("risale_reader/{bookId}") { backStackEntry ->
                val bookId = backStackEntry.arguments?.getString("bookId") ?: ""
                RisaleWebViewScreen(
                    bookId = bookId,
                    onBackClick = { navController.popBackStack() }
                )
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
            composable("share_card") {
                ShareCardScreen(
                    prayerViewModel = viewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}
