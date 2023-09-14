package com.compose.chi.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.compose.chi.analytics.AnalyticsLogger
import com.compose.chi.analytics.AnalyticsLoggerImpl
import com.compose.chi.presentation.navigation.components.BottomNavItem
import com.compose.chi.presentation.navigation.components.AppBottomNavigation
import com.compose.chi.presentation.navigation.AppNavHost
import com.compose.chi.presentation.navigation.Screen
import com.compose.chi.presentation.ui.theme.CHITheme

class MainActivity : ComponentActivity(), AnalyticsLogger by AnalyticsLoggerImpl() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Call AnalyticsLogger Interface function
        registerLifecycleOwner(this)

        setContent {
            CHITheme {
                val navController = rememberNavController()
                Scaffold(
                    bottomBar = {
                        AppBottomNavigation(
                            items = listOf(
                                BottomNavItem(
                                    name = "Random Joke",
                                    route = Screen.FirstTabScreen.route,
                                    icon = Icons.Default.Home
                                ),
                                BottomNavItem(
                                    name = "Ten Jokes",
                                    route = Screen.TenJokesScreen.route,
                                    icon = Icons.Default.List,
                                    badgeCount = 10
                                )
                            ),
                            navController = navController,
                            onItemClick = {
                                navController.navigate(it.route)
                            }
                        )
                    }
                ){ paddingValues ->
                    AppNavHost(
                        navController = navController,
                        modifier = Modifier.padding(paddingValues)
                    )
                }
            }
        }
    }
}