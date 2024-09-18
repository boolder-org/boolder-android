package com.boolder.boolder.view

import android.content.res.Resources
import com.boolder.boolder.offline.FileExplorer
import com.boolder.boolder.utils.FileSizeFormatter
import com.boolder.boolder.utils.MapboxStyleFactory
import com.boolder.boolder.view.areadetails.areacircuit.AreaCircuitViewModel
import com.boolder.boolder.view.areadetails.areaoverview.AreaOverviewViewModel
import com.boolder.boolder.view.areadetails.areaproblems.AreaProblemsViewModel
import com.boolder.boolder.view.discover.discover.DiscoverViewModel
import com.boolder.boolder.view.discover.driesfast.DriesFastViewModel
import com.boolder.boolder.view.discover.levels.LevelsViewModel
import com.boolder.boolder.view.discover.levels.beginner.BeginnerLevelsViewModel
import com.boolder.boolder.view.discover.trainandbike.TrainAndBikeViewModel
import com.boolder.boolder.view.fullscreenphoto.FullScreenPhotoViewModel
import com.boolder.boolder.view.map.MapViewModel
import com.boolder.boolder.view.map.areadownload.AreaDownloadViewModel
import com.boolder.boolder.view.map.filter.grade.GradesFilterViewModel
import com.boolder.boolder.view.offlinephotos.OfflinePhotosViewModel
import com.boolder.boolder.view.offlinephotos.OfflinePhotosViewModelImpl
import com.boolder.boolder.view.search.SearchViewModel
import com.boolder.boolder.view.ticklist.TickListViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.binds
import org.koin.dsl.module

val viewModelModule = module {
    single<Resources> { androidApplication().resources }

    viewModelOf(::MapViewModel)
    viewModelOf(::SearchViewModel)
    factory { MapboxStyleFactory() }

    viewModelOf(::GradesFilterViewModel)

    viewModelOf(::OfflinePhotosViewModelImpl) { binds(listOf(OfflinePhotosViewModel::class)) }
    factory { FileExplorer(androidApplication()) }
    factory { FileSizeFormatter() }

    viewModelOf(::AreaOverviewViewModel)
    viewModelOf(::AreaProblemsViewModel)
    viewModelOf(::AreaCircuitViewModel)

    viewModelOf(::DiscoverViewModel)
    viewModelOf(::DriesFastViewModel)
    viewModelOf(::LevelsViewModel)
    viewModelOf(::BeginnerLevelsViewModel)
    viewModelOf(::TrainAndBikeViewModel)

    viewModelOf(::TickListViewModel)

    viewModelOf(::FullScreenPhotoViewModel)

    viewModelOf(::AreaDownloadViewModel)
}
