package com.decideforme.presentation.navigation

sealed class NavRoutes(val route: String) {
    object Onboarding : NavRoutes("onboarding")
    object Home : NavRoutes("home")
    object Categories : NavRoutes("categories")
    object History : NavRoutes("history")
    object Stats : NavRoutes("stats")
    object Settings : NavRoutes("settings")
    object Share : NavRoutes("share")
    object CategoryDetail : NavRoutes("category/{categoryId}") {
        fun createRoute(categoryId: String) = "category/$categoryId"
    }
}
