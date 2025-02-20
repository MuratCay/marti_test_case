package com.muratcay.marti_test_case.utils

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.muratcay.marti_test_case.R
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PermissionManager @Inject constructor() {
    private var activity: AppCompatActivity? = null
    private var permissionCallback: PermissionCallback? = null
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var singlePermissionLauncher: ActivityResultLauncher<String>

    fun init(activity: AppCompatActivity) {
        this.activity = activity
        setupPermissionLaunchers()
    }

    private fun setupPermissionLaunchers() {
        activity?.let { activity ->
            permissionLauncher = activity.registerForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) { permissions ->
                val allGranted = permissions.entries.all { it.value }
                handlePermissionResult(allGranted)
            }

            singlePermissionLauncher = activity.registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                handlePermissionResult(isGranted)
            }
        }
    }

    fun requestLocationPermissions(callback: PermissionCallback) {
        this.permissionCallback = callback
        activity?.let { activity ->
            when {
                PermissionUtils.hasLocationPermissions(activity) -> {
                    permissionCallback?.onPermissionGranted()
                }
                else -> {
                    showLocationPermissionRationale {
                        permissionLauncher.launch(PermissionUtils.locationPermissions)
                    }
                }
            }
        }
    }

    fun requestBackgroundLocationPermission(callback: PermissionCallback) {
        this.permissionCallback = callback
        PermissionUtils.backgroundLocationPermission?.let { permission ->
            activity?.let { activity ->
                when {
                    PermissionUtils.hasBackgroundLocationPermission(activity) -> {
                        permissionCallback?.onPermissionGranted()
                    }
                    else -> {
                        showBackgroundLocationPermissionRationale {
                            singlePermissionLauncher.launch(permission)
                        }
                    }
                }
            }
        } ?: permissionCallback?.onPermissionGranted()
    }

    fun requestNotificationPermission(callback: PermissionCallback) {
        this.permissionCallback = callback
        PermissionUtils.notificationPermission?.let { permission ->
            activity?.let { activity ->
                when {
                    PermissionUtils.hasNotificationPermission(activity) -> {
                        permissionCallback?.onPermissionGranted()
                    }
                    else -> {
                        showNotificationPermissionRationale {
                            singlePermissionLauncher.launch(permission)
                        }
                    }
                }
            }
        } ?: permissionCallback?.onPermissionGranted()
    }

    private fun handlePermissionResult(isGranted: Boolean) {
        if (isGranted) {
            permissionCallback?.onPermissionGranted()
        } else {
            permissionCallback?.onPermissionDenied()
            showPermissionDeniedDialog()
        }
    }

    private fun showLocationPermissionRationale(onPositive: () -> Unit) {
        activity?.let { activity ->
            MaterialAlertDialogBuilder(activity)
                .setTitle(activity.getString(R.string.location_permission_required))
                .setMessage(activity.getString(R.string.location_permission_required_app_function))
                .setPositiveButton(activity.getString(R.string.allow)) { _, _ -> onPositive() }
                .setNegativeButton(activity.getString(R.string.cancel)) { dialog, _ ->
                    dialog.dismiss()
                    permissionCallback?.onPermissionDenied()
                }
                .show()
        }
    }

    private fun showBackgroundLocationPermissionRationale(onPositive: () -> Unit) {
        activity?.let { activity ->
            MaterialAlertDialogBuilder(activity)
                .setTitle(activity.getString(R.string.background_location_permission))
                .setMessage(activity.getString(R.string.background_location_permission_required_app_closed))
                .setPositiveButton(activity.getString(R.string.allow)) { _, _ -> onPositive() }
                .setNegativeButton(activity.getString(R.string.cancel)) { dialog, _ ->
                    dialog.dismiss()
                    permissionCallback?.onPermissionDenied()
                }
                .show()
        }
    }

    private fun showNotificationPermissionRationale(onPositive: () -> Unit) {
        activity?.let { activity ->
            MaterialAlertDialogBuilder(activity)
                .setTitle(activity.getString(R.string.notification_permission_required))
                .setMessage(activity.getString(R.string.notification_permission_required_important_info))
                .setPositiveButton(activity.getString(R.string.allow)) { _, _ -> onPositive() }
                .setNegativeButton(activity.getString(R.string.cancel)) { dialog, _ ->
                    dialog.dismiss()
                    permissionCallback?.onPermissionDenied()
                }
                .show()
        }
    }

    private fun showPermissionDeniedDialog() {
        activity?.let { activity ->
            MaterialAlertDialogBuilder(activity)
                .setTitle(activity.getString(R.string.permissions_denied))
                .setMessage(activity.getString(R.string.permissions_required_settings))
                .setPositiveButton(activity.getString(R.string.go_to_settings)) { _, _ -> openAppSettings() }
                .setNegativeButton(activity.getString(R.string.cancel)) { dialog, _ -> dialog.dismiss() }
                .show()
        }
    }

    private fun openAppSettings() {
        activity?.let { activity ->
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", activity.packageName, null)
                activity.startActivity(this)
            }
        }
    }

    fun onCleared() {
        activity = null
        permissionCallback = null
    }

    interface PermissionCallback {
        fun onPermissionGranted()
        fun onPermissionDenied()
    }
} 