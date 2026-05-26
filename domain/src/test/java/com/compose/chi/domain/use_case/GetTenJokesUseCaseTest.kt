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

class GetTenJokesUseCaseTest {

    @Test
    fun `returns repository result unchanged`() = runTest {
        val expectedResult = Resource.Success(TestJokes.tenJokes())
        val repository: JokeRepository = mockk {
            coEvery { getTenJokes() } returns expectedResult
        }

        val result = GetTenJokesUseCase(repository)()

        assertSame(expectedResult, result)
        coVerify(exactly = 1) { repository.getTenJokes() }
    }
}
