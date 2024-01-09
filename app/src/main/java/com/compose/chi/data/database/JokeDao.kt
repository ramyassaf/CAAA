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
    suspend fun upsertJoke(joke: JokeEntity)

    @Delete
    suspend fun deleteJoke(joke: JokeEntity)

    @Query("SELECT * FROM jokes")
    fun getAllJokes(): Flow<List<JokeEntity>>

    @Query("SELECT * FROM jokes WHERE isFavourite")
    fun getAllLikedJokes(): Flow<List<JokeEntity>>

    @Query("DELETE FROM jokes")
    fun deleteAllJokes()

    @Query("SELECT EXISTS(SELECT 1 FROM jokes WHERE id = :jokeId AND isFavourite LIMIT 1)")
    suspend fun isFavoriteJoke(jokeId: Int): Boolean
}