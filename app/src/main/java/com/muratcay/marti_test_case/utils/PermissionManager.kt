package com.muratcay.marti_test_case.utils

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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
                .setTitle("Konum İzni Gerekli")
                .setMessage("Uygulamanın düzgün çalışması için konum izni gereklidir.")
                .setPositiveButton("İzin Ver") { _, _ -> onPositive() }
                .setNegativeButton("İptal") { dialog, _ ->
                    dialog.dismiss()
                    permissionCallback?.onPermissionDenied()
                }
                .show()
        }
    }

    private fun showBackgroundLocationPermissionRationale(onPositive: () -> Unit) {
        activity?.let { activity ->
            MaterialAlertDialogBuilder(activity)
                .setTitle("Arka Plan Konum İzni")
                .setMessage("Uygulama kapalıyken de konum takibi için arka plan konum izni gereklidir.")
                .setPositiveButton("İzin Ver") { _, _ -> onPositive() }
                .setNegativeButton("İptal") { dialog, _ ->
                    dialog.dismiss()
                    permissionCallback?.onPermissionDenied()
                }
                .show()
        }
    }

    private fun showNotificationPermissionRationale(onPositive: () -> Unit) {
        activity?.let { activity ->
            MaterialAlertDialogBuilder(activity)
                .setTitle("Bildirim İzni Gerekli")
                .setMessage("Önemli bilgileri alabilmeniz için bildirim izni gereklidir.")
                .setPositiveButton("İzin Ver") { _, _ -> onPositive() }
                .setNegativeButton("İptal") { dialog, _ ->
                    dialog.dismiss()
                    permissionCallback?.onPermissionDenied()
                }
                .show()
        }
    }

    private fun showPermissionDeniedDialog() {
        activity?.let { activity ->
            MaterialAlertDialogBuilder(activity)
                .setTitle("İzinler Reddedildi")
                .setMessage("Uygulamanın düzgün çalışması için gerekli izinleri ayarlardan etkinleştirmelisiniz.")
                .setPositiveButton("Ayarlara Git") { _, _ -> openAppSettings() }
                .setNegativeButton("İptal") { dialog, _ -> dialog.dismiss() }
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