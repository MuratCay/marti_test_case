package com.muratcay.marti_test_case.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.muratcay.domain.model.LocationPoint
import com.muratcay.domain.usecase.StartLocationTrackingUseCase
import com.muratcay.domain.utils.Result
import com.muratcay.marti_test_case.MainActivity
import com.muratcay.marti_test_case.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import timber.log.Timber

@AndroidEntryPoint
class LocationService : Service() {

    @Inject
    lateinit var startLocationTrackingUseCase: StartLocationTrackingUseCase

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> start()
            ACTION_STOP -> stop()
        }
        return START_STICKY
    }

    private fun start() {
        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Location Tracking")
            .setContentText("Starting location tracking...")
            .setSmallIcon(R.drawable.ic_location)
            .setOngoing(true)
            .setContentIntent(getPendingIntent())
            .build()

        startForeground(NOTIFICATION_ID, notification)

        startLocationTrackingUseCase()
            .onEach { result ->
                when (result) {
                    is Result.Success -> {
                        result.data?.let { location ->
                            Timber.d("Location update: ${location.latitude}, ${location.longitude}")
                            updateNotification(location)
                        }
                    }
                    is Result.Error -> {
                        Timber.e(result.error, "Location tracking error")
                        stopSelf()
                    }
                    is Result.Loading -> {
                        Timber.d("Location tracking loading")
                    }
                }
            }
            .catch { e ->
                Timber.e(e, "Location tracking failed")
                stopSelf()
            }
            .launchIn(serviceScope)
    }

    private fun stop() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Location Tracking",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Used for tracking location in background"
                setShowBadge(false)
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun getPendingIntent(): PendingIntent {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        return PendingIntent.getActivity(this, 0, intent, flags)
    }

    @SuppressLint("DefaultLocale")
    private fun updateNotification(location: LocationPoint) {
        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Location Tracking Active")
            .setContentText("Location: ${String.format("%.6f", location.latitude)}, ${String.format("%.6f", location.longitude)}")
            .setSmallIcon(R.drawable.ic_location)
            .setOngoing(true)
            .setContentIntent(getPendingIntent())
            .build()

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
        private const val NOTIFICATION_CHANNEL_ID = "location_tracking_channel"
        private const val NOTIFICATION_ID = 1
    }
} 