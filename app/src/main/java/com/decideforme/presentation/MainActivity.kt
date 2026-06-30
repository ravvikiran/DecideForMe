package com.decideforme.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.decideforme.data.model.AppData
import com.decideforme.data.repository.DecisionRepository
import com.decideforme.presentation.navigation.AppNavigation
import com.decideforme.presentation.navigation.NavRoutes
import com.decideforme.presentation.theme.DecideForMeTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var repository: DecisionRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            var isLoading by remember { mutableStateOf(true) }
            var initialData by remember { mutableStateOf<AppData?>(null) }

            LaunchedEffect(Unit) {
                repository.initialize()
                initialData = repository.currentData
                isLoading = false
            }

            if (isLoading || initialData == null) {
                DecideForMeTheme {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            } else {
                val appData by repository.appData.collectAsState(
                    initial = initialData!!
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
}
