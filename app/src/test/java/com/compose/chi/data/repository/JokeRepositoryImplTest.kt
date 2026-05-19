package com.compose.chi.data.repository

import app.cash.turbine.test
import com.compose.chi.data.database.JokeDao
import com.compose.chi.data.database.model.JokeEntity
import com.compose.chi.data.remote.JokeApi
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
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

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
    fun `getJoke maps API DTO to domain joke`() = runTest {
        coEvery { api.getJoke() } returns TestJokes.jokeDto1

        val result = repository.getJoke()

        assertEquals(TestJokes.jokeDto1.toJoke(), result)
        coVerify(exactly = 1) { api.getJoke() }
    }

    @Test
    fun `getTenJokes maps every DTO from API to domain joke`() = runTest {
        val dtos = TestJokes.tenJokeDtos()
        coEvery { api.getTenJokes() } returns dtos

        val result = repository.getTenJokes()

        assertEquals(dtos.map { it.toJoke() }, result)
        assertEquals(dtos.size, result.size)
        coVerify(exactly = 1) { api.getTenJokes() }
    }

    @Test
    fun `getJokeById passes the id to the API and maps the DTO`() = runTest {
        val jokeId = "42"
        val dto = TestJokes.jokeDto1.copy(id = 42)
        coEvery { api.getJokeById(jokeId) } returns dto

        val result = repository.getJokeById(jokeId)

        assertEquals(dto.toJoke(), result)
        coVerify(exactly = 1) { api.getJokeById(jokeId) }
    }

    @Test
    fun `remote HttpException is not swallowed by getJoke`() = runTest {
        val httpException = HttpException(
            Response.error<Any>(
                500,
                "boom".toResponseBody("text/plain".toMediaType())
            )
        )
        coEvery { api.getJoke() } throws httpException

        val thrown = assertThrows(HttpException::class.java) {
            kotlinx.coroutines.runBlocking { repository.getJoke() }
        }
        assertEquals(500, thrown.code())
    }

    @Test
    fun `remote IOException is not swallowed by getJokeById`() = runTest {
        coEvery { api.getJokeById("1") } throws IOException("offline")

        assertThrows(IOException::class.java) {
            kotlinx.coroutines.runBlocking { repository.getJokeById("1") }
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
}
