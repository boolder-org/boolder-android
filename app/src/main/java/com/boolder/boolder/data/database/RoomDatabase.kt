package com.boolder.boolder.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.boolder.boolder.data.database.dao.AreaDao
import com.boolder.boolder.data.database.dao.CircuitDao
import com.boolder.boolder.data.database.dao.LineDao
import com.boolder.boolder.data.database.dao.ProblemDao
import com.boolder.boolder.data.database.entity.AreasEntity
import com.boolder.boolder.data.database.entity.CircuitEntity
import com.boolder.boolder.data.database.entity.LineEntity
import com.boolder.boolder.data.database.entity.ProblemEntity

@Database(
    entities = [
        AreasEntity::class,
        CircuitEntity::class,
        LineEntity::class,
        ProblemEntity::class
    ],
    version = 10, // increment version number everytime the boolder.db database changes (schema or data)
    exportSchema = true
)
abstract class BoolderAppDatabase : RoomDatabase() {
    abstract fun areaDao(): AreaDao
    abstract fun circuitDao(): CircuitDao
    abstract fun lineDao(): LineDao
    abstract fun problemDao(): ProblemDao
}


