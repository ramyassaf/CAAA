package com.compose.chi.di

import com.compose.chi.domain.use_case.DeleteAllJokesUseCase
import com.compose.chi.domain.use_case.GetJokeByIdUseCase
import com.compose.chi.domain.use_case.GetJokeUseCase
import com.compose.chi.domain.use_case.GetTenJokesUseCase
import com.compose.chi.domain.use_case.ObserveJokeLikedStatusUseCase
import com.compose.chi.domain.use_case.ObserveLikedJokesUseCase
import com.compose.chi.domain.use_case.UpsertJokeUseCase
import com.compose.chi.presentation.screens.joke_details_page.JokeDetailsViewModel
import com.compose.chi.presentation.screens.joke_home_page.JokeHomeViewModel
import com.compose.chi.presentation.screens.my_favourite_jokes_page.MyFavouriteJokesViewModel
import com.compose.chi.presentation.screens.ten_jokes_page.TenJokesViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appKoinModule = module {
    factory { GetJokeUseCase(get()) }
    factory { GetTenJokesUseCase(get()) }
    factory { GetJokeByIdUseCase(get()) }
    factory { ObserveLikedJokesUseCase(get()) }
    factory { ObserveJokeLikedStatusUseCase(get()) }
    factory { UpsertJokeUseCase(get()) }
    factory { DeleteAllJokesUseCase(get()) }

    viewModel { JokeHomeViewModel(get(), get(), get()) }
    viewModel { TenJokesViewModel(get()) }
    viewModel { MyFavouriteJokesViewModel(get(), get()) }
    viewModel { JokeDetailsViewModel(get(), get(), get(), get()) }
}
