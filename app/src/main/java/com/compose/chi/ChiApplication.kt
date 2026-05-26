package com.compose.chi

import android.app.Application
import com.compose.chi.data.di.dataKoinModule
import com.compose.chi.di.appKoinModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class ChiApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@ChiApplication)
            modules(dataKoinModule, appKoinModule)
        }
    }

}
