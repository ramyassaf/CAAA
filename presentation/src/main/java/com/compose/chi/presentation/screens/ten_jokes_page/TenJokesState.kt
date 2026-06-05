package com.compose.chi.presentation.screens.ten_jokes_page

import com.compose.chi.domain.model.Joke

data class TenJokesState(
    val isLoading: Boolean = false,
    val jokes: List<Joke> = emptyList(),
    val error: String = ""
)
