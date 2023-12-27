package com.compose.chi.data.remote.dto

import com.compose.chi.domain.model.Joke

data class JokeDto(
    val id: Int,
    val punchline: String,
    val setup: String,
    val type: String
) {
    fun toJoke(): Joke {
        return Joke (
            punchline = punchline,
            setup = setup,
            type = type,
            id = id
        )
    }
}