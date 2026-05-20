package com.compose.chi.domain.use_case

import app.cash.turbine.test
import com.compose.chi.domain.repository.JokeRepository
import com.compose.chi.domain.result.DomainError
import com.compose.chi.domain.result.Resource
import com.compose.chi.testing.TestJokes
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ObserveLikedJokesUseCaseTest {

    @Test
    fun `returns the repository liked jokes flow`() = runTest {
        val likedJokes = listOf(TestJokes.joke1Favourite, TestJokes.joke2Favourite)
        val repository: JokeRepository = mockk {
            every { observeLikedJokes() } returns flowOf(Resource.Success(likedJokes))
        }

        ObserveLikedJokesUseCase(repository)().test {
            assertEquals(Resource.Success(likedJokes), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `returns repository error results unchanged`() = runTest {
        val repository: JokeRepository = mockk {
            every { observeLikedJokes() } returns flowOf(Resource.Error(DomainError.Persistence))
        }

        ObserveLikedJokesUseCase(repository)().test {
            assertEquals(Resource.Error(DomainError.Persistence), awaitItem())
            awaitComplete()
        }
    }
}
