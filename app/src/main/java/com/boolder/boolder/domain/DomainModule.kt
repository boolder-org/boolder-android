package com.boolder.boolder.domain

import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val domainModule = module {
    factoryOf(::TopoDataAggregator)
    factoryOf(::CircuitProblemsRetriever)
}
