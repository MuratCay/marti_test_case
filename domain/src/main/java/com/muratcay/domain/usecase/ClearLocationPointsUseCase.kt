package com.muratcay.domain.usecase

import com.muratcay.domain.repository.LocationRepository
import com.muratcay.domain.utils.Result
import com.muratcay.domain.utils.flowCall
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ClearLocationPointsUseCase @Inject constructor(
    private val locationRepository: LocationRepository
) {
    operator fun invoke(): Flow<Result<Unit>> = flowCall {
        locationRepository.clearLocationPoints()
    }
} 