package com.compose.chi.data.database.model

import com.compose.chi.domain.model.Joke
import org.junit.Assert.assertEquals
import org.junit.Test

class JokeEntityMapperTest {

    @Test
    fun `Joke toJokeEntity preserves every field including isFavourite`() {
        val joke = Joke(
            id = 7,
            setup = "Setup",
            punchline = "Punchline",
            type = "general",
            isFavourite = true
        )

        val entity = joke.toJokeEntity()

        assertEquals(joke.id, entity.id)
        assertEquals(joke.setup, entity.setup)
        assertEquals(joke.punchline, entity.punchline)
        assertEquals(joke.type, entity.type)
        assertEquals(joke.isFavourite, entity.isFavourite)
    }

    @Test
    fun `JokeEntity toJoke preserves every field including isFavourite`() {
        val entity = JokeEntity(
            id = 11,
            setup = "Setup",
            punchline = "Punchline",
            type = "general",
            isFavourite = true
        )

        val joke = entity.toJoke()

        assertEquals(entity.id, joke.id)
        assertEquals(entity.setup, joke.setup)
        assertEquals(entity.punchline, joke.punchline)
        assertEquals(entity.type, joke.type)
        assertEquals(entity.isFavourite, joke.isFavourite)
    }
}
