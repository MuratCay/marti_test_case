package com.muratcay.marti_test_case.service

import android.content.Context
import android.content.Intent
import com.muratcay.domain.service.LocationServiceController
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class LocationServiceControllerImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : LocationServiceController {

    override fun startService() {
        val intent = Intent(context, LocationService::class.java).apply {
            action = LocationService.ACTION_START
        }
        context.startService(intent)
    }

    override fun stopService() {
        val intent = Intent(context, LocationService::class.java).apply {
            action = LocationService.ACTION_STOP
        }
        context.startService(intent)
    }
} 