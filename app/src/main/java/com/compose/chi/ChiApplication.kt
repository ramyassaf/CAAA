package com.compose.chi

import android.app.Application
import com.compose.chi.di.AppModule
import com.compose.chi.di.AppModuleImpl
import com.compose.chi.di.appKoinModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

// ** Manual Dependency injection
class ChiApplication : Application() {

    // companion 'static' object that can be accessed across the app (active as long as app is active)
    companion object {
        lateinit var appModule: AppModule
    }

    override fun onCreate() {
        super.onCreate()

        appModule = AppModuleImpl(this)

        startKoin {
            androidLogger()
            androidContext(this@ChiApplication)
            modules(appKoinModule)
        }
    }

}
