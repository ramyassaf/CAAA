package com.compose.chi.data.remote

import com.compose.chi.data.remote.dto.JokeDto
import retrofit2.http.GET
import retrofit2.http.Path

interface JokeApi {

    @GET("/random_joke")
    suspend fun getJoke() : JokeDto

    @GET("/random_ten")
    suspend fun getTenJokes(): List<JokeDto>

    @GET("/jokes/{jokeId}")
    suspend fun getJokeById(@Path("jokeId") jokeId: Int): JokeDto
}