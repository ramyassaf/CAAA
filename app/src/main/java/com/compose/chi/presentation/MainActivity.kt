package com.compose.chi.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.ThumbUp
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

class MainActivity : ComponentActivity(), AnalyticsLogger by AnalyticsLoggerImpl() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Call AnalyticsLogger Interface function
        registerLifecycleOwner(this)

        setContent {

            var darkTheme by remember { mutableStateOf(false) }

            CHITheme(darkTheme = darkTheme) {
                val navController = rememberNavController()

                // Collect Favourite
                val myFavouriteJokesViewModel = viewModel<MyFavouriteJokesViewModel>(factory = MyFavouriteJokesViewModel.Factory)
                val myFavState by myFavouriteJokesViewModel.state.collectAsState()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
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
                                    route = Screen.TenJokesScreen.route,
                                    icon = Icons.Default.List
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
                                navController.navigate(it.route)
                            }
                        )
                    }
                ){ paddingValues ->
                    AppNavHost(
                        navController = navController,
                        modifier = Modifier.padding(paddingValues),
                        onToggleDarkMode = {
                            darkTheme = !darkTheme
                        }
                    )
                }
            }
        }
    }
}