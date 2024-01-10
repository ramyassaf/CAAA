package com.compose.chi.domain.use_case

import com.compose.chi.domain.repository.JokeRepository

class DeleteAllJokesUseCase(
    private val repository: JokeRepository
) {
    suspend operator fun invoke() = repository.deleteAllJokes()
}