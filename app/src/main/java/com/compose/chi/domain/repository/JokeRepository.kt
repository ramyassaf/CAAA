package com.compose.chi.domain.repository

import com.compose.chi.domain.model.Joke
import kotlinx.coroutines.flow.Flow

// **
interface JokeRepository {

    // Remote
    suspend fun getJoke(): Joke

    suspend fun getTenJokes(): List<Joke>

    suspend fun getJokeById(jokeId: String): Joke

    // Local (db)
    fun observeLikedJokes(): Flow<List<Joke>>

    fun observeJokeLikedStatus(jokeId: Int): Flow<Boolean>

    suspend fun upsertJoke(joke: Joke)

    suspend fun deleteAllJokes()
}