package com.compose.chi.di

import androidx.room.Room
import com.compose.chi.common.Constants.BASE_URL_JOKES
import com.compose.chi.data.database.AppDatabase
import com.compose.chi.data.remote.JokeApi
import com.compose.chi.data.repository.JokeRepositoryImpl
import com.compose.chi.domain.repository.JokeRepository
import com.compose.chi.domain.use_case.DeleteAllJokesUseCase
import com.compose.chi.domain.use_case.GetJokeByIdUseCase
import com.compose.chi.domain.use_case.GetJokeUseCase
import com.compose.chi.domain.use_case.GetLikedJokesUseCase
import com.compose.chi.domain.use_case.GetTenJokesUseCase
import com.compose.chi.domain.use_case.IsJokeLikedUseCase
import com.compose.chi.domain.use_case.UpsertJokeUseCase
import com.compose.chi.presentation.screens.joke_details_page.JokeDetailsViewModel
import com.compose.chi.presentation.screens.joke_home_page.JokeHomeViewModel
import com.compose.chi.presentation.screens.my_favourite_jokes_page.MyFavouriteJokesViewModel
import com.compose.chi.presentation.screens.ten_jokes_page.TenJokesViewModel
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val appKoinModule = module {
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

    factory { GetJokeUseCase(get()) }
    factory { GetTenJokesUseCase(get()) }
    factory { GetJokeByIdUseCase(get()) }
    factory { GetLikedJokesUseCase(get()) }
    factory { IsJokeLikedUseCase(get()) }
    factory { UpsertJokeUseCase(get()) }
    factory { DeleteAllJokesUseCase(get()) }

    viewModel { JokeHomeViewModel(get(), get(), get()) }
    viewModel { TenJokesViewModel(get()) }
    viewModel { MyFavouriteJokesViewModel(get(), get()) }
    viewModel { JokeDetailsViewModel(get(), get(), get(), get()) }
}
