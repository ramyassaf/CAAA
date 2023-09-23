package com.compose.chi.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [JokeEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase: RoomDatabase() {

    abstract val dao: JokeDao

    companion object {
        val DATABASE_NAME: String = "jokes_db"
    }
}