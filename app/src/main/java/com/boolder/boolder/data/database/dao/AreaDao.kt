package com.boolder.boolder.data.database.dao

import androidx.room.Dao
import androidx.room.MapColumn
import androidx.room.Query
import com.boolder.boolder.data.database.entity.AccessFromPoi
import com.boolder.boolder.data.database.entity.AreasEntity

@Dao
interface AreaDao {

    @Query("SELECT * FROM areas WHERE name_searchable LIKE :name ORDER BY priority ASC LIMIT 10")
    suspend fun areasByName(name: String): List<AreasEntity>

    @Query("SELECT * FROM areas WHERE id = :id")
    suspend fun getAreaById(id: Int): AreasEntity

    @Query("SELECT * FROM areas ORDER BY name_searchable ASC")
    suspend fun getAllAreas(): List<AreasEntity>

    @Query("""
        SELECT 
            poi_routes.distance_in_minutes AS "distanceInMinutes",
            poi_routes.transport,
            pois.poi_type AS "poiType", 
            pois.short_name AS "shortName",
            pois.google_url AS "googleUrl"
        FROM poi_routes INNER JOIN pois ON poi_routes.poi_id = pois.id
        WHERE area_id = :areaId
    """)
    suspend fun getAccessesFromPoiByAreaId(areaId: Int): List<AccessFromPoi>

    @Query("""
        SELECT substr(grade, 1, 1) as degree, count(id) as count
        FROM problems
        WHERE area_id = :areaId
        GROUP BY degree
    """)
    suspend fun getDegreeCountsByArea(areaId: Int):
        Map<@MapColumn("degree") String, @MapColumn("count") Int>
}
