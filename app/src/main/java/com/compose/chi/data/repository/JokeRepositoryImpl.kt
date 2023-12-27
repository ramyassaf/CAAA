package com.compose.chi.data.repository

import com.compose.chi.data.remote.JokeApi
import com.compose.chi.domain.model.Joke
import com.compose.chi.domain.repository.JokeRepository

class JokeRepositoryImpl(
    private val api: JokeApi
): JokeRepository {
    override suspend fun getJoke(): Joke {
        return api.getJoke().toJoke()
    }

    override suspend fun getTenJokes(): List<Joke> {
        var jokesList = mutableListOf<Joke>()
        api.getTenJokes().forEach{person -> jokesList.add(person.toJoke())}
        return jokesList
    }

    override suspend fun getJokeById(jokeId: String): Joke {
        return api.getJokeById(jokeId).toJoke()
    }
}