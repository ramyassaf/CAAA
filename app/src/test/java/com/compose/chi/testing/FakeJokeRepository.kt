package com.compose.chi.testing

import com.compose.chi.domain.model.Joke
import com.compose.chi.domain.repository.JokeRepository
import com.compose.chi.domain.result.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

/**
 * Hand-written fake of [JokeRepository] for ViewModel and use-case tests.
 *
 * Each remote method holds a configured Resource. Local liked-jokes state is
 * exposed via [likedJokes] so tests can push updates and assert downstream
 * collection behavior.
 */
class FakeJokeRepository : JokeRepository {

    var jokeResource: Resource<Joke> = Resource.Success(TestJokes.joke1)

    var tenJokesResource: Resource<List<Joke>> = Resource.Success(TestJokes.tenJokes())

    var jokeByIdResource: Resource<Joke> = Resource.Success(TestJokes.joke1)
    var lastRequestedJokeId: String? = null

    private val likedJokes = MutableStateFlow<List<Joke>>(emptyList())
    private val likedStatuses = MutableStateFlow<Map<Int, Boolean>>(emptyMap())

    var upsertError: Throwable? = null
    var deleteAllError: Throwable? = null

    val upsertedJokes: MutableList<Joke> = mutableListOf()
    var deleteAllCallCount: Int = 0

    fun setLikedJokes(jokes: List<Joke>) {
        likedJokes.value = jokes
    }

    fun setLikedStatus(jokeId: Int, isLiked: Boolean) {
        likedStatuses.value = likedStatuses.value + (jokeId to isLiked)
    }

    override suspend fun getJoke(): Resource<Joke> = jokeResource

    override suspend fun getTenJokes(): Resource<List<Joke>> = tenJokesResource

    override suspend fun getJokeById(jokeId: String): Resource<Joke> {
        lastRequestedJokeId = jokeId
        return jokeByIdResource
    }

    override fun observeLikedJokes(): Flow<List<Joke>> = likedJokes.asStateFlow()

    override fun observeJokeLikedStatus(jokeId: Int): Flow<Boolean> =
        likedStatuses.asStateFlow().map { it[jokeId] ?: false }

    override suspend fun upsertJoke(joke: Joke) {
        upsertError?.let { throw it }
        upsertedJokes.add(joke)
    }

    override suspend fun deleteAllJokes() {
        deleteAllError?.let { throw it }
        deleteAllCallCount += 1
        likedJokes.value = emptyList()
    }
}
