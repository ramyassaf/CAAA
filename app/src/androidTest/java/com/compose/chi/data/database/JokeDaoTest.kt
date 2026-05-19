package com.compose.chi.data.database

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.compose.chi.data.database.model.JokeEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class JokeDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: JokeDao

    private val likedJoke = JokeEntity(
        id = 1,
        setup = "Setup 1",
        punchline = "Punchline 1",
        type = "general",
        isFavourite = true
    )

    private val notLikedJoke = JokeEntity(
        id = 2,
        setup = "Setup 2",
        punchline = "Punchline 2",
        type = "general",
        isFavourite = false
    )

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.dao
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun upsertJoke_inserts_and_getAllJokes_emits_it() = runBlocking {
        dao.upsertJoke(likedJoke)

        val stored = dao.getAllJokes().first()
        assertEquals(1, stored.size)
        assertEquals(likedJoke, stored.single())
    }

    @Test
    fun observeAllLikedJokes_returns_only_jokes_where_isFavourite_is_true() = runBlocking {
        dao.upsertJoke(likedJoke)
        dao.upsertJoke(notLikedJoke)

        val liked = dao.observeAllLikedJokes().first()
        assertEquals(listOf(likedJoke), liked)
    }

    @Test
    fun observeFavoriteJoke_emits_true_for_liked_joke() = runBlocking {
        dao.upsertJoke(likedJoke)

        assertTrue(dao.observeFavoriteJoke(likedJoke.id).first())
    }

    @Test
    fun observeFavoriteJoke_emits_false_for_non_liked_or_missing_joke() = runBlocking {
        dao.upsertJoke(notLikedJoke)

        assertFalse("not-liked joke must not register as favourite",
            dao.observeFavoriteJoke(notLikedJoke.id).first())
        assertFalse("missing joke must not register as favourite",
            dao.observeFavoriteJoke(jokeId = 999).first())
    }

    @Test
    fun deleteAllJokes_clears_stored_jokes() = runBlocking {
        dao.upsertJoke(likedJoke)
        dao.upsertJoke(notLikedJoke)

        dao.deleteAllJokes()

        assertTrue(dao.getAllJokes().first().isEmpty())
    }
}
