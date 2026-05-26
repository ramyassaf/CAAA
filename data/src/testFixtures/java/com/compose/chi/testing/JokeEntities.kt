package com.compose.chi.testing

import com.compose.chi.data.database.model.JokeEntity

/**
 * Canonical [JokeEntity] samples built from the domain [TestJokes] fixtures.
 */
object JokeEntities {

    val first: JokeEntity = JokeEntity(
        id = TestJokes.joke1.id,
        setup = TestJokes.joke1.setup,
        punchline = TestJokes.joke1.punchline,
        type = TestJokes.joke1.type,
        isFavourite = false
    )

    val firstFavourite: JokeEntity = first.copy(isFavourite = true)

    val secondFavourite: JokeEntity = JokeEntity(
        id = TestJokes.joke2.id,
        setup = TestJokes.joke2.setup,
        punchline = TestJokes.joke2.punchline,
        type = TestJokes.joke2.type,
        isFavourite = true
    )
}
