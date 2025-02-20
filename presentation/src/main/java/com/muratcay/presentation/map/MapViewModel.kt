package com.muratcay.presentation.map

import androidx.lifecycle.viewModelScope
import com.muratcay.domain.model.LocationPoint
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
    private val startLocationTrackingUseCase: StartLocationTrackingUseCase,
    private val stopLocationTrackingUseCase: StopLocationTrackingUseCase,
    private val getLocationPointsUseCase: GetLocationPointsUseCase,
    private val clearLocationPointsUseCase: ClearLocationPointsUseCase
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
                    is Result.Loading -> {
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

        locationTrackingJob = startLocationTrackingUseCase()
            .onEach { result ->
                when (result) {
                    is Result.Success -> {
                        result.data?.let { location ->
                            lastKnownLocation = location
                            // Only add points if they are more than 100 meters apart
                            if (shouldAddNewPoint(location)) {
                                locationPoints.add(location)
                            }
                            updateTrackingState()
                        }
                    }
                    is Result.Error -> {
                        setState(MapState.Error(result.error?.message ?: "Failed to track location"))
                    }
                    is Result.Loading -> {
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
                        is Result.Loading -> {
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
                        is Result.Loading -> {
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

    companion object {
        private const val MIN_DISTANCE_METERS = 100f
    }
}