package com.muratcay.domain.repository

import com.muratcay.domain.model.LocationPoint
import com.muratcay.domain.utils.Result
import kotlinx.coroutines.flow.Flow

interface LocationRepository {
    fun startLocationUpdates(): Flow<LocationPoint>
    suspend fun stopLocationUpdates()
    suspend fun saveLocationPoint(locationPoint: LocationPoint)
    fun getLocationPoints(): Flow<List<LocationPoint>>
    suspend fun clearLocationPoints()
    suspend fun getAddressFromLocation(latitude: Double, longitude: Double): Result<String>
} 