package com.muratcay.data.source

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.muratcay.domain.model.LocationPoint
import com.muratcay.data.local.dao.LocationDao
import com.muratcay.domain.utils.Result
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.Locale
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

interface LocationDataSource {
    fun startLocationUpdates(): Flow<LocationPoint>
    suspend fun stopLocationUpdates()
    suspend fun getAddressFromLocation(latitude: Double, longitude: Double): Result<String>
}

class LocationDataSourceImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val locationDao: LocationDao
) : LocationDataSource {

    private var fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)
    private var locationCallback: LocationCallback? = null
    private var lastLocation: android.location.Location? = null

    @SuppressLint("MissingPermission")
    override fun startLocationUpdates(): Flow<LocationPoint> = callbackFlow {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            UPDATE_INTERVAL
        ).setMinUpdateDistanceMeters(MIN_DISTANCE_METERS)
        .setWaitForAccurateLocation(true)
        .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    if (shouldUpdateLocation(location)) {
                        val locationPoint = LocationPoint(
                            latitude = location.latitude,
                            longitude = location.longitude
                        )
                        lastLocation = location
                        trySend(locationPoint)
                    }
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback!!,
            Looper.getMainLooper()
        )

        awaitClose {
            locationCallback?.let { callback ->
                fusedLocationClient.removeLocationUpdates(callback)
                locationCallback = null
            }
        }
    }

    private fun shouldUpdateLocation(newLocation: android.location.Location): Boolean {
        if (lastLocation == null) return true

        val distance = lastLocation!!.distanceTo(newLocation)
        return distance >= MIN_DISTANCE_METERS
    }

    override suspend fun stopLocationUpdates() {
        locationCallback?.let { callback ->
            fusedLocationClient.removeLocationUpdates(callback)
            locationCallback = null
        }
    }

    @Suppress("DEPRECATION")
    override suspend fun getAddressFromLocation(
        latitude: Double,
        longitude: Double
    ): Result<String> = suspendCoroutine { continuation ->
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            
            if (addresses.isNullOrEmpty()) {
                continuation.resume(Result.Error(Exception("No address found")))
                return@suspendCoroutine
            }

            val address = addresses[0]
            val addressText = StringBuilder()
            
            for (i in 0..address.maxAddressLineIndex) {
                addressText.append(address.getAddressLine(i))
                if (i < address.maxAddressLineIndex) {
                    addressText.append(", ")
                }
            }
            
            continuation.resume(Result.Success(addressText.toString()))
        } catch (e: Exception) {
            continuation.resume(Result.Error(e))
        }
    }

    companion object {
        private const val UPDATE_INTERVAL = 5000L // 5 seconds
        private const val MIN_DISTANCE_METERS = 100f // 100 meters
    }
} 