package com.muratcay.marti_test_case.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import javax.inject.Inject

class PermissionManager @Inject constructor() {

    private var activity: AppCompatActivity? = null
    private var onPermissionGranted: (() -> Unit)? = null
    private var onPermissionDenied: (() -> Unit)? = null

    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var backgroundLocationPermissionLauncher: ActivityResultLauncher<String>

    fun init(activity: AppCompatActivity) {
        this.activity = activity
        setupPermissionLaunchers(activity)
    }

    private fun setupPermissionLaunchers(activity: AppCompatActivity) {
        permissionLauncher = activity.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val allGranted = permissions.entries.all { it.value }
            if (allGranted) {
                onPermissionGranted?.invoke()
            } else {
                onPermissionDenied?.invoke()
                showPermissionDeniedDialog(activity)
            }
        }

        backgroundLocationPermissionLauncher = activity.registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                onPermissionGranted?.invoke()
            } else {
                onPermissionDenied?.invoke()
                showPermissionDeniedDialog(activity)
            }
        }
    }

    fun requestLocationPermissions(
        onGranted: () -> Unit = {},
        onDenied: () -> Unit = {}
    ) {
        this.onPermissionGranted = onGranted
        this.onPermissionDenied = onDenied

        activity?.let { activity ->
            when {
                hasLocationPermissions(activity) -> onGranted()
                shouldShowLocationRationale(activity) -> showLocationRationaleDialog(activity)
                else -> permissionLauncher.launch(PermissionUtils.LOCATION_PERMISSIONS)
            }
        }
    }

    fun requestBackgroundLocationPermission(
        onGranted: () -> Unit = {},
        onDenied: () -> Unit = {}
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            this.onPermissionGranted = onGranted
            this.onPermissionDenied = onDenied

            activity?.let { activity ->
                when {
                    hasBackgroundLocationPermission(activity) -> onGranted()
                    shouldShowBackgroundLocationRationale(activity) -> 
                        showBackgroundLocationRationaleDialog(activity)
                    else -> backgroundLocationPermissionLauncher.launch(
                        android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    )
                }
            }
        } else {
            onGranted()
        }
    }

    fun requestNotificationPermission(
        onGranted: () -> Unit = {},
        onDenied: () -> Unit = {}
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            this.onPermissionGranted = onGranted
            this.onPermissionDenied = onDenied

            activity?.let { activity ->
                when {
                    hasNotificationPermission(activity) -> onGranted()
                    shouldShowNotificationRationale(activity) -> 
                        showNotificationRationaleDialog(activity)
                    else -> permissionLauncher.launch(PermissionUtils.NOTIFICATION_PERMISSION)
                }
            }
        } else {
            onGranted()
        }
    }

    private fun hasLocationPermissions(context: Context): Boolean {
        return PermissionUtils.LOCATION_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun hasBackgroundLocationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun shouldShowLocationRationale(activity: Activity): Boolean {
        return PermissionUtils.LOCATION_PERMISSIONS.any {
            ActivityCompat.shouldShowRequestPermissionRationale(activity, it)
        }
    }

    private fun shouldShowBackgroundLocationRationale(activity: Activity): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ActivityCompat.shouldShowRequestPermissionRationale(
                activity,
                android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        } else {
            false
        }
    }

    private fun shouldShowNotificationRationale(activity: Activity): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.shouldShowRequestPermissionRationale(
                activity,
                android.Manifest.permission.POST_NOTIFICATIONS
            )
        } else {
            false
        }
    }

    private fun showLocationRationaleDialog(activity: Activity) {
        MaterialAlertDialogBuilder(activity)
            .setTitle("Konum İzni Gerekli")
            .setMessage("Uygulamanın düzgün çalışması için konum izni gereklidir.")
            .setPositiveButton("İzin Ver") { _, _ ->
                permissionLauncher.launch(PermissionUtils.LOCATION_PERMISSIONS)
            }
            .setNegativeButton("İptal") { dialog, _ ->
                dialog.dismiss()
                onPermissionDenied?.invoke()
            }
            .show()
    }

    private fun showBackgroundLocationRationaleDialog(activity: Activity) {
        MaterialAlertDialogBuilder(activity)
            .setTitle("Arka Plan Konum İzni")
            .setMessage("Uygulama kapalıyken de konum takibi için arka plan konum izni gereklidir.")
            .setPositiveButton("İzin Ver") { _, _ ->
                backgroundLocationPermissionLauncher.launch(
                    android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )
            }
            .setNegativeButton("İptal") { dialog, _ ->
                dialog.dismiss()
                onPermissionDenied?.invoke()
            }
            .show()
    }

    private fun showNotificationRationaleDialog(activity: Activity) {
        MaterialAlertDialogBuilder(activity)
            .setTitle("Bildirim İzni Gerekli")
            .setMessage("Önemli bilgileri alabilmeniz için bildirim izni gereklidir.")
            .setPositiveButton("İzin Ver") { _, _ ->
                permissionLauncher.launch(PermissionUtils.NOTIFICATION_PERMISSION)
            }
            .setNegativeButton("İptal") { dialog, _ ->
                dialog.dismiss()
                onPermissionDenied?.invoke()
            }
            .show()
    }

    private fun showPermissionDeniedDialog(activity: Activity) {
        MaterialAlertDialogBuilder(activity)
            .setTitle("İzinler Reddedildi")
            .setMessage("Uygulamanın düzgün çalışması için gerekli izinleri ayarlardan etkinleştirmelisiniz.")
            .setPositiveButton("Ayarlara Git") { _, _ ->
                openAppSettings(activity)
            }
            .setNegativeButton("İptal") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun openAppSettings(activity: Activity) {
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", activity.packageName, null)
            activity.startActivity(this)
        }
    }

    fun onCleared() {
        activity = null
        onPermissionGranted = null
        onPermissionDenied = null
    }
} 