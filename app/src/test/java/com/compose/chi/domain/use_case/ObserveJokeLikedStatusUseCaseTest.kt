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

class ObserveJokeLikedStatusUseCaseTest {

    @Test
    fun `returns repository flow for the given joke id`() = runTest {
        val jokeId = 7
        val repository: JokeRepository = mockk {
            every { observeJokeLikedStatus(jokeId) } returns flowOf(true)
        }

        ObserveJokeLikedStatusUseCase(repository)(jokeId).test {
            assertEquals(true, awaitItem())
            awaitComplete()
        }
        verify(exactly = 1) { repository.observeJokeLikedStatus(jokeId) }
    }

    @Test
    fun `propagates errors from the repository flow`() = runTest {
        val repository: JokeRepository = mockk {
            every { observeJokeLikedStatus(any()) } returns flow { throw IllegalStateException("boom") }
        }

        ObserveJokeLikedStatusUseCase(repository)(1).test {
            assertTrue(awaitError() is IllegalStateException)
        }
    }
}
