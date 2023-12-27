package com.compose.chi.domain.model

data class Joke(
    val id: Int,
    val punchline: String,
    val setup: String,
    val type: String,
    var isFavourite: Boolean = false
)