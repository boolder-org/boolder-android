package com.boolder.boolder.database

import androidx.room.Room
import com.boolder.boolder.database.repository.AreaRepository
import com.boolder.boolder.database.repository.LineRepository
import com.boolder.boolder.database.repository.ProblemRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {
    single {
        Room.databaseBuilder(androidContext(), BoolderAppDatabase::class.java, "boolder.db")
            .createFromAsset("databases/boolder.db")
            .build()
    }

    single { AreaRepository(get<BoolderAppDatabase>().areaDao()) }
    single { LineRepository(get<BoolderAppDatabase>().lineDao()) }
    single { ProblemRepository(get<BoolderAppDatabase>().problemDao()) }

}
