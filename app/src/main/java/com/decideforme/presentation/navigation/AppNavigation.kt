package com.decideforme.presentation.navigation

import androidx.compose.animation.*
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.decideforme.presentation.categories.CategoriesScreen
import com.decideforme.presentation.history.HistoryScreen
import com.decideforme.presentation.home.HomeScreen
import com.decideforme.presentation.onboarding.OnboardingScreen
import com.decideforme.presentation.settings.SettingsScreen
import com.decideforme.presentation.sharing.ShareScreen
import com.decideforme.presentation.stats.StatsScreen

data class BottomNavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(NavRoutes.Home.route, "Decide", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome),
    BottomNavItem(NavRoutes.Categories.route, "Categories", Icons.Filled.Category, Icons.Outlined.Category),
    BottomNavItem(NavRoutes.History.route, "History", Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth),
    BottomNavItem(NavRoutes.Stats.route, "Stats", Icons.Filled.BarChart, Icons.Outlined.BarChart),
    BottomNavItem(NavRoutes.Settings.route, "Settings", Icons.Filled.Settings, Icons.Outlined.Settings)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(
    startDestination: String
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in bottomNavItems.map { it.route } ||
            currentRoute == NavRoutes.Share.route

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        val isSelected = currentRoute == item.route
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.label
                                )
                            },
                            label = { Text(item.label) },
                            selected = isSelected,
                            onClick = {
                                if (currentRoute != item.route) {
                                    navController.navigate(item.route) {
                                        popUpTo(NavRoutes.Home.route) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(NavRoutes.Onboarding.route) {
                OnboardingScreen(
                    onComplete = {
                        navController.navigate(NavRoutes.Home.route) {
                            popUpTo(NavRoutes.Onboarding.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(NavRoutes.Home.route) {
                HomeScreen()
            }
            composable(NavRoutes.Categories.route) {
                CategoriesScreen(
                    onNavigateToShare = {
                        navController.navigate(NavRoutes.Share.route)
                    }
                )
            }
            composable(NavRoutes.History.route) {
                HistoryScreen()
            }
            composable(NavRoutes.Stats.route) {
                StatsScreen()
            }
            composable(NavRoutes.Settings.route) {
                SettingsScreen()
            }
            composable(NavRoutes.Share.route) {
                ShareScreen()
            }
        }
    }
}
