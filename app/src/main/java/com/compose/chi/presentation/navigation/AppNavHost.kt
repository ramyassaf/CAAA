package com.compose.chi.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
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
            val homeViewModel = viewModel<JokeHomeViewModel>(factory = JokeHomeViewModel.Factory)
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
                val tenJokesViewModel = viewModel<TenJokesViewModel>(factory = TenJokesViewModel.Factory)
                TenJokesScreen(
                    navController = navController,
                    viewModel = tenJokesViewModel
                )
            }
            composable(
                route = Screen.JokeDetails.route + "/{jokeId}"
            ) {
                val jokeDetailsViewModel = viewModel<JokeDetailsViewModel>(factory = JokeDetailsViewModel.Factory)
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
            val myFavouriteJokesViewModel = viewModel<MyFavouriteJokesViewModel>(factory = MyFavouriteJokesViewModel.Factory)
            MyFavouriteJokesScreen(
                navController = navController,
                viewModel = myFavouriteJokesViewModel
            )
        }
    }
}