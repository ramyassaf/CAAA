package com.compose.chi.di

import android.content.Context
import androidx.room.Room
import com.compose.chi.common.Constants.BASE_URL_JOKES
import com.compose.chi.data.db.AppDatabase
import com.compose.chi.data.remote.JokeApi
import com.compose.chi.data.repository.JokeRepositoryImpl
import com.compose.chi.domain.repository.JokeRepository
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// ** Manual Dependency injection
interface AppModule {
    val jokeApi: JokeApi
    val jokeRepository: JokeRepository
    val db: AppDatabase
}

class AppModuleImpl(
    private val appContext: Context
): AppModule {

    override val jokeApi: JokeApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL_JOKES)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(JokeApi::class.java)
    }

    override val jokeRepository: JokeRepository by lazy {
        JokeRepositoryImpl(jokeApi)
    }

    override val db: AppDatabase by lazy {
        Room.databaseBuilder(
            appContext,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        ).build()
    }
}