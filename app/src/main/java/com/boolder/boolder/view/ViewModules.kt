package com.boolder.boolder.view

import android.content.res.Resources
import com.boolder.boolder.utils.CubicCurveAlgorithm
import com.boolder.boolder.utils.MapboxStyleFactory
import com.boolder.boolder.utils.NetworkObserverImpl
import com.boolder.boolder.view.map.MapViewModel
import com.boolder.boolder.view.map.filter.GradesFilterViewModel
import com.boolder.boolder.view.search.SearchViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val viewModelModule = module {
    single<Resources> { androidApplication().resources }

    viewModelOf(::MapViewModel)
    single { SearchViewModel(get(), get()) }
    single { NetworkObserverImpl() }
    factory { MapboxStyleFactory() }
    factory { CubicCurveAlgorithm() }

    viewModelOf(::GradesFilterViewModel)
}
