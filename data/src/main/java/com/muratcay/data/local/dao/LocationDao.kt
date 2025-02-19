package com.muratcay.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.muratcay.data.model.LocationPointEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocationPoint(locationPoint: LocationPointEntity)

    @Query("SELECT * FROM location_points ORDER BY timestamp ASC")
    fun getLocationPoints(): Flow<List<LocationPointEntity>>

    @Query("DELETE FROM location_points")
    suspend fun clearLocationPoints()
} 