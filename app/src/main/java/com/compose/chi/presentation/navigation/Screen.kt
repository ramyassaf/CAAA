package com.compose.chi.presentation.navigation

/**
 * Structure of App Screens Routes
 */
sealed class Screen(val route: String) {

    // Route to first tab screen of the BottomNavigation,
    // Which is a NavGraphBuilder.composable() inside the NavHost
    object FirstTabScreen: Screen("first_tab")

    // Route to second tab screen of the BottomNavigation,
    // Which is a NavGraphBuilder.navigation() inside the NavHost
    object SecondTabNavigationScreen: Screen("second_tab")
    // Route to first screen of NavGraphBuilder.navigation() "second_tab",
    // Which is a NavGraphBuilder.composable() inside the NavGraphBuilder.navigation() "second_tab"
    object TenJokesScreen: Screen("ten_jokes")
    // Route to second screen of NavGraphBuilder.navigation() "second_tab",
    // Which is a NavGraphBuilder.composable() inside the NavGraphBuilder.navigation() "second_tab"
    object Home2Screen: Screen("home2")
}
