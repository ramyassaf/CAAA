package com.compose.chi.data.repository

import app.cash.turbine.test
import com.compose.chi.data.database.JokeDao
import com.compose.chi.data.database.model.JokeEntity
import com.compose.chi.data.remote.JokeApi
import com.compose.chi.domain.result.DomainError
import com.compose.chi.domain.result.Resource
import com.compose.chi.testing.TestJokes
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException
import org.junit.Assert.fail

class JokeRepositoryImplTest {

    private lateinit var api: JokeApi
    private lateinit var dao: JokeDao
    private lateinit var repository: JokeRepositoryImpl

    @Before
    fun setUp() {
        api = mockk()
        dao = mockk()
        repository = JokeRepositoryImpl(api, dao)
    }

    // --- Remote -------------------------------------------------------------

    @Test
    fun `getJoke returns Success with mapped domain joke`() = runTest {
        coEvery { api.getJoke() } returns TestJokes.jokeDto1

        val result = repository.getJoke()

        assertEquals(Resource.Success(TestJokes.jokeDto1.toJoke()), result)
        coVerify(exactly = 1) { api.getJoke() }
    }

    @Test
    fun `getTenJokes returns Success with mapped domain jokes`() = runTest {
        val dtos = TestJokes.tenJokeDtos()
        coEvery { api.getTenJokes() } returns dtos

        val result = repository.getTenJokes()

        assertEquals(Resource.Success(dtos.map { it.toJoke() }), result)
        coVerify(exactly = 1) { api.getTenJokes() }
    }

    @Test
    fun `getJokeById returns Success and passes the id to the API`() = runTest {
        val jokeId = "42"
        val dto = TestJokes.jokeDto1.copy(id = 42)
        coEvery { api.getJokeById(jokeId) } returns dto

        val result = repository.getJokeById(jokeId)

        assertEquals(Resource.Success(dto.toJoke()), result)
        coVerify(exactly = 1) { api.getJokeById(jokeId) }
    }

    @Test
    fun `IOException maps to Network error`() = runTest {
        coEvery { api.getJoke() } throws IOException("offline")

        val result = repository.getJoke()

        assertEquals(Resource.Error(DomainError.Network), result)
    }

    @Test
    fun `HttpException 404 maps to NotFound error`() = runTest {
        coEvery { api.getJokeById("1") } throws httpException(404)

        val result = repository.getJokeById("1")

        assertEquals(Resource.Error(DomainError.NotFound), result)
    }

    @Test
    fun `HttpException 5xx maps to Server error`() = runTest {
        coEvery { api.getTenJokes() } throws httpException(503)

        val result = repository.getTenJokes()

        assertEquals(Resource.Error(DomainError.Server), result)
    }

    @Test
    fun `other HttpException maps to Unknown error`() = runTest {
        coEvery { api.getJoke() } throws httpException(400)

        val result = repository.getJoke()

        assertEquals(Resource.Error(DomainError.Unknown), result)
    }

    @Test
    fun `unknown RuntimeException maps to Unknown error`() = runTest {
        coEvery { api.getJoke() } throws RuntimeException("boom")

        val result = repository.getJoke()

        assertEquals(Resource.Error(DomainError.Unknown), result)
    }

    @Test
    fun `CancellationException is rethrown instead of converted to Error`() = runTest {
        coEvery { api.getJoke() } throws CancellationException("cancelled")

        try {
            repository.getJoke()
            fail("Expected CancellationException")
        } catch (e: CancellationException) {
            assertEquals("cancelled", e.message)
        }
    }

    // --- Local --------------------------------------------------------------

    @Test
    fun `observeLikedJokes maps DAO entities into domain jokes`() = runTest {
        val entities = listOf(
            TestJokes.jokeEntity1Favourite,
            TestJokes.jokeEntity2Favourite
        )
        every { dao.observeAllLikedJokes() } returns flowOf(entities)

        repository.observeLikedJokes().test {
            val emitted = awaitItem()
            assertEquals(entities.map { it.toJoke() }, emitted)
            awaitComplete()
        }
        verify(exactly = 1) { dao.observeAllLikedJokes() }
    }

    @Test
    fun `observeLikedJokes preserves isFavourite true on every emitted joke`() = runTest {
        val entities = listOf(
            TestJokes.jokeEntity1Favourite,
            TestJokes.jokeEntity2Favourite
        )
        every { dao.observeAllLikedJokes() } returns flowOf(entities)

        repository.observeLikedJokes().test {
            val emitted = awaitItem()
            assertTrue("every liked joke must report isFavourite = true",
                emitted.isNotEmpty() && emitted.all { it.isFavourite })
            awaitComplete()
        }
    }

    @Test
    fun `observeJokeLikedStatus returns the DAO flow for the requested id`() = runTest {
        val jokeId = 5
        every { dao.observeFavoriteJoke(jokeId) } returns flowOf(true)

        repository.observeJokeLikedStatus(jokeId).test {
            assertEquals(true, awaitItem())
            awaitComplete()
        }
        verify(exactly = 1) { dao.observeFavoriteJoke(jokeId) }
    }

    @Test
    fun `upsertJoke maps domain joke to entity and delegates to DAO`() = runTest {
        val joke = TestJokes.joke1Favourite
        coEvery { dao.upsertJoke(any()) } just Runs

        repository.upsertJoke(joke)

        val expectedEntity = JokeEntity(
            id = joke.id,
            setup = joke.setup,
            punchline = joke.punchline,
            type = joke.type,
            isFavourite = joke.isFavourite
        )
        coVerify(exactly = 1) { dao.upsertJoke(expectedEntity) }
    }

    @Test
    fun `deleteAllJokes delegates to DAO`() = runTest {
        coEvery { dao.deleteAllJokes() } just Runs

        repository.deleteAllJokes()

        coVerify(exactly = 1) { dao.deleteAllJokes() }
    }

    private fun httpException(code: Int): HttpException =
        HttpException(
            Response.error<Any>(
                code,
                "error".toResponseBody("text/plain".toMediaType())
            )
        )
}
