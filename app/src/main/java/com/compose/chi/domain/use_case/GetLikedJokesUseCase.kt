package com.compose.chi.domain.use_case

import com.compose.chi.domain.model.Joke
import com.compose.chi.domain.repository.JokeRepository
import kotlinx.coroutines.flow.Flow

class GetLikedJokesUseCase(
    private val repository: JokeRepository
) {
    operator fun invoke(): Flow<List<Joke>> = repository.getLikedJokes()
}