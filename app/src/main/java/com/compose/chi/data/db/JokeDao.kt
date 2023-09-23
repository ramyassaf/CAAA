package com.compose.chi.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.compose.chi.data.db.model.JokeEntity
import com.compose.chi.domain.model.Joke
import kotlinx.coroutines.flow.Flow

@Dao
interface JokeDao {

    @Upsert()
    suspend fun addJoke(joke: Joke)

    @Delete
    suspend fun deleteJoke(joke: Joke)

    @Query("SELECT * from jokes")
    suspend fun getAllJokes(): Flow<List<JokeEntity>>
}