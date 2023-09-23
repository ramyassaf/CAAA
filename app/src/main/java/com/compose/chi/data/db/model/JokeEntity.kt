package com.compose.chi.data.db.model

import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "jokes")
data class JokeEntity(
    @PrimaryKey(autoGenerate = false)
    val id: Int,
    val punchline: String,
    val setup: String,
    val type: String
)
