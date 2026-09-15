package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.data.SettingsRepository
import com.example.ui.HistoryScreen
import com.example.ui.JarvisScreen
import com.example.ui.JarvisViewModel
import com.example.ui.SettingsScreen
import com.example.ui.theme.JARVISTheme
import com.example.utils.PermissionManager

class MainActivity : ComponentActivity() {

    private lateinit var permissionManager: PermissionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        permissionManager = PermissionManager(this)
        if (!permissionManager.hasAudioPermission()) {
            permissionManager.requestAudioPermission { /* Handle result if needed */ }
        }

        setContent {
            JARVISTheme {
                JarvisApp()
            }
        }
    }
}

@Composable
fun JarvisApp() {
    val navController = rememberNavController()
    val viewModel: JarvisViewModel = viewModel()

    NavHost(navController = navController, startDestination = "jarvis") {
        composable("jarvis") {
            JarvisScreen(
                viewModel = viewModel,
                onNavigateToSettings = { navController.navigate("settings") },
                onNavigateToHistory = { navController.navigate("history") }
            )
        }
        composable("settings") {
            SettingsScreen(
                settingsRepository = viewModel.settingsRepository,
                onTestSpeak = { text -> viewModel.speakText(text) },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("history") {
            HistoryScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
