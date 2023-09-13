package com.compose.chi.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.compose.chi.ChiApp
import com.compose.chi.analytics.AnalyticsLogger
import com.compose.chi.analytics.AnalyticsLoggerImpl
import com.compose.chi.domain.use_case.GetJokeUseCase
import com.compose.chi.presentation.helpers.viewModelFactory
import com.compose.chi.presentation.screens.joke_home_page.JokeHomeScreen
import com.compose.chi.presentation.screens.joke_home_page.JokeHomeViewModel
import com.compose.chi.presentation.ui.theme.CHITheme

class MainActivity : ComponentActivity(), AnalyticsLogger by AnalyticsLoggerImpl() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Call AnalyticsLogger Interface function
        registerLifecycleOwner(this)

        setContent {
            CHITheme {
                val homeViewModel = viewModel<JokeHomeViewModel>(
                    factory = viewModelFactory {
                        val getJokeUseCase: GetJokeUseCase = GetJokeUseCase(ChiApp.appModule.jokeRepository)
                        JokeHomeViewModel(getJokeUseCase)
                    }
                )
                JokeHomeScreen(viewModel = homeViewModel)
            }
        }
    }
}