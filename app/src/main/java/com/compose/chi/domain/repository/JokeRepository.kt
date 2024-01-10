package com.compose.chi.domain.repository

import com.compose.chi.domain.model.Joke
import kotlinx.coroutines.flow.Flow

// **
interface JokeRepository {

    suspend fun getJoke(): Joke

    suspend fun getTenJokes(): List<Joke>

    suspend fun getJokeById(jokeId: String): Joke

    fun getLikedJokes(): Flow<List<Joke>>

    fun isJokeLiked(jokeId: Int): Flow<Boolean>
}