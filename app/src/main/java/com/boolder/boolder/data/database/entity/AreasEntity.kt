package com.boolder.boolder.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "areas",
    indices = [Index(
        name = "area_idx",
        value = ["id"],
        unique = false,
        orders = [Index.Order.ASC]
    )]
)
data class AreasEntity(
    @PrimaryKey
    val id: Int,
    val name: String,
    @ColumnInfo(name = "name_searchable")
    val nameSearchable: String,
    val priority: Int,
    @ColumnInfo(name = "description_fr")
    val descriptionFr: String?,
    @ColumnInfo(name = "description_en")
    val descriptionEn: String?,
    @ColumnInfo(name = "warning_fr")
    val warningFr: String?,
    @ColumnInfo(name = "warning_en")
    val warningEn: String?,
    val tags: String?,
    @ColumnInfo(name = "south_west_lat")
    val southWestLat: Float,
    @ColumnInfo(name = "south_west_lon")
    val southWestLon: Float,
    @ColumnInfo(name = "north_east_lat")
    val northEastLat: Float,
    @ColumnInfo(name = "north_east_lon")
    val northEastLon: Float,
    @ColumnInfo(name = "level1_count")
    val level1Count: Int,
    @ColumnInfo(name = "level2_count")
    val level2Count: Int,
    @ColumnInfo(name = "level3_count")
    val level3Count: Int,
    @ColumnInfo(name = "level4_count")
    val level4Count: Int,
    @ColumnInfo(name = "level5_count")
    val level5Count: Int,
    @ColumnInfo(name = "level6_count")
    val level6Count: Int,
    @ColumnInfo(name = "level7_count")
    val level7Count: Int,
    @ColumnInfo(name = "level8_count")
    val level8Count: Int,
    @ColumnInfo(name = "problems_count")
    val problemsCount: Int,
)

