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
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mkat_nur.ui.prayer.PrayerTimesScreen
import com.example.mkat_nur.ui.settings.SettingsScreen
import com.example.mkat_nur.viewmodel.PrayerViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Bildirim İzni İste (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }

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
                Text(
                    "MÎKAT-I NUR",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White
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
                    label = { Text("Kur'an-ı Kerim", color = Color.White) },
                    selected = false,
                    icon = { Icon(Icons.Default.MenuBook, null, tint = Color.White) },
                    onClick = { scope.launch { drawerState.close() } },
                    colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
                )
                NavigationDrawerItem(
                    label = { Text("Risale-i Nur", color = Color.White) },
                    selected = false,
                    icon = { Icon(Icons.Default.AutoStories, null, tint = Color.White) },
                    onClick = { scope.launch { drawerState.close() } },
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
            }
        }
    ) {
        NavHost(navController = navController, startDestination = "prayer_times") {
            composable("prayer_times") {
                PrayerTimesScreen(
                    viewModel = viewModel,
                    onMenuClick = { scope.launch { drawerState.open() } }
                )
            }
            composable("settings") {
                SettingsScreen(viewModel = viewModel)
            }
            composable("tesbihat") {
                com.example.mkat_nur.ui.tesbihat.TesbihatScreen(
                    onMenuClick = { scope.launch { drawerState.open() } }
                )
            }
        }
    }
}
