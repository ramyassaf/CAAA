package com.compose.chi.domain.use_case

import com.compose.chi.domain.repository.JokeRepository
import kotlinx.coroutines.flow.Flow

class ObserveJokeLikedStatusUseCase(
    private val repository: JokeRepository
) {

    operator fun invoke(jokeId: Int): Flow<Boolean> = repository.observeJokeLikedStatus(jokeId = jokeId)
}