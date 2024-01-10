package com.compose.chi.data.repository

import com.compose.chi.data.database.JokeDao
import com.compose.chi.data.database.model.toJokeEntity
import com.compose.chi.data.remote.JokeApi
import com.compose.chi.domain.model.Joke
import com.compose.chi.domain.repository.JokeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class JokeRepositoryImpl(
    private val api: JokeApi,
    private val jokeDao: JokeDao
): JokeRepository {

    // Remote
    override suspend fun getJoke(): Joke =
        api.getJoke().toJoke()

    override suspend fun getTenJokes(): List<Joke> =
        api.getTenJokes().map { it.toJoke() }

    override suspend fun getJokeById(jokeId: String): Joke =
        api.getJokeById(jokeId).toJoke()


    // Local (db)
    override fun getLikedJokes(): Flow<List<Joke>> =
        jokeDao.getAllLikedJokes().map { list -> list.map {joke ->  joke.toJoke() } }


    override fun isJokeLiked(jokeId: Int): Flow<Boolean> =
        jokeDao.isFavoriteJoke(jokeId = jokeId)


    override suspend fun upsertJoke(joke: Joke) =
        jokeDao.upsertJoke(joke.toJokeEntity())


    override suspend fun deleteAllJokes() =
        jokeDao.deleteAllJokes()

}