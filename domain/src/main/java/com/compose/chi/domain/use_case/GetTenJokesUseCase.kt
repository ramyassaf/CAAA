package com.compose.chi.domain.use_case

import com.compose.chi.domain.model.Joke
import com.compose.chi.domain.repository.JokeRepository
import com.compose.chi.domain.result.Resource

class GetTenJokesUseCase(
    private val repository: JokeRepository
) {
    suspend operator fun invoke(): Resource<List<Joke>> = repository.getTenJokes()
}
