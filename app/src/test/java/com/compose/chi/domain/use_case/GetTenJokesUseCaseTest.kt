package com.compose.chi.domain.use_case

import com.compose.chi.common.Resource
import com.compose.chi.domain.model.Joke
import com.compose.chi.domain.repository.JokeRepository
import com.compose.chi.testing.TestJokes
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class GetTenJokesUseCaseTest {

    @Test
    fun `emits Loading then Success with jokes from repository`() = runTest {
        val jokes = TestJokes.tenJokes()
        val repository: JokeRepository = mockk {
            coEvery { getTenJokes() } returns jokes
        }

        val result = GetTenJokesUseCase(repository)().toList()

        assertEquals(2, result.size)
        assertTrue(result[0] is Resource.Loading<List<Joke>>)
        assertTrue(result[1] is Resource.Success<List<Joke>>)
        assertEquals(jokes, (result[1] as Resource.Success<List<Joke>>).data)
    }

    @Test
    fun `emits Loading then Error on HttpException`() = runTest {
        val errorMessage = "HTTP 500 Response.error()"
        val response: Response<Any> = Response.error(
            500,
            errorMessage.toResponseBody("text/plain".toMediaType())
        )
        val repository: JokeRepository = mockk {
            coEvery { getTenJokes() } throws HttpException(response)
        }

        val result = GetTenJokesUseCase(repository)().toList()

        assertEquals(2, result.size)
        assertTrue(result[0] is Resource.Loading<List<Joke>>)
        assertTrue(result[1] is Resource.Error<List<Joke>>)
        assertEquals(errorMessage, (result[1] as Resource.Error<List<Joke>>).message)
    }

    @Test
    fun `emits Loading then Error with network message on IOException`() = runTest {
        val repository: JokeRepository = mockk {
            coEvery { getTenJokes() } throws IOException()
        }

        val result = GetTenJokesUseCase(repository)().toList()

        assertEquals(2, result.size)
        assertTrue(result[0] is Resource.Loading<List<Joke>>)
        assertTrue(result[1] is Resource.Error<List<Joke>>)
        assertEquals(
            "Couldn't reach server. Check your internet connection.",
            (result[1] as Resource.Error<List<Joke>>).message
        )
    }
}
