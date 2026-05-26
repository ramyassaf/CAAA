package com.compose.chi.domain.use_case

import com.compose.chi.domain.repository.JokeRepository
import com.compose.chi.domain.result.Resource
import kotlinx.coroutines.flow.Flow

class ObserveJokeLikedStatusUseCase(
    private val repository: JokeRepository
) {

    operator fun invoke(jokeId: Int): Flow<Resource<Boolean>> =
        repository.observeJokeLikedStatus(jokeId = jokeId)
}
