package com.compose.chi.domain.use_case

import app.cash.turbine.test
import com.compose.chi.domain.repository.JokeRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class IsJokeLikedUseCaseTest {

    @Test
    fun `returns repository flow for the given joke id`() = runTest {
        val jokeId = 7
        val repository: JokeRepository = mockk {
            every { isJokeLiked(jokeId) } returns flowOf(true)
        }

        IsJokeLikedUseCase(repository)(jokeId).test {
            assertEquals(true, awaitItem())
            awaitComplete()
        }
        verify(exactly = 1) { repository.isJokeLiked(jokeId) }
    }

    @Test
    fun `propagates errors from the repository flow`() = runTest {
        val repository: JokeRepository = mockk {
            every { isJokeLiked(any()) } returns flow { throw IllegalStateException("boom") }
        }

        IsJokeLikedUseCase(repository)(1).test {
            assertTrue(awaitError() is IllegalStateException)
        }
    }
}
