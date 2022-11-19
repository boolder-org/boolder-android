package com.boolder.boolder.data.network

import com.boolder.boolder.data.network.repository.TopoRepository
import org.koin.dsl.module

val networkModule = module {
    single { KtorClient() }
    single { TopoRepository(get()) }
}