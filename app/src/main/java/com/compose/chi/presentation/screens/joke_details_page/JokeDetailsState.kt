package com.compose.chi.presentation.screens.joke_details_page

import com.compose.chi.domain.model.Joke

data class JokeDetailsState(
    val isLoading: Boolean = false,
    val joke: Joke? = null,
    val error: String = ""
)
