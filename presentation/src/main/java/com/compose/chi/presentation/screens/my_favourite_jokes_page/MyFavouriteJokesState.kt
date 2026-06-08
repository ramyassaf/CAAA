package com.compose.chi.presentation.screens.my_favourite_jokes_page

import com.compose.chi.domain.model.Joke

data class MyFavouriteJokesState(
    val jokes: List<Joke> = emptyList(),
    val error: String = ""
)
