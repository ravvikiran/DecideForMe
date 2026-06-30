package com.decideforme.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.decideforme.data.repository.DecisionRepository
import com.decideforme.presentation.navigation.AppNavigation
import com.decideforme.presentation.navigation.NavRoutes
import com.decideforme.presentation.theme.DecideForMeTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var repository: DecisionRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize repository synchronously on startup (fast - just reads a local JSON file)
        runBlocking { repository.initialize() }

        setContent {
            val appData by repository.appData.collectAsState(
                initial = repository.currentData
            )

            val startDestination = if (appData.userProfile.onboardingCompleted) {
                NavRoutes.Home.route
            } else {
                NavRoutes.Onboarding.route
            }

            DecideForMeTheme(
                themeMode = appData.settings.themeMode,
                colorPalette = appData.settings.colorPalette
            ) {
                AppNavigation(startDestination = startDestination)
            }
        }
    }
}
