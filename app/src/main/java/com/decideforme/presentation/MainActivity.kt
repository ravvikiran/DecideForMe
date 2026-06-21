package com.decideforme.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.lifecycle.lifecycleScope
import com.decideforme.data.repository.DecisionRepository
import com.decideforme.presentation.navigation.AppNavigation
import com.decideforme.presentation.navigation.NavRoutes
import com.decideforme.presentation.theme.DecideForMeTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var repository: DecisionRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        lifecycleScope.launch {
            repository.initialize()
            val appData = repository.appData.first()

            setContent {
                val themeMode = appData.settings.themeMode
                val colorPalette = appData.settings.colorPalette
                val startDestination = if (appData.userProfile.onboardingCompleted) {
                    NavRoutes.Home.route
                } else {
                    NavRoutes.Onboarding.route
                }

                // Observe settings changes for live theme updates
                val currentData by repository.appData.collectAsState(initial = appData)

                DecideForMeTheme(
                    themeMode = currentData.settings.themeMode,
                    colorPalette = currentData.settings.colorPalette
                ) {
                    AppNavigation(startDestination = startDestination)
                }
            }
        }
    }
}
