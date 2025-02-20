package com.muratcay.domain.usecase

import com.muratcay.domain.model.LocationPoint
import com.muratcay.domain.repository.LocationRepository
import com.muratcay.domain.utils.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetLocationPointsUseCase @Inject constructor(
    private val locationRepository: LocationRepository
) {
    operator fun invoke(): Flow<Result<List<LocationPoint>>> {
        return locationRepository.getLocationPoints().map { locationPoints ->
                Result.Success(locationPoints)
            }
    }
} 