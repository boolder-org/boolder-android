package com.boolder.boolder.data.database

import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.boolder.boolder.data.database.repository.AreaRepository
import com.boolder.boolder.data.database.repository.LineRepository
import com.boolder.boolder.data.database.repository.ProblemRepository
import com.boolder.boolder.data.database.repository.TickRepository
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
    single { LineRepository(get<BoolderAppDatabase>().lineDao()) }
    single { ProblemRepository(get<BoolderAppDatabase>().problemDao()) }

}

val tickDatabaseModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            TickDatabase::class.java,
            "tick.db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    single { TickRepository(get<TickDatabase>().tickDao(), get<BoolderAppDatabase>().problemDao()) }
    single { ProblemRepository(get<BoolderAppDatabase>().problemDao()) }
}