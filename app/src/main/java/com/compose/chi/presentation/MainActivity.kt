package com.compose.chi.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.rememberNavController
import com.compose.chi.analytics.AnalyticsLogger
import com.compose.chi.analytics.AnalyticsLoggerImpl
import com.compose.chi.presentation.navigation.components.BottomNavItem
import com.compose.chi.presentation.navigation.components.AppBottomNavigation
import com.compose.chi.presentation.navigation.AppNavHost
import com.compose.chi.presentation.navigation.Screen
import com.compose.chi.presentation.screens.my_favourite_jokes_page.MyFavouriteJokesViewModel
import com.compose.chi.presentation.ui.theme.CHITheme
import com.compose.chi.presentation.ui.theme.DarkThemeController
import com.compose.chi.presentation.ui.theme.LocalDarkTheme

class MainActivity : ComponentActivity(), AnalyticsLogger by AnalyticsLoggerImpl() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Call AnalyticsLogger Interface function
        registerLifecycleOwner(this)

        setContent {

            var darkTheme by remember { mutableStateOf(false) }
            val darkThemeController = remember(darkTheme) {
                DarkThemeController(isDark = darkTheme, toggle = { darkTheme = !darkTheme })
            }

            CompositionLocalProvider(LocalDarkTheme provides darkThemeController) {
                CHITheme(darkTheme = darkTheme) {
                    val navController = rememberNavController()

                    // Collect Favourite
                    val myFavouriteJokesViewModel =
                        viewModel<MyFavouriteJokesViewModel>(factory = MyFavouriteJokesViewModel.Factory)
                    val myFavState by myFavouriteJokesViewModel.state.collectAsState()

                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        contentWindowInsets = WindowInsets(0),
                        bottomBar = {
                            AppBottomNavigation(
                                items = listOf(
                                    BottomNavItem(
                                        name = "Random Joke",
                                        route = Screen.FirstTabScreen.route,
                                        icon = Icons.Default.ThumbUp
                                    ),
                                    BottomNavItem(
                                        name = "Ten Jokes",
                                        route = Screen.SecondTabNavigationScreen.route,
                                        icon = Icons.AutoMirrored.Filled.List
                                    ),
                                    BottomNavItem(
                                        name = "My Favourite💚Jokes",
                                        route = Screen.MyFavouriteJokesScreen.route,
                                        icon = Icons.Default.Favorite,
                                        badgeCount = myFavState.jokes.count()
                                    )
                                ),
                                navController = navController,
                                onItemClick = {
                                    println("it.route = ${it.route}")
                                    navController.navigate(it.route) {
                                        // Support multiple back stacks
                                        launchSingleTop = true
                                        restoreState = true
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                    }
                                }
                            )
                        }
                    ) { paddingValues ->
                        AppNavHost(
                            navController = navController,
                            modifier = Modifier.padding(paddingValues),
                        )
                    }
                }
            }
        }
    }
}