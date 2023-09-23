package com.compose.chi.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.compose.chi.data.remote.dto.JokeDto
import com.compose.chi.domain.model.Joke

@Entity(tableName = "jokes")
data class JokeEntity(
    val punchline: String,
    val setup: String,
    val type: String,
    @PrimaryKey(autoGenerate = true)
    val id: Int
)

fun JokeEntity.toJoke(): Joke {
    return Joke (
        punchline = punchline,
        setup = setup,
        type = type,
        id = id
    )
}