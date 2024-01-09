package com.boolder.boolder

import androidx.work.WorkManager
import com.boolder.boolder.offline.BoolderOfflineRepository
import com.boolder.boolder.offline.worker.BoolderWorkerFactory
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val appModule = module {
    factoryOf(::BoolderWorkerFactory)
    factoryOf(::BoolderOfflineRepository)
    factory { WorkManager.getInstance(androidApplication()) }
    factory { androidApplication().resources }
}
