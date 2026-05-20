package com.compose.chi.domain.use_case

import com.compose.chi.domain.model.Joke
import com.compose.chi.domain.repository.JokeRepository
import com.compose.chi.domain.result.Resource
import kotlinx.coroutines.flow.Flow

class ObserveLikedJokesUseCase(
    private val repository: JokeRepository
) {
    operator fun invoke(): Flow<Resource<List<Joke>>> = repository.observeLikedJokes()
}
