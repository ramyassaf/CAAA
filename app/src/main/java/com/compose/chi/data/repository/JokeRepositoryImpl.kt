package com.compose.chi.data.repository

import com.compose.chi.data.database.JokeDao
import com.compose.chi.data.remote.JokeApi
import com.compose.chi.domain.model.Joke
import com.compose.chi.domain.repository.JokeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class JokeRepositoryImpl(
    private val api: JokeApi,
    private val jokeDao: JokeDao
): JokeRepository {
    override suspend fun getJoke(): Joke {
        return api.getJoke().toJoke()
    }

    override suspend fun getTenJokes(): List<Joke> {
        val jokesList = mutableListOf<Joke>()
        api.getTenJokes().forEach{joke -> jokesList.add(joke.toJoke())}
        return jokesList
    }

    override suspend fun getJokeById(jokeId: String): Joke {
        return api.getJokeById(jokeId).toJoke()
    }

    override fun getLikedJokes(): Flow<List<Joke>> = flow {
        jokeDao.getAllLikedJokes().collect {
            val mappedList = it.map { jokeEntity -> jokeEntity.toJoke() }
            emit(mappedList)
        }
    }

    override fun isJokeLiked(jokeId: Int): Flow<Boolean> = flow {
        jokeDao.isFavoriteJoke(jokeId = jokeId).collect {
            emit(it)
        }
    }
}