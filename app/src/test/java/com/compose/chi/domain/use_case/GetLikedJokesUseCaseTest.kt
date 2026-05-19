package com.compose.chi.domain.use_case

import app.cash.turbine.test
import com.compose.chi.domain.model.Joke
import com.compose.chi.domain.repository.JokeRepository
import com.compose.chi.testing.TestJokes
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetLikedJokesUseCaseTest {

    @Test
    fun `returns the repository liked jokes flow`() = runTest {
        val likedJokes = listOf(TestJokes.joke1Favourite, TestJokes.joke2Favourite)
        val repository: JokeRepository = mockk {
            every { getLikedJokes() } returns flowOf(likedJokes)
        }

        GetLikedJokesUseCase(repository)().test {
            assertEquals(likedJokes, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `propagates errors from the repository flow`() = runTest {
        val failure = IllegalStateException("db unavailable")
        val repository: JokeRepository = mockk {
            every { getLikedJokes() } returns flow<List<Joke>> { throw failure }
        }

        GetLikedJokesUseCase(repository)().test {
            assertTrue(awaitError() is IllegalStateException)
        }
    }
}
