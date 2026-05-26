package com.compose.chi.testing

import com.compose.chi.data.remote.dto.JokeDto

/**
 * Canonical [JokeDto] samples built from the domain [TestJokes] fixtures.
 */
object JokeDtos {

    val first: JokeDto = JokeDto(
        id = TestJokes.joke1.id,
        setup = TestJokes.joke1.setup,
        punchline = TestJokes.joke1.punchline,
        type = TestJokes.joke1.type
    )

    val second: JokeDto = JokeDto(
        id = TestJokes.joke2.id,
        setup = TestJokes.joke2.setup,
        punchline = TestJokes.joke2.punchline,
        type = TestJokes.joke2.type
    )

    fun ten(): List<JokeDto> = TestJokes.tenJokes().map { joke ->
        JokeDto(
            id = joke.id,
            setup = joke.setup,
            punchline = joke.punchline,
            type = joke.type
        )
    }
}
