package com.compose.chi.presentation.screens.joke_home_page

import com.compose.chi.domain.model.Joke

data class JokeHomeState(
    val isLoading: Boolean = false,
    val joke: Joke? = null,
    val error: String = ""
)
