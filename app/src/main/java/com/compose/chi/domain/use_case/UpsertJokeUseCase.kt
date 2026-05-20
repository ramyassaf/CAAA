package com.compose.chi.domain.use_case

import com.compose.chi.domain.model.Joke
import com.compose.chi.domain.repository.JokeRepository
import com.compose.chi.domain.result.Resource

class UpsertJokeUseCase(
    private val repository: JokeRepository
) {

    suspend operator fun invoke(joke: Joke): Resource<Unit> = repository.upsertJoke(joke)
}
