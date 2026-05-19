package com.compose.chi.domain.use_case

import com.compose.chi.domain.repository.JokeRepository
import com.compose.chi.testing.TestJokes
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertThrows
import org.junit.Test

class UpsertJokeUseCaseTest {

    @Test
    fun `delegates exactly once with the given joke`() = runTest {
        val joke = TestJokes.joke1Favourite
        val repository: JokeRepository = mockk {
            coEvery { upsertJoke(joke) } just Runs
        }

        UpsertJokeUseCase(repository)(joke)

        coVerify(exactly = 1) { repository.upsertJoke(joke) }
    }

    @Test
    fun `propagates exceptions from the repository`() = runTest {
        val joke = TestJokes.joke1
        val repository: JokeRepository = mockk {
            coEvery { upsertJoke(joke) } throws IllegalStateException("write failed")
        }
        val useCase = UpsertJokeUseCase(repository)

        assertThrows(IllegalStateException::class.java) {
            kotlinx.coroutines.runBlocking { useCase(joke) }
        }
    }
}
