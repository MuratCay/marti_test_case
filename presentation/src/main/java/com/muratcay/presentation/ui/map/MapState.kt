package com.muratcay.presentation.ui.map

import com.muratcay.domain.model.LocationPoint
import com.muratcay.presentation.base.IState

sealed interface MapState : IState {
    data object Loading : MapState
    data object Idle : MapState
    data class Tracking(
        val isTracking: Boolean,
        val currentLocation: LocationPoint? = null,
        val locationPoints: List<LocationPoint> = emptyList()
    ) : MapState
    data class Error(val message: String) : MapState
    data class AddressLoaded(
        val latitude: Double,
        val longitude: Double,
        val address: String
    ) : MapState
}