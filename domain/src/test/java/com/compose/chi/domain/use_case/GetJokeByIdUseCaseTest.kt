package com.compose.chi.domain.use_case

import com.compose.chi.domain.repository.JokeRepository
import com.compose.chi.domain.result.Resource
import com.compose.chi.testing.TestJokes
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertSame
import org.junit.Test

class GetJokeByIdUseCaseTest {

    @Test
    fun `returns repository result unchanged and passes requested id`() = runTest {
        val jokeId = TestJokes.joke1.id.toString()
        val expectedResult = Resource.Success(TestJokes.joke1)
        val repository: JokeRepository = mockk {
            coEvery { getJokeById(jokeId) } returns expectedResult
        }

        val result = GetJokeByIdUseCase(repository)(jokeId)

        assertSame(expectedResult, result)
        coVerify(exactly = 1) { repository.getJokeById(jokeId) }
    }
}
