package com.compose.chi.domain.repository

import com.compose.chi.data.remote.dto.JokeDto

// **
interface JokeRepository {

    suspend fun getJoke(): JokeDto

    suspend fun getTenJokes(): List<JokeDto>

    suspend fun getJokeById(jokeId: Int): JokeDto
}