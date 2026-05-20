package com.compose.chi.domain.use_case

import app.cash.turbine.test
import com.compose.chi.domain.repository.JokeRepository
import com.compose.chi.domain.result.DomainError
import com.compose.chi.domain.result.Resource
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ObserveJokeLikedStatusUseCaseTest {

    @Test
    fun `returns repository flow for the given joke id`() = runTest {
        val jokeId = 7
        val repository: JokeRepository = mockk {
            every { observeJokeLikedStatus(jokeId) } returns flowOf(Resource.Success(true))
        }

        ObserveJokeLikedStatusUseCase(repository)(jokeId).test {
            assertEquals(Resource.Success(true), awaitItem())
            awaitComplete()
        }
        verify(exactly = 1) { repository.observeJokeLikedStatus(jokeId) }
    }

    @Test
    fun `returns repository error results unchanged`() = runTest {
        val repository: JokeRepository = mockk {
            every { observeJokeLikedStatus(any()) } returns flowOf(Resource.Error(DomainError.Persistence))
        }

        ObserveJokeLikedStatusUseCase(repository)(1).test {
            assertEquals(Resource.Error(DomainError.Persistence), awaitItem())
            awaitComplete()
        }
    }
}
