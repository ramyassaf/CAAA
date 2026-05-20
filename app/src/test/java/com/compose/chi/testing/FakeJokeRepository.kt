package com.compose.chi.testing

import com.compose.chi.domain.model.Joke
import com.compose.chi.domain.repository.JokeRepository
import com.compose.chi.domain.result.DomainError
import com.compose.chi.domain.result.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
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

    var likedJokesError: DomainError? = null
    var likedStatusError: DomainError? = null
    var upsertResource: Resource<Unit> = Resource.Success(Unit)
    var deleteAllResource: Resource<Unit> = Resource.Success(Unit)

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

    override fun observeLikedJokes(): Flow<Resource<List<Joke>>> =
        likedJokesError?.let { error ->
            flowOf(Resource.Error(error))
        } ?: likedJokes.asStateFlow().map { Resource.Success(it) }

    override fun observeJokeLikedStatus(jokeId: Int): Flow<Resource<Boolean>> =
        likedStatusError?.let { error ->
            flowOf(Resource.Error(error))
        } ?: likedStatuses.asStateFlow().map { Resource.Success(it[jokeId] ?: false) }

    override suspend fun upsertJoke(joke: Joke): Resource<Unit> {
        return when (val result = upsertResource) {
            is Resource.Success -> {
                upsertedJokes.add(joke)
                result
            }
            is Resource.Error -> result
        }
    }

    override suspend fun deleteAllJokes(): Resource<Unit> {
        deleteAllCallCount += 1
        return when (val result = deleteAllResource) {
            is Resource.Success -> {
                likedJokes.value = emptyList()
                result
            }
            is Resource.Error -> result
        }
    }
}
