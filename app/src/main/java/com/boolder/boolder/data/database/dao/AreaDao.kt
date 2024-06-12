package com.boolder.boolder.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.boolder.boolder.data.database.entity.AccessFromPoi
import com.boolder.boolder.data.database.entity.AreaEntity
import com.boolder.boolder.domain.model.AreasEntityWithBeginnerCircuitsCount
import com.boolder.boolder.domain.model.TrainStationPoiWithBikeRoutes

@Dao
interface AreaDao {

    @Query("SELECT * FROM areas WHERE name_searchable LIKE :name ORDER BY priority ASC LIMIT 10")
    suspend fun areasByName(name: String): List<AreaEntity>

    @Query("SELECT * FROM areas WHERE id = :id")
    suspend fun getAreaById(id: Int): AreaEntity?

    @Query("SELECT * FROM areas ORDER BY name COLLATE UNICODE")
    suspend fun getAllAreas(): List<AreaEntity>

    @Query("SELECT * FROM areas ORDER BY problems_count DESC")
    suspend fun getAllAreasByProblemsCount(): List<AreaEntity>

    @Query("""
        SELECT 
            *,
            (
                SELECT COUNT(*) FROM circuits 
                WHERE id IN (
                    SELECT circuit_id FROM problems
                    WHERE area_id = parentArea.id AND circuit_id IS NOT NULL AND beginner_friendly = 1
                    GROUP BY circuit_id
                    HAVING COUNT(*) >= 10
                )
            ) AS 'beginnerCircuitsCount'
        FROM areas parentArea
        WHERE beginnerCircuitsCount > 0
        ORDER BY beginnerCircuitsCount DESC, problems_count DESC
    """)
    suspend fun getAllBeginnerFriendlyAreas(): List<AreasEntityWithBeginnerCircuitsCount>

    @Query("""
        SELECT * FROM areas
        WHERE tags LIKE '%' || :tag || '%'
        ORDER BY problems_count DESC
    """)
    suspend fun getTaggedAreas(tag: String): List<AreaEntity>

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
        SELECT 
            pois.name AS 'trainStationName',
            pois.google_url AS 'googleUrl',
            areas.id AS 'areaId',
            areas.name AS 'areaName',
            poi_routes.distance_in_minutes AS 'bikingTime'
        FROM pois 
            JOIN poi_routes ON pois.id = poi_routes.poi_id 
            JOIN areas ON poi_routes.area_id = areas.id
        WHERE pois.poi_type == 'train_station' AND poi_routes.transport == 'bike'
        ORDER BY pois.id ASC, poi_routes.distance_in_minutes ASC
    """)
    suspend fun getTrainStationPoiWithBikeRoutes(): List<TrainStationPoiWithBikeRoutes>

    @Query("""
        SELECT DISTINCT lines.topo_id 
        FROM areas
            JOIN problems ON problems.area_id = areas.id
            JOIN lines ON lines.problem_id = problems.id
        WHERE areas.id = :areaId
        ORDER BY lines.topo_id ASC
    """)
    suspend fun getAllTopoIdsForArea(areaId: Int): List<Int>

    @Query("""
        SELECT id FROM areas 
        WHERE north_east_lat > :latSouth AND :latNorth > south_west_lat
            AND north_east_lon > :lonWest AND :lonEast > south_west_lon
        ORDER BY priority ASC, name ASC
        LIMIT 5
    """)
    suspend fun getIntersectingAreaIds(
        latNorth: Float,
        latSouth: Float,
        lonEast: Float,
        lonWest: Float
    ): List<Int>
}
