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

class GetJokeUseCaseTest {

    @Test
    fun `emits Loading then Success with joke from repository`() = runTest {
        val repository: JokeRepository = mockk {
            coEvery { getJoke() } returns TestJokes.joke1
        }

        val result = GetJokeUseCase(repository)().toList()

        assertEquals(2, result.size)
        assertTrue(result[0] is Resource.Loading<Joke>)
        assertTrue(result[1] is Resource.Success<Joke>)
        assertEquals(TestJokes.joke1, (result[1] as Resource.Success<Joke>).data)
    }

    @Test
    fun `emits Loading then Error on HttpException`() = runTest {
        val errorMessage = "HTTP 404 Response.error()"
        val response: Response<Any> = Response.error(
            404,
            errorMessage.toResponseBody("text/plain".toMediaType())
        )
        val repository: JokeRepository = mockk {
            coEvery { getJoke() } throws HttpException(response)
        }

        val result = GetJokeUseCase(repository)().toList()

        assertEquals(2, result.size)
        assertTrue(result[0] is Resource.Loading<Joke>)
        assertTrue(result[1] is Resource.Error<Joke>)
        assertEquals(errorMessage, (result[1] as Resource.Error<Joke>).message)
    }

    @Test
    fun `emits Loading then Error with network message on IOException`() = runTest {
        val repository: JokeRepository = mockk {
            coEvery { getJoke() } throws IOException()
        }

        val result = GetJokeUseCase(repository)().toList()

        assertEquals(2, result.size)
        assertTrue(result[0] is Resource.Loading<Joke>)
        assertTrue(result[1] is Resource.Error<Joke>)
        assertEquals(
            "Couldn't reach server. Check your internet connection.",
            (result[1] as Resource.Error<Joke>).message
        )
    }
}
