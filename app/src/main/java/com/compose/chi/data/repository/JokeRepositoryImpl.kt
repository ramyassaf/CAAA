package com.compose.chi.data.repository

import com.compose.chi.data.remote.JokeApi
import com.compose.chi.data.remote.dto.JokeDto
import com.compose.chi.domain.repository.JokeRepository

class JokeRepositoryImpl(
    private val api: JokeApi
): JokeRepository {
    override suspend fun getJoke(): JokeDto {
        return api.getJoke()
    }

    override suspend fun getTenJokes(): List<JokeDto> {
        return api.getTenJokes()
    }

    override suspend fun getJokeById(jokeId: Int): JokeDto {
        return api.getJokeById(jokeId)
    }
}