package com.compose.chi.domain.use_case

import com.compose.chi.domain.model.Joke
import com.compose.chi.domain.repository.JokeRepository
import com.compose.chi.domain.result.Resource

class GetJokeByIdUseCase(
    private val repository: JokeRepository
) {
    suspend operator fun invoke(jokeId: String): Resource<Joke> = repository.getJokeById(jokeId)
}
