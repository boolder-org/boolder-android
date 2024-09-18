package com.boolder.boolder.data

import androidx.room.Room
import com.boolder.boolder.data.database.BoolderAppDatabase
import com.boolder.boolder.data.database.repository.AreaRepository
import com.boolder.boolder.data.database.repository.CircuitRepository
import com.boolder.boolder.data.database.repository.ClusterRepository
import com.boolder.boolder.data.database.repository.LineRepository
import com.boolder.boolder.data.database.repository.ProblemRepository
import com.boolder.boolder.data.network.repository.TopoRepository
import com.boolder.boolder.data.userdatabase.UserDatabase
import com.boolder.boolder.data.userdatabase.repository.TickedProblemRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {
    single {
        Room.databaseBuilder(androidContext(), BoolderAppDatabase::class.java, "boolder.db")
            .createFromAsset("databases/boolder.db")
            .fallbackToDestructiveMigration()
            .build()
    }

    single { AreaRepository(get<BoolderAppDatabase>().areaDao()) }
    single { ClusterRepository(get<BoolderAppDatabase>().clusterDao()) }
    single { CircuitRepository(get<BoolderAppDatabase>().circuitDao()) }
    single { LineRepository(get<BoolderAppDatabase>().lineDao()) }
    single { ProblemRepository(get<BoolderAppDatabase>().problemDao()) }

    single {
        Room.databaseBuilder(androidContext(), UserDatabase::class.java, "boolder_user.db")
            .build()
    }

    single { TickedProblemRepository(get<UserDatabase>().tickedProblemDao()) }

    single { TopoRepository(get()) }
}
