package com.boolder.boolder.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.boolder.boolder.database.dao.AreaDao
import com.boolder.boolder.database.dao.LineDao
import com.boolder.boolder.database.dao.ProblemDao
import com.boolder.boolder.database.entity.Areas
import com.boolder.boolder.database.entity.Line
import com.boolder.boolder.database.entity.Problem

@Database(
    entities = [
        Areas::class,
        Line::class,
        Problem::class
    ],
    version = 1,
    exportSchema = true
)
abstract class BoolderAppDatabase : RoomDatabase() {
    abstract fun areaDao(): AreaDao
    abstract fun lineDao(): LineDao
    abstract fun problemDao(): ProblemDao
}


