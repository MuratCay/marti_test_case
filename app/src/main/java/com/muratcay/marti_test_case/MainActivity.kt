package com.muratcay.marti_test_case

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commitNow
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.muratcay.marti_test_case.databinding.ActivityMainBinding
import com.muratcay.marti_test_case.utils.PermissionManager
import com.muratcay.marti_test_case.utils.PermissionUtils
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var permissionManager: PermissionManager

    private var navHostFragment: NavHostFragment? = null
    private var navController: NavController? = null
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initializeApp()
    }

    private fun initializeApp() {
        if (hasAllRequiredPermissions()) {
            startApp()
        } else {
            setupPermissions()
        }
    }

    private fun hasAllRequiredPermissions(): Boolean {
        return PermissionUtils.hasLocationPermissions(this) &&
                PermissionUtils.hasBackgroundLocationPermission(this) &&
                PermissionUtils.hasNotificationPermission(this)
    }

    private fun setupPermissions() {
        permissionManager.init(this)
        requestLocationPermission()
    }

    private fun requestLocationPermission() {
        permissionManager.requestLocationPermissions(object : PermissionManager.PermissionCallback {
            override fun onPermissionGranted() {
                requestBackgroundLocationPermission()
            }

            override fun onPermissionDenied() {
                showPermissionRequiredDialog(getString(R.string.the_app_cannot_work_without_location_permission)) {
                    requestLocationPermission()
                }
            }
        })
    }

    private fun requestBackgroundLocationPermission() {
        permissionManager.requestBackgroundLocationPermission(object : PermissionManager.PermissionCallback {
            override fun onPermissionGranted() {
                requestNotificationPermission()
            }

            override fun onPermissionDenied() {
                showPermissionRequiredDialog(getString(R.string.the_app_cannot_work_without_background_location_permission)) {
                    requestBackgroundLocationPermission()
                }
            }
        })
    }

    private fun requestNotificationPermission() {
        permissionManager.requestNotificationPermission(object : PermissionManager.PermissionCallback {
            override fun onPermissionGranted() {
                startApp()
            }

            override fun onPermissionDenied() {
                startApp()
            }
        })
    }

    private fun showPermissionRequiredDialog(message: String, onRetry: () -> Unit) {
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.permission_required))
            .setMessage(message)
            .setPositiveButton(getString(R.string.try_again)) { _, _ -> onRetry() }
            .setNegativeButton(getString(R.string.exit_application)) { _, _ -> finish() }
            .setCancelable(false)
            .show()
    }

    private fun startApp() {
        setupNavigation()
    }

    private fun setupNavigation() {
        // TODO: Bu kodları yazmamdaki amaç, emülatörlerde ilk çalıştırmada izinler verildikten hemen sonra konum bilgisini edinememektir. Fiziksel cihazlarda ise böyle bir problemle karşılaşılmamaktadır.
        navHostFragment = NavHostFragment().apply {
            arguments = Bundle().apply {
                putInt("android-support-nav:fragment:graphId", R.navigation.nav_graph)
            }
        }
        supportFragmentManager.commitNow {
            replace(R.id.container, navHostFragment!!)
            setPrimaryNavigationFragment(navHostFragment)
        }
        navController = navHostFragment?.navController
    }

    override fun onDestroy() {
        super.onDestroy()
        permissionManager.onCleared()
        navController = null
        navHostFragment = null
    }
}