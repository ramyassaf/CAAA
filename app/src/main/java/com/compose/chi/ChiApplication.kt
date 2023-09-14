package com.compose.chi

import android.app.Application
import com.compose.chi.di.AppModule
import com.compose.chi.di.AppModuleImpl

// ** Manual Dependency injection
class ChiApplication: Application() {

    // companion 'static' object that can be accessed across the app (active as long as app is active)
    companion object {
        lateinit var appModule: AppModule
    }

    override fun onCreate() {
        super.onCreate()
        appModule = AppModuleImpl(this)
    }

}