package com.boolder.boolder.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.boolder.boolder.data.database.dao.AreaDao
import com.boolder.boolder.data.database.dao.CircuitDao
import com.boolder.boolder.data.database.dao.ClusterDao
import com.boolder.boolder.data.database.dao.LineDao
import com.boolder.boolder.data.database.dao.ProblemDao
import com.boolder.boolder.data.database.entity.AreaEntity
import com.boolder.boolder.data.database.entity.CircuitEntity
import com.boolder.boolder.data.database.entity.ClusterEntity
import com.boolder.boolder.data.database.entity.LineEntity
import com.boolder.boolder.data.database.entity.PoiEntity
import com.boolder.boolder.data.database.entity.PoiRouteEntity
import com.boolder.boolder.data.database.entity.ProblemEntity

@Database(
    entities = [
        AreaEntity::class,
        ClusterEntity::class,
        CircuitEntity::class,
        LineEntity::class,
        PoiEntity::class,
        PoiRouteEntity::class,
        ProblemEntity::class
    ],
    version = 25, // increment version number everytime the boolder.db database changes (schema or data)
    exportSchema = true
)
abstract class BoolderAppDatabase : RoomDatabase() {
    abstract fun areaDao(): AreaDao
    abstract fun clusterDao(): ClusterDao
    abstract fun circuitDao(): CircuitDao
    abstract fun lineDao(): LineDao
    abstract fun problemDao(): ProblemDao
}


