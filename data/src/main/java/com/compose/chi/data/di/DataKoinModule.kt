package com.compose.chi.data.di

import androidx.room.Room
import com.compose.chi.data.database.AppDatabase
import com.compose.chi.data.remote.JokeApi
import com.compose.chi.data.repository.JokeRepositoryImpl
import com.compose.chi.domain.repository.JokeRepository
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private const val BASE_URL_JOKES = "https://official-joke-api.appspot.com"

val dataKoinModule = module {
    single {
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    single {
        OkHttpClient.Builder()
            .addInterceptor(get<HttpLoggingInterceptor>())
            .build()
    }

    single {
        Retrofit.Builder()
            .baseUrl(BASE_URL_JOKES)
            .addConverterFactory(GsonConverterFactory.create())
            .client(get())
            .build()
    }

    single<JokeApi> {
        get<Retrofit>().create(JokeApi::class.java)
    }

    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        ).build()
    }

    single { get<AppDatabase>().dao }

    single<JokeRepository> {
        JokeRepositoryImpl(
            api = get(),
            jokeDao = get()
        )
    }
}
