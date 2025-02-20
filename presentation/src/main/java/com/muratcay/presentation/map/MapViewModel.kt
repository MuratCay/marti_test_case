package com.muratcay.presentation.map

import androidx.lifecycle.viewModelScope
import com.muratcay.domain.model.LocationPoint
import com.muratcay.domain.repository.LocationRepository
import com.muratcay.domain.service.LocationServiceController
import com.muratcay.domain.usecase.ClearLocationPointsUseCase
import com.muratcay.domain.usecase.GetLocationPointsUseCase
import com.muratcay.domain.usecase.StartLocationTrackingUseCase
import com.muratcay.domain.usecase.StopLocationTrackingUseCase
import com.muratcay.domain.utils.Result
import com.muratcay.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val locationServiceController: LocationServiceController,
    private val startLocationTrackingUseCase: StartLocationTrackingUseCase,
    private val stopLocationTrackingUseCase: StopLocationTrackingUseCase,
    private val getLocationPointsUseCase: GetLocationPointsUseCase,
    private val clearLocationPointsUseCase: ClearLocationPointsUseCase,
    private val locationRepository: LocationRepository
) : BaseViewModel<MapState>() {

    private var locationTrackingJob: Job? = null
    private var lastKnownLocation: LocationPoint? = null
    private val locationPoints = mutableListOf<LocationPoint>()

    override fun setInitialState(): MapState = MapState.Idle

    init {
        loadSavedLocationPoints()
    }

    private fun loadSavedLocationPoints() {
        getLocationPointsUseCase()
            .onEach { result ->
                when (result) {
                    is Result.Success -> {
                        locationPoints.clear()
                        locationPoints.addAll(result.data ?: emptyList())
                        updateTrackingState()
                    }
                    is Result.Error -> {
                        setState(MapState.Error(result.error?.message ?: "Failed to load location points"))
                    }
                    Result.Loading -> {
                        setState(MapState.Loading)
                    }
                }
            }
            .catch { e ->
                setState(MapState.Error(e.message ?: "Unknown error occurred"))
            }
            .launchIn(viewModelScope)
    }

    fun startLocationTracking() {
        if (locationTrackingJob != null) return

        locationServiceController.startService()

        locationTrackingJob = startLocationTrackingUseCase()
            .onEach { result ->
                when (result) {
                    is Result.Success -> {
                        result.data?.let { location ->
                            lastKnownLocation = location
                            if (shouldAddNewPoint(location)) {
                                locationPoints.add(location)
                            }
                            updateTrackingState()
                        }
                    }
                    is Result.Error -> {
                        setState(MapState.Error(result.error?.message ?: "Failed to track location"))
                    }
                    Result.Loading -> {
                        setState(MapState.Loading)
                    }
                }
            }
            .catch { e ->
                setState(MapState.Error(e.message ?: "Unknown error occurred"))
            }
            .launchIn(viewModelScope)
    }

    fun stopLocationTracking() {
        locationServiceController.stopService()

        viewModelScope.launch {
            stopLocationTrackingUseCase()
                .collect { result ->
                    when (result) {
                        is Result.Success -> {
                            locationTrackingJob?.cancel()
                            locationTrackingJob = null
                            updateTrackingState()
                        }
                        is Result.Error -> {
                            setState(MapState.Error(result.error?.message ?: "Failed to stop tracking"))
                        }
                        Result.Loading -> {
                            setState(MapState.Loading)
                        }
                    }
                }
        }
    }

    fun clearRoute() {
        viewModelScope.launch {
            clearLocationPointsUseCase()
                .collect { result ->
                    when (result) {
                        is Result.Success -> {
                            locationPoints.clear()
                            updateTrackingState()
                        }
                        is Result.Error -> {
                            setState(MapState.Error(result.error?.message ?: "Failed to clear route"))
                        }
                        Result.Loading -> {
                            setState(MapState.Loading)
                        }
                    }
                }
        }
    }

    private fun updateTrackingState() {
        setState(
            MapState.Tracking(
                isTracking = locationTrackingJob != null,
                currentLocation = lastKnownLocation,
                locationPoints = locationPoints.toList()
            )
        )
    }

    private fun shouldAddNewPoint(newLocation: LocationPoint): Boolean {
        if (locationPoints.isEmpty()) return true

        val lastPoint = locationPoints.last()
        return calculateDistance(
            lastPoint.latitude, lastPoint.longitude,
            newLocation.latitude, newLocation.longitude
        ) >= MIN_DISTANCE_METERS
    }

    private fun calculateDistance(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Float {
        val results = FloatArray(1)
        android.location.Location.distanceBetween(lat1, lon1, lat2, lon2, results)
        return results[0]
    }

    suspend fun getAddressForLocation(latitude: Double, longitude: Double) {
        setState(MapState.Loading)
        when (val result = locationRepository.getAddressFromLocation(latitude, longitude)) {
            is Result.Success -> {
                setState(
                    MapState.AddressLoaded(
                        latitude = latitude,
                        longitude = longitude,
                        address = result.data ?: "Address not found"
                    )
                )
            }
            is Result.Error -> {
                setState(MapState.Error(result.error?.message ?: "Failed to get address"))
            }
            Result.Loading -> setState(MapState.Loading)
        }
    }

    companion object {
        private const val MIN_DISTANCE_METERS = 100f
    }
}