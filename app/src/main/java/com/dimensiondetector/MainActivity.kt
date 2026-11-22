package com.dimensiondetector

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.dimensiondetector.ui.screens.ArScanScreen
import com.dimensiondetector.ui.screens.DiagnosticsScreen
import com.dimensiondetector.ui.screens.HistoryScreen
import com.dimensiondetector.ui.screens.HomeScreen
import com.dimensiondetector.ui.screens.SettingsScreen
import com.dimensiondetector.ui.theme.Cyan400
import com.dimensiondetector.ui.theme.DimensionDetectorTheme
import com.dimensiondetector.ui.theme.Slate400
import com.dimensiondetector.ui.theme.Slate700
import com.dimensiondetector.ui.theme.Slate800
import com.dimensiondetector.ui.theme.Slate900
import com.dimensiondetector.ui.theme.White

sealed class NavigationTab(val route: String, val title: String, val icon: ImageVector) {
    data object Studio : NavigationTab("home", "Studio", Icons.Default.ViewInAr)
    data object History : NavigationTab("history", "History", Icons.Default.History)
    data object Settings : NavigationTab("settings", "Settings", Icons.Default.Settings)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DimensionDetectorTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route ?: "home"

                val tabs = listOf(
                    NavigationTab.Studio,
                    NavigationTab.History,
                    NavigationTab.Settings
                )

                val showBottomBar = currentRoute in listOf("home", "history", "settings")

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = Slate900,
                    bottomBar = {
                        if (showBottomBar) {
                            NavigationBar(
                                containerColor = Slate800,
                                tonalElevation = 8.dp
                            ) {
                                tabs.forEach { tab ->
                                    val isSelected = currentRoute == tab.route
                                    NavigationBarItem(
                                        icon = {
                                            Icon(
                                                imageVector = tab.icon,
                                                contentDescription = tab.title
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = tab.title,
                                                fontSize = 11.sp
                                            )
                                        },
                                        selected = isSelected,
                                        onClick = {
                                            if (currentRoute != tab.route) {
                                                navController.navigate(tab.route) {
                                                    popUpTo(navController.graph.findStartDestination().id) {
                                                        saveState = true
                                                    }
                                                    launchSingleTop = true
                                                    restoreState = true
                                                }
                                            }
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = Slate900,
                                            selectedTextColor = Cyan400,
                                            indicatorColor = Cyan400,
                                            unselectedIconColor = Slate400,
                                            unselectedTextColor = Slate400
                                        )
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "home",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("home") {
                            HomeScreen(
                                onNavigateToScan = { mode ->
                                    navController.navigate("scan?mode=$mode")
                                },
                                onNavigateToSettings = {
                                    navController.navigate("settings")
                                }
                            )
                        }

                        composable("history") {
                            HistoryScreen(
                                onNavigateToScan = {
                                    navController.navigate("scan?mode=object")
                                }
                            )
                        }

                        composable("settings") {
                            SettingsScreen(
                                onNavigateBack = {
                                    navController.popBackStack()
                                },
                                onNavigateToDiagnostics = {
                                    navController.navigate("diagnostics")
                                }
                            )
                        }

                        composable(
                            route = "scan?mode={mode}",
                            arguments = listOf(
                                navArgument("mode") {
                                    type = NavType.StringType
                                    defaultValue = "object"
                                }
                            )
                        ) { backStackEntry ->
                            val scanMode = backStackEntry.arguments?.getString("mode") ?: "object"
                            ArScanScreen(
                                onNavigateBack = { navController.popBackStack() },
                                onNavigateToDiagnostics = {
                                    navController.popBackStack()
                                    navController.navigate("diagnostics")
                                },
                                scanMode = scanMode
                            )
                        }

                        composable("diagnostics") {
                            DiagnosticsScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
