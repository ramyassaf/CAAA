package com.compose.chi.domain.repository

import com.compose.chi.domain.model.Joke

// **
interface JokeRepository {

    suspend fun getJoke(): Joke

    suspend fun getTenJokes(): List<Joke>
}