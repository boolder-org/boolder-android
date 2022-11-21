package com.boolder.boolder.view

import com.boolder.boolder.utils.CubicCurveAlgorithm
import com.boolder.boolder.utils.MapboxStyleFactory
import com.boolder.boolder.utils.NetworkObserverImpl
import com.boolder.boolder.view.map.MapViewModel
import com.boolder.boolder.view.search.SearchViewModel
import org.koin.dsl.module

val viewModelModule = module {
    single { MapViewModel(get(), get(), get(), get()) }
    single { SearchViewModel() }
    single { NetworkObserverImpl() }
    factory { MapboxStyleFactory() }
    factory { CubicCurveAlgorithm() }
}