package com.compose.chi.domain.use_case

import com.compose.chi.domain.repository.JokeRepository
import com.compose.chi.domain.result.DomainError
import com.compose.chi.domain.result.Resource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class DeleteAllJokesUseCaseTest {

    @Test
    fun `delegates exactly once to repository`() = runTest {
        val repository: JokeRepository = mockk {
            coEvery { deleteAllJokes() } returns Resource.Success(Unit)
        }

        val result = DeleteAllJokesUseCase(repository)()

        assertEquals(Resource.Success(Unit), result)
        coVerify(exactly = 1) { repository.deleteAllJokes() }
    }

    @Test
    fun `returns repository error result unchanged`() = runTest {
        val repository: JokeRepository = mockk {
            coEvery { deleteAllJokes() } returns Resource.Error(DomainError.Persistence)
        }
        val useCase = DeleteAllJokesUseCase(repository)

        assertEquals(Resource.Error(DomainError.Persistence), useCase())
    }
}
