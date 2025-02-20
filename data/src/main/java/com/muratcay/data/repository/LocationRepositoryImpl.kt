package com.muratcay.data.repository

import com.muratcay.domain.model.LocationPoint
import com.muratcay.domain.repository.LocationRepository
import com.muratcay.data.local.dao.LocationDao
import com.muratcay.data.mapper.toDomain
import com.muratcay.data.mapper.toEntity
import com.muratcay.data.source.LocationDataSource
import com.muratcay.domain.utils.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

class LocationRepositoryImpl @Inject constructor(
    private val locationDataSource: LocationDataSource,
    private val locationDao: LocationDao
) : LocationRepository {

    override fun startLocationUpdates(): Flow<LocationPoint> {
        return locationDataSource.startLocationUpdates()
            .onEach { locationPoint ->
                // Save each location point as it comes in
                saveLocationPoint(locationPoint)
            }
    }

    override suspend fun stopLocationUpdates() {
        locationDataSource.stopLocationUpdates()
    }

    override suspend fun saveLocationPoint(locationPoint: LocationPoint) {
        locationDao.insertLocationPoint(locationPoint.toEntity())
    }

    override fun getLocationPoints(): Flow<List<LocationPoint>> {
        return locationDao.getLocationPoints().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun clearLocationPoints() {
        locationDao.clearLocationPoints()
    }

    override suspend fun getAddressFromLocation(
        latitude: Double,
        longitude: Double
    ): Result<String> {
        return locationDataSource.getAddressFromLocation(latitude, longitude)
    }
} 