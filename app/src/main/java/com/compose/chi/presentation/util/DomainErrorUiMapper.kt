package com.compose.chi.presentation.util

import com.compose.chi.domain.result.DomainError

fun DomainError.toUiMessage(): String = when (this) {
    DomainError.Network -> "Couldn't reach server. Check your internet connection."
    DomainError.NotFound -> "Joke not found."
    DomainError.Server -> "An unexpected server error occurred."
    DomainError.Persistence -> "Couldn't update saved jokes. Please try again."
    DomainError.Unknown -> "An unexpected error occurred."
}
