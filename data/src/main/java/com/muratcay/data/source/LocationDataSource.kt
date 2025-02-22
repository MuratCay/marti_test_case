package com.muratcay.data.source

import com.muratcay.domain.model.LocationPoint
import com.muratcay.domain.utils.Result
import kotlinx.coroutines.flow.Flow

interface LocationDataSource {
    fun startLocationUpdates(): Flow<LocationPoint>
    suspend fun stopLocationUpdates()
    suspend fun getAddressFromLocation(latitude: Double, longitude: Double): Result<String>
}