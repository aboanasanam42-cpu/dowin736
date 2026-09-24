package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.components.ClinicTopBar
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PatientsScreen
import com.example.ui.screens.RemindersScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.ClinicCyanAccent
import com.example.ui.theme.ClinicDarkBackground
import com.example.ui.theme.ClinicDarkCardBorder
import com.example.ui.theme.ClinicDarkSurface
import com.example.ui.theme.ClinicHeaderTeal
import com.example.ui.theme.ClinicTealPrimary
import com.example.ui.theme.ClinicTextMuted
import com.example.ui.theme.ClinicTextPrimary
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.ClinicViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: ClinicViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                // Request Necessary Android 15 Permissions smoothly on startup
                RequestAppPermissions()

                // Full RTL layout support for Arabic text
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

                    Scaffold(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(ClinicDarkBackground),
                        topBar = {
                            ClinicTopBar(
                                title = "حسابات ديون المرضى لعيادة الرحمن",
                                onMenuClick = { selectedTab = 3 },
                                onProfileClick = { selectedTab = 1 }
                            )
                        },
                        bottomBar = {
                            ClinicBottomNavigationBar(
                                selectedTab = selectedTab,
                                onTabSelected = { selectedTab = it }
                            )
                        },
                        contentWindowInsets = WindowInsets.safeDrawing
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                                .background(ClinicDarkBackground)
                        ) {
                            Crossfade(targetState = selectedTab, label = "tab_crossfade") { tab ->
                                when (tab) {
                                    0 -> HomeScreen(viewModel = viewModel)
                                    1 -> PatientsScreen(
                                        viewModel = viewModel,
                                        onSelectPatientForHome = { selectedTab = 0 }
                                    )
                                    2 -> RemindersScreen(viewModel = viewModel)
                                    3 -> SettingsScreen(viewModel = viewModel)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RequestAppPermissions() {
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        // Permissions handled
    }

    LaunchedEffect(Unit) {
        val permissionsToRequest = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.SEND_SMS)
        }

        if (permissionsToRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }
}

@Composable
fun ClinicBottomNavigationBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    NavigationBar(
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.navigationBars)
            .testTag("clinic_bottom_navigation"),
        containerColor = ClinicDarkSurface,
        tonalElevation = 4.dp
    ) {
        // Tab 0: الرئيسية
        NavigationBarItem(
            selected = selectedTab == 0,
            onClick = { onTabSelected(0) },
            icon = {
                Icon(
                    imageVector = if (selectedTab == 0) Icons.Filled.Home else Icons.Outlined.Home,
                    contentDescription = "الرئيسية"
                )
            },
            label = {
                Text(
                    text = "الرئيسية",
                    fontSize = 11.5.sp
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.Black,
                selectedTextColor = ClinicCyanAccent,
                indicatorColor = ClinicTealPrimary,
                unselectedIconColor = ClinicTextMuted,
                unselectedTextColor = ClinicTextMuted
            ),
            modifier = Modifier.testTag("nav_tab_home")
        )

        // Tab 1: المرضى
        NavigationBarItem(
            selected = selectedTab == 1,
            onClick = { onTabSelected(1) },
            icon = {
                Icon(
                    imageVector = if (selectedTab == 1) Icons.Filled.People else Icons.Outlined.People,
                    contentDescription = "المرضى"
                )
            },
            label = {
                Text(
                    text = "المرضى",
                    fontSize = 11.5.sp
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.Black,
                selectedTextColor = ClinicCyanAccent,
                indicatorColor = ClinicTealPrimary,
                unselectedIconColor = ClinicTextMuted,
                unselectedTextColor = ClinicTextMuted
            ),
            modifier = Modifier.testTag("nav_tab_patients")
        )

        // Tab 2: التقويم والتذكير
        NavigationBarItem(
            selected = selectedTab == 2,
            onClick = { onTabSelected(2) },
            icon = {
                Icon(
                    imageVector = if (selectedTab == 2) Icons.Filled.CalendarMonth else Icons.Outlined.CalendarMonth,
                    contentDescription = "التقويم"
                )
            },
            label = {
                Text(
                    text = "التقويم",
                    fontSize = 11.5.sp
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.Black,
                selectedTextColor = ClinicCyanAccent,
                indicatorColor = ClinicTealPrimary,
                unselectedIconColor = ClinicTextMuted,
                unselectedTextColor = ClinicTextMuted
            ),
            modifier = Modifier.testTag("nav_tab_reminders")
        )

        // Tab 3: الإعدادات
        NavigationBarItem(
            selected = selectedTab == 3,
            onClick = { onTabSelected(3) },
            icon = {
                Icon(
                    imageVector = if (selectedTab == 3) Icons.Filled.Settings else Icons.Outlined.Settings,
                    contentDescription = "الإعدادات"
                )
            },
            label = {
                Text(
                    text = "الإعدادات",
                    fontSize = 11.5.sp
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color.Black,
                selectedTextColor = ClinicCyanAccent,
                indicatorColor = ClinicTealPrimary,
                unselectedIconColor = ClinicTextMuted,
                unselectedTextColor = ClinicTextMuted
            ),
            modifier = Modifier.testTag("nav_tab_settings")
        )
    }
}
