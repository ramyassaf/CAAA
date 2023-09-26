package com.compose.chi.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.compose.chi.data.database.model.JokeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface JokeDao {

    @Upsert()
    suspend fun addJoke(joke: JokeEntity)

    @Delete
    suspend fun deleteJoke(joke: JokeEntity)

    @Query("SELECT * from jokes")
    fun getAllJokes(): Flow<List<JokeEntity>>

    @Query("DELETE from jokes")
    fun deleteAllJokes()
}