package com.compose.chi.domain.use_case

import com.compose.chi.domain.model.Joke
import com.compose.chi.domain.repository.JokeRepository

class UpsertJokeUseCase(
    private val repository: JokeRepository
) {

    suspend operator fun invoke(joke: Joke) = repository.upsertJoke(joke)
}