package com.compose.chi.data.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.compose.chi.domain.model.Joke

@Entity(tableName = "jokes")
data class JokeEntity(
    val punchline: String,
    val setup: String,
    val type: String,
    var isFavourite: Boolean = false,
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

fun Joke.toJokeEntity(): JokeEntity {
    return JokeEntity (
        punchline = punchline,
        setup = setup,
        type = type,
        isFavourite = isFavourite,
        id = id
    )
}