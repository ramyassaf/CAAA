package com.compose.chi.data.db.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.compose.chi.data.db.JokeDao
import com.compose.chi.data.db.model.JokeEntity

@Database(
    entities = [JokeEntity::class],
    version = 1
)
abstract class AppDatabase: RoomDatabase() {

    abstract fun jokesDao(): JokeDao

    companion object {
        val DATABASE_NAME: String = "jokes_db"
    }
}