package com.compose.chi.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.compose.chi.ChiApplication
import com.compose.chi.data.database.JokeDao
import com.compose.chi.domain.use_case.GetJokeUseCase
import com.compose.chi.domain.use_case.GetTenJokesUseCase
import com.compose.chi.presentation.helpers.viewModelFactory
import com.compose.chi.presentation.screens.joke_details_page.JokeDetailsScreen
import com.compose.chi.presentation.screens.joke_details_page.JokeDetailsViewModel
import com.compose.chi.presentation.screens.joke_home_page.JokeHomeScreen
import com.compose.chi.presentation.screens.joke_home_page.JokeHomeViewModel
import com.compose.chi.presentation.screens.my_favourite_jokes_page.MyFavouriteJokesScreen
import com.compose.chi.presentation.screens.my_favourite_jokes_page.MyFavouriteJokesViewModel
import com.compose.chi.presentation.screens.ten_jokes_page.TenJokesScreen
import com.compose.chi.presentation.screens.ten_jokes_page.TenJokesViewModel

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    // Nested Navigation Graph
    NavHost(
        navController = navController,
        startDestination = Screen.FirstTabScreen.route,
        modifier = modifier
    ) {

        // Screen without a navigation
        composable(
            route = Screen.FirstTabScreen.route
        ) {
            val homeViewModel = viewModel<JokeHomeViewModel>(
                factory = viewModelFactory {
                    val getJokeUseCase: GetJokeUseCase = GetJokeUseCase(ChiApplication.appModule.jokeRepository)
                    val jokeDao: JokeDao = ChiApplication.appModule.db.dao
                    JokeHomeViewModel(getJokeUseCase, jokeDao)
                }
            )
            JokeHomeScreen(
                navController = navController,
                viewModel = homeViewModel
            )
        }

        // Screen with a navigation, a navigation inside a nav
        navigation(
            startDestination = Screen.TenJokesScreen.route,
            route = Screen.SecondTabNavigationScreen.route
        ) {

            composable(
                route = Screen.TenJokesScreen.route
            ) {
                val tenJokesViewModel = viewModel<TenJokesViewModel>(
                    factory = viewModelFactory {
                        val getTenJokesUseCase: GetTenJokesUseCase = GetTenJokesUseCase(ChiApplication.appModule.jokeRepository)
                        TenJokesViewModel(getTenJokesUseCase)
                    }
                )
                TenJokesScreen(
                    navController = navController,
                    viewModel = tenJokesViewModel
                )
            }
            composable(
                route = Screen.JokeDetails.route
            ) {
                val jokeDetailsViewModel = viewModel<JokeDetailsViewModel>(
                    factory = viewModelFactory {
                        val getJokeUseCase: GetJokeUseCase = GetJokeUseCase(ChiApplication.appModule.jokeRepository)
                        JokeDetailsViewModel(getJokeUseCase)
                    }
                )
                JokeDetailsScreen(
                    navController = navController,
                    viewModel = jokeDetailsViewModel
                )
            }
        }

        // Screen without a navigation
        composable(
            route = Screen.MyFavouriteJokesScreen.route
        ) {
            val myFavouriteJokesViewModel = viewModel<MyFavouriteJokesViewModel>(
                factory = viewModelFactory {
                    val jokeDao: JokeDao = ChiApplication.appModule.db.dao
                    MyFavouriteJokesViewModel(jokeDao)
                }
            )
            MyFavouriteJokesScreen(
                navController = navController,
                viewModel = myFavouriteJokesViewModel
            )
        }
    }
}