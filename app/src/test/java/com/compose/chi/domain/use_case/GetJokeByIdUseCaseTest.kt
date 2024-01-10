package com.compose.chi.domain.use_case

import com.compose.chi.common.Resource
import com.compose.chi.data.remote.dto.JokeDto
import com.compose.chi.domain.model.Joke
import com.compose.chi.domain.repository.JokeRepository
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

class GetJokeByIdUseCaseTest {

    @Test
    fun `get joke by id success`() = runTest {
        // Arrange
        val jokeId = "1"
        val mockJokeDto = JokeDto(
            id = jokeId.toInt(),
            punchline = "Test joke",
            setup = "joke setup",
            type = "joke type"
        )
        val mockRepository: JokeRepository = mockk {
            coEvery { getJokeById(jokeId) } returns mockJokeDto.toJoke()
        }

        // Act
        val result = GetJokeByIdUseCase(mockRepository)(jokeId).toList()

        // Assert
        assertEquals(2, result.size) // Expect loading and success states
        assertTrue(result[0] is Resource.Loading<Joke>)
        assertTrue(result[1] is Resource.Success<Joke>)
        assertEquals(mockJokeDto.toJoke(), (result[1] as Resource.Success<Joke>).data)
    }


    @Test
    fun `get joke by id HTTP error`() = runTest {
        // Arrange
        val jokeId = "1"
        val expectedErrorMessage =
            "HTTP 404 Response.error()" // Adjust this based on the actual error message
        val contentType = "text/plain".toMediaType()
        val errorResponseBody = expectedErrorMessage.toResponseBody(contentType)
        val response: Response<Any> = Response.error(404, errorResponseBody)
        val mockRepository: JokeRepository = mockk {
            coEvery { getJokeById(jokeId) } throws HttpException(response)
        }

        // Act
        val result = GetJokeByIdUseCase(mockRepository)(jokeId).toList()

        // Print the result for debugging
        println("Actual result: $result")

        // Assert
        assertEquals(2, result.size) // Expect loading and error states
        assertTrue(result[0] is Resource.Loading<Joke>)
        assertTrue(result[1] is Resource.Error<Joke>)
        assertEquals(expectedErrorMessage, (result[1] as Resource.Error<Joke>).message)
    }


    @Test
    fun `get joke by id network error`() = runTest {
        // Arrange
        val jokeId = "1"
        val mockRepository: JokeRepository = mockk {
            coEvery { getJokeById(jokeId) } throws IOException()
        }

        // Act
        val result =
            GetJokeByIdUseCase(mockRepository)(jokeId).toList() // Collect the flow into a list

        // Print the result for debugging
        println("Actual result: $result")

        // Assert
        assertEquals(2, result.size) // Expect loading and error states
        assertTrue(result[0] is Resource.Loading<Joke>)
        assertTrue(result[1] is Resource.Error<Joke>)
        assertEquals(
            "Couldn't reach server. Check your internet connection.",
            (result[1] as Resource.Error<Joke>).message
        )
    }
}
