package com.compose.chi.domain.model

import com.compose.chi.data.database.model.JokeEntity

data class Joke(
    val id: Int,
    val punchline: String,
    val setup: String,
    val type: String,
    var isFavourite: Boolean = false
)

fun Joke.toJokeEntity(): JokeEntity {
    return JokeEntity (
        punchline = punchline,
        setup = setup,
        type = type,
        isFavourite = isFavourite,
        id = id
    )
}