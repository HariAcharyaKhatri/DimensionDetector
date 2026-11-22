package com.dimensiondetector

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dimensiondetector.ui.screens.ArScanScreen
import com.dimensiondetector.ui.screens.DiagnosticsScreen
import com.dimensiondetector.ui.screens.HomeScreen
import com.dimensiondetector.ui.theme.DimensionDetectorTheme
import com.dimensiondetector.ui.theme.Slate900

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DimensionDetectorTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Slate900
                ) {
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = "home"
                    ) {
                        composable("home") {
                            HomeScreen(
                                onNavigateToScan = { navController.navigate("scan") },
                                onNavigateToDiagnostics = { navController.navigate("diagnostics") }
                            )
                        }

                        composable("diagnostics") {
                            DiagnosticsScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        composable("scan") {
                            ArScanScreen(
                                onNavigateBack = { navController.popBackStack() },
                                onNavigateToDiagnostics = {
                                    navController.popBackStack()
                                    navController.navigate("diagnostics")
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
