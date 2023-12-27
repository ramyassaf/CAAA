package com.compose.chi.domain.use_case

import com.compose.chi.common.Resource
import com.compose.chi.domain.model.Joke
import com.compose.chi.domain.repository.JokeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException

class GetTenJokesUseCase(
    private val repository: JokeRepository
) {
    operator fun invoke(): Flow<Resource<List<Joke>>> = flow {
        try {
            emit(Resource.Loading<List<Joke>>())
            val joke = repository.getTenJokes().map { it }
            emit(Resource.Success<List<Joke>>(joke))
        } catch(e: HttpException) {
            emit(Resource.Error<List<Joke>>(e.localizedMessage ?: "An unexpected error occurred"))
        } catch(e: IOException) {
            emit(Resource.Error<List<Joke>>("Couldn't reach server. Check your internet connection."))
        }
    }
}