package com.compose.chi.domain.repository

import com.compose.chi.domain.model.Joke
import com.compose.chi.domain.result.Resource
import kotlinx.coroutines.flow.Flow

// **
interface JokeRepository {

    // Remote
    suspend fun getJoke(): Resource<Joke>

    suspend fun getTenJokes(): Resource<List<Joke>>

    suspend fun getJokeById(jokeId: String): Resource<Joke>

    // Local (db)
    fun observeLikedJokes(): Flow<List<Joke>>

    fun observeJokeLikedStatus(jokeId: Int): Flow<Boolean>

    suspend fun upsertJoke(joke: Joke)

    suspend fun deleteAllJokes()
}
