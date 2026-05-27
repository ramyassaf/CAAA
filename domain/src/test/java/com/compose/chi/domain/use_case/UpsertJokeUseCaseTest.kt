package com.compose.chi.domain.use_case

import com.compose.chi.domain.repository.JokeRepository
import com.compose.chi.domain.result.DomainError
import com.compose.chi.domain.result.Resource
import com.compose.chi.testing.TestJokes
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class UpsertJokeUseCaseTest {

    @Test
    fun `delegates exactly once with the given joke`() = runTest {
        val joke = TestJokes.joke1Favourite
        val repository: JokeRepository = mockk {
            coEvery { upsertJoke(joke) } returns Resource.Success(Unit)
        }

        val result = UpsertJokeUseCase(repository)(joke)

        assertEquals(Resource.Success(Unit), result)
        coVerify(exactly = 1) { repository.upsertJoke(joke) }
    }

    @Test
    fun `returns repository error result unchanged`() = runTest {
        val joke = TestJokes.joke1
        val repository: JokeRepository = mockk {
            coEvery { upsertJoke(joke) } returns Resource.Error(DomainError.Persistence)
        }
        val useCase = UpsertJokeUseCase(repository)

        assertEquals(Resource.Error(DomainError.Persistence), useCase(joke))
    }
}
