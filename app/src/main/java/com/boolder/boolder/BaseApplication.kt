package com.boolder.boolder

import android.app.Application
import android.util.Log
import androidx.work.Configuration
import androidx.work.WorkManager
import com.boolder.boolder.data.databaseModule
import com.boolder.boolder.data.network.networkModule
import com.boolder.boolder.domain.domainModule
import com.boolder.boolder.view.viewModelModule
import com.boolder.boolder.offline.worker.BoolderWorkerFactory
import org.koin.android.ext.android.getKoin
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
            modules(
                listOf(
                    appModule,
                    databaseModule,
                    networkModule,
                    domainModule,
                    viewModelModule
                )
            )
        }

        val workManagerConfiguration = Configuration.Builder()
            .setMinimumLoggingLevel(Log.INFO)
            .setWorkerFactory(getKoin().get<BoolderWorkerFactory>())
            .build()

        WorkManager.initialize(this, workManagerConfiguration)
    }
}
