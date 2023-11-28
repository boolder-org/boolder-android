package com.boolder.boolder

import android.content.res.Resources
import androidx.work.WorkManager
import com.boolder.boolder.offline.BoolderOfflineRepository
import com.boolder.boolder.offline.worker.BoolderWorkerFactory
import com.mapbox.bindgen.Value
import com.mapbox.common.TileDataDomain
import com.mapbox.common.TileStore
import com.mapbox.common.TileStoreOptions
import com.mapbox.maps.MapInitOptions
import com.mapbox.maps.OfflineManager
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val appModule = module {
    factoryOf(::BoolderWorkerFactory)
    factoryOf(::BoolderOfflineRepository)
    factory { WorkManager.getInstance(androidApplication()) }
    factory { androidApplication().resources }

    factory {
        OfflineManager(MapInitOptions.getDefaultResourceOptions(androidApplication()))
    }

    single {
        TileStore.create().also {
            it.setOption(
                TileStoreOptions.MAPBOX_ACCESS_TOKEN,
                TileDataDomain.MAPS,
                Value(get<Resources>().getString(R.string.mapbox_access_token))
            )
        }
    }
}
