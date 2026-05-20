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
import kotlinx.coroutines.flow.catch
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
    override fun observeLikedJokes(): Flow<Resource<List<Joke>>> =
        jokeDao.observeAllLikedJokes()
            .localResourceFlow { list -> list.map { joke -> joke.toJoke() } }


    override fun observeJokeLikedStatus(jokeId: Int): Flow<Resource<Boolean>> =
        jokeDao.observeFavoriteJoke(jokeId = jokeId)
            .localResourceFlow { isLiked -> isLiked }


    override suspend fun upsertJoke(joke: Joke): Resource<Unit> =
        localResource { jokeDao.upsertJoke(joke.toJokeEntity()) }


    override suspend fun deleteAllJokes(): Resource<Unit> =
        localResource { jokeDao.deleteAllJokes() }

}

private suspend fun <T> remoteResource(
    block: suspend () -> T
): Resource<T> = try {
    Resource.Success(block())
} catch (exception: Exception) {
    if (exception is CancellationException) throw exception
    Resource.Error(exception.toDomainError())
}

private suspend fun localResource(
    block: suspend () -> Unit
): Resource<Unit> = try {
    block()
    Resource.Success(Unit)
} catch (exception: Exception) {
    if (exception is CancellationException) throw exception
    Resource.Error(DomainError.Persistence)
}

private fun <T, R> Flow<T>.localResourceFlow(
    mapper: (T) -> R
): Flow<Resource<R>> =
    map<T, Resource<R>> { value -> Resource.Success(mapper(value)) }
        .catch { exception ->
            if (exception is CancellationException) throw exception
            emit(Resource.Error(DomainError.Persistence))
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
