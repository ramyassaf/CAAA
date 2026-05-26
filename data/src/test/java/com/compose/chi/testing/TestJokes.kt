package com.compose.chi.testing

import com.compose.chi.data.database.model.JokeEntity
import com.compose.chi.data.remote.dto.JokeDto
import com.compose.chi.domain.model.Joke

object TestJokes {

    val joke1: Joke = Joke(
        id = 1,
        setup = "Why did the chicken cross the road?",
        punchline = "To get to the other side.",
        type = "general",
        isFavourite = false
    )

    val joke1Favourite: Joke = joke1.copy(isFavourite = true)

    val joke2: Joke = Joke(
        id = 2,
        setup = "What do you call a fish with no eyes?",
        punchline = "Fsh.",
        type = "general",
        isFavourite = false
    )

    val joke2Favourite: Joke = joke2.copy(isFavourite = true)

    val jokeDto1: JokeDto = JokeDto(
        id = joke1.id,
        setup = joke1.setup,
        punchline = joke1.punchline,
        type = joke1.type
    )

    val jokeDto2: JokeDto = JokeDto(
        id = joke2.id,
        setup = joke2.setup,
        punchline = joke2.punchline,
        type = joke2.type
    )

    val jokeEntity1: JokeEntity = JokeEntity(
        id = joke1.id,
        setup = joke1.setup,
        punchline = joke1.punchline,
        type = joke1.type,
        isFavourite = false
    )

    val jokeEntity1Favourite: JokeEntity = jokeEntity1.copy(isFavourite = true)

    val jokeEntity2Favourite: JokeEntity = JokeEntity(
        id = joke2.id,
        setup = joke2.setup,
        punchline = joke2.punchline,
        type = joke2.type,
        isFavourite = true
    )

    fun tenJokes(): List<Joke> = (1..10).map { index ->
        Joke(
            id = index,
            setup = "Setup $index",
            punchline = "Punchline $index",
            type = "general",
            isFavourite = false
        )
    }

    fun tenJokeDtos(): List<JokeDto> = tenJokes().map { joke ->
        JokeDto(
            id = joke.id,
            setup = joke.setup,
            punchline = joke.punchline,
            type = joke.type
        )
    }
}
