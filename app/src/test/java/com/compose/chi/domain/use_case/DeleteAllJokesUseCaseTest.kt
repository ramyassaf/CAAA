package com.compose.chi.domain.use_case

import com.compose.chi.domain.repository.JokeRepository
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertThrows
import org.junit.Test

class DeleteAllJokesUseCaseTest {

    @Test
    fun `delegates exactly once to repository`() = runTest {
        val repository: JokeRepository = mockk {
            coEvery { deleteAllJokes() } just Runs
        }

        DeleteAllJokesUseCase(repository)()

        coVerify(exactly = 1) { repository.deleteAllJokes() }
    }

    @Test
    fun `propagates exceptions from the repository`() = runTest {
        val repository: JokeRepository = mockk {
            coEvery { deleteAllJokes() } throws IllegalStateException("delete failed")
        }
        val useCase = DeleteAllJokesUseCase(repository)

        assertThrows(IllegalStateException::class.java) {
            kotlinx.coroutines.runBlocking { useCase() }
        }
    }
}
