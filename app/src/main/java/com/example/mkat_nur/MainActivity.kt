package com.example.mkat_nur

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mkat_nur.util.AppConfig
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.example.mkat_nur.ui.imsakiye.ImsakiyeScreen
import com.example.mkat_nur.ui.kaza.KazaScreen
import com.example.mkat_nur.ui.imsakiye.ImsakiyeScreen
import com.example.mkat_nur.ui.kaza.KazaScreen
import com.example.mkat_nur.ui.prayer.PrayerTimesScreen
import com.example.mkat_nur.ui.quran.QuranScreen
import com.example.mkat_nur.ui.religious.WomenSpecialScreen
import com.example.mkat_nur.ui.settings.SettingsScreen
import com.example.mkat_nur.ui.qibla.QiblaScreen
import com.example.mkat_nur.viewmodel.PrayerViewModel
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
                MkatNurApp(prayerViewModel)
            }
        }
    }
}

@Composable
fun MkatNurApp(viewModel: PrayerViewModel) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Color(0xFF1B263B),
                modifier = Modifier.width(300.dp)
            ) {
                Spacer(Modifier.height(24.dp))
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_mosque),
                    contentDescription = null,
                    modifier = Modifier
                        .size(100.dp)
                        .padding(16.dp)
                        .clip(CircleShape)
                        .align(Alignment.CenterHorizontally),
                    contentScale = ContentScale.Crop
                )
                Text(
                    "MÎKAT-I NUR",
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
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
                    label = { Text("Kadın Özel", color = Color.White) },
                    selected = false,
                    icon = { Icon(Icons.Default.Woman, null, tint = Color.White) },
                    onClick = {
                        scope.launch { drawerState.close() }
                        navController.navigate("women_special")
                    },
                    colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
                )
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

                Spacer(Modifier.weight(1f))
                
                Text(
                    text = "${AppConfig.PROJECT_NAME} v${AppConfig.VERSION_NAME}\nDeveloped by ${AppConfig.DEVELOPER}",
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center
                )
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
            composable("risale") {
                com.example.mkat_nur.ui.risale.RisaleScreen(
                    onMenuClick = { scope.launch { drawerState.open() } }
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
        }
    }
}
