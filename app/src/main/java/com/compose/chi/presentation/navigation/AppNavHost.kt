package com.compose.chi.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.compose.chi.ChiApp
import com.compose.chi.domain.use_case.GetJokeUseCase
import com.compose.chi.domain.use_case.GetTenJokesUseCase
import com.compose.chi.presentation.helpers.viewModelFactory
import com.compose.chi.presentation.screens.joke_home_page.JokeHomeScreen
import com.compose.chi.presentation.screens.joke_home_page.JokeHomeViewModel
import com.compose.chi.presentation.screens.then_jokes_page.TenJokesScreen
import com.compose.chi.presentation.screens.then_jokes_page.TenJokesViewModel

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    // Nested Navigation Graph
    NavHost(
        navController = navController,
        startDestination = "first_tab",
        modifier = modifier
    ) {

        // Screen without a navigation
        composable("first_tab") {
            val homeViewModel = viewModel<JokeHomeViewModel>(
                factory = viewModelFactory {
                    val getJokeUseCase: GetJokeUseCase = GetJokeUseCase(ChiApp.appModule.jokeRepository)
                    JokeHomeViewModel(getJokeUseCase)
                }
            )
            JokeHomeScreen(
                navController = navController,
                viewModel = homeViewModel
            )
        }

        // Screen with a navigation, a navigation inside a nav
        navigation(
            startDestination = "ten_jokes",
            route = "second_tab"
        ) {
            composable("ten_jokes") {
                val tenJokesViewModel = viewModel<TenJokesViewModel>(
                    factory = viewModelFactory {
                        val getTenJokesUseCase: GetTenJokesUseCase = GetTenJokesUseCase(ChiApp.appModule.jokeRepository)
                        TenJokesViewModel(getTenJokesUseCase)
                    }
                )
                TenJokesScreen(
                    navController = navController,
                    viewModel = tenJokesViewModel
                )
            }
            composable("home2") {
                val homeViewModel = viewModel<JokeHomeViewModel>(
                    factory = viewModelFactory {
                        val getJokeUseCase: GetJokeUseCase = GetJokeUseCase(ChiApp.appModule.jokeRepository)
                        JokeHomeViewModel(getJokeUseCase)
                    }
                )
                JokeHomeScreen(
                    navController = navController,
                    viewModel = homeViewModel
                )
            }
        }
    }
}