package com.compose.chi.data.remote.dto

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class JokeDtoMapperTest {

    @Test
    fun `toJoke maps every field and defaults isFavourite to false`() {
        val dto = JokeDto(
            id = 42,
            setup = "Why don't scientists trust atoms?",
            punchline = "Because they make up everything.",
            type = "general"
        )

        val joke = dto.toJoke()

        assertEquals(dto.id, joke.id)
        assertEquals(dto.setup, joke.setup)
        assertEquals(dto.punchline, joke.punchline)
        assertEquals(dto.type, joke.type)
        assertFalse("DTO has no favourite info; mapper must default to false", joke.isFavourite)
    }
}
