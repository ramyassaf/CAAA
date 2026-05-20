package com.compose.chi.data.repository

import com.compose.chi.data.database.JokeDao
import com.compose.chi.data.database.model.toJokeEntity
import com.compose.chi.data.remote.JokeApi
import com.compose.chi.domain.model.Joke
import com.compose.chi.domain.repository.JokeRepository
import com.compose.chi.domain.result.DomainError
import com.compose.chi.domain.result.Resource
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import retrofit2.HttpException

class JokeRepositoryImpl(
    private val api: JokeApi,
    private val jokeDao: JokeDao
): JokeRepository {

    // Remote
    override suspend fun getJoke(): Resource<Joke> =
        remoteResource { api.getJoke().toJoke() }

    override suspend fun getTenJokes(): Resource<List<Joke>> =
        remoteResource { api.getTenJokes().map { it.toJoke() } }

    override suspend fun getJokeById(jokeId: String): Resource<Joke> =
        remoteResource { api.getJokeById(jokeId).toJoke() }


    // Local (db)
    override fun observeLikedJokes(): Flow<List<Joke>> =
        jokeDao.observeAllLikedJokes().map { list -> list.map { joke ->  joke.toJoke() } }


    override fun observeJokeLikedStatus(jokeId: Int): Flow<Boolean> =
        jokeDao.observeFavoriteJoke(jokeId = jokeId)


    override suspend fun upsertJoke(joke: Joke) =
        jokeDao.upsertJoke(joke.toJokeEntity())


    override suspend fun deleteAllJokes() =
        jokeDao.deleteAllJokes()

}

private suspend fun <T> remoteResource(
    block: suspend () -> T
): Resource<T> = try {
    Resource.Success(block())
} catch (exception: Exception) {
    if (exception is CancellationException) throw exception
    Resource.Error(exception.toDomainError())
}

private fun Throwable.toDomainError(): DomainError = when (this) {
    is IOException -> DomainError.Network
    is HttpException -> when (code()) {
        404 -> DomainError.NotFound
        in 500..599 -> DomainError.Server
        else -> DomainError.Unknown
    }
    else -> DomainError.Unknown
}
