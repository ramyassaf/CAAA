package com.compose.chi.domain.use_case

import com.compose.chi.common.Resource
import com.compose.chi.data.remote.dto.toJoke
import com.compose.chi.domain.model.Joke
import com.compose.chi.domain.repository.JokeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException

class GetJokeUseCase(
    private val repository: JokeRepository
) {
    operator fun invoke(): Flow<Resource<Joke>> = flow {
        try {
            emit(Resource.Loading<Joke>())
            val joke = repository.getJoke().toJoke()
            emit(Resource.Success<Joke>(joke))
        } catch(e: HttpException) {
            emit(Resource.Error<Joke>(e.localizedMessage ?: "An unexpected error occurred"))
        } catch(e: IOException) {
            emit(Resource.Error<Joke>("Couldn't reach server. Check your internet connection."))
        }
    }
}