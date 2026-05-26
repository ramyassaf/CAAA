package com.compose.chi.testing

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

    fun tenJokes(): List<Joke> = (1..10).map { index ->
        Joke(
            id = index,
            setup = "Setup $index",
            punchline = "Punchline $index",
            type = "general",
            isFavourite = false
        )
    }
}
