package com.boolder.boolder.view

import com.boolder.boolder.MainViewModel
import org.koin.dsl.module

val viewModelModule = module {
    single { MainViewModel(get(), get(), get()) }
}