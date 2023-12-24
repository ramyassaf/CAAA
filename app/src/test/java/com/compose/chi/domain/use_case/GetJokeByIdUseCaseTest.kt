import com.compose.chi.common.Resource
import com.compose.chi.data.remote.dto.JokeDto
import com.compose.chi.data.remote.dto.toJoke
import com.compose.chi.domain.model.Joke
import com.compose.chi.domain.repository.JokeRepository
import com.compose.chi.domain.use_case.GetJokeByIdUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import okhttp3.MediaType.Companion.toMediaType
import org.junit.Assert.assertTrue

@ExperimentalCoroutinesApi
class GetJokeByIdUseCaseTest {

    @Mock
    private lateinit var mockRepository: JokeRepository

    private lateinit var getJokeByIdUseCase: GetJokeByIdUseCase

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        getJokeByIdUseCase = GetJokeByIdUseCase(mockRepository)
    }

    @Test
    fun `get joke by id success`() = runTest {
        // Arrange
        val jokeId = 1
        val mockJoke = JokeDto(id = jokeId, punchline = "Test joke", setup = "joke setup", type = "joke type")
        `when`(mockRepository.getJokeById(jokeId)).thenReturn(mockJoke)

        // Act
        val result = getJokeByIdUseCase(jokeId).first()

        // Assert
        assertTrue(result is Resource.Success<Joke>)
        assertEquals(mockJoke.toJoke(), (result as Resource.Success<Joke>).data)
    }

    @Test
    fun `get joke by id HTTP error`() = runTest {
        // Arrange
        val jokeId = 1
        val expectedErrorMessage = "HTTP 404 Response.error()" // Adjust this based on the actual error message
        val contentType = "text/plain".toMediaType()
        val errorResponseBody = expectedErrorMessage.toResponseBody(contentType)
        val response: Response<Any> = Response.error(404, errorResponseBody)

        `when`(mockRepository.getJokeById(jokeId)).thenThrow(HttpException(response))

        // Act
        val result = getJokeByIdUseCase(jokeId).first()

        // Assert
        assertTrue(result is Resource.Error<Joke>)
        assertEquals(expectedErrorMessage, (result as Resource.Error<Joke>).message)
    }

    @Test
    fun `get joke by id network error`() = runTest {
        // Arrange
        val jokeId = 1
        val mockRepository: JokeRepository = mockk {
            coEvery { getJokeById(jokeId) } throws IOException()
        }

        // Act
        val result = GetJokeByIdUseCase(mockRepository)(jokeId).first()

        // Assert
        assertTrue(result is Resource.Error<Joke>)
        assertEquals("Couldn't reach server. Check your internet connection.", (result as Resource.Error<Joke>).message)
    }
}
