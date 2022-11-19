package com.boolder.boolder.view

import com.boolder.boolder.view.map.MapViewModel
import org.koin.dsl.module

val viewModelModule = module {
    single { MapViewModel(get(), get(), get(), get()) }
}