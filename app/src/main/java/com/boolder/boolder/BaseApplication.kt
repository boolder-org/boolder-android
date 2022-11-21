package com.boolder.boolder

import android.app.Application
import com.boolder.boolder.data.database.databaseModule
import com.boolder.boolder.data.network.networkModule
import com.boolder.boolder.view.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level.INFO

class BaseApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger(INFO)
            androidContext(this@BaseApplication)
            modules(listOf(databaseModule, networkModule, viewModelModule))
        }
    }
}