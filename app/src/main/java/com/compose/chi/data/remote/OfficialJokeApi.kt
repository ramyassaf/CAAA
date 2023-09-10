package com.compose.chi.data.remote

import com.compose.chi.data.remote.dto.JokeDto
import retrofit2.http.GET

interface JokeApi {

    @GET("/random_joke")
    suspend fun getJoke() : JokeDto

}