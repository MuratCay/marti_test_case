package com.muratcay.marti_test_case

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.muratcay.marti_test_case.databinding.ActivityMainBinding
import com.muratcay.marti_test_case.utils.PermissionManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var permissionManager: PermissionManager

    private lateinit var navHostFragment: NavHostFragment
    private lateinit var navController: NavController
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setupPermissions()
    }

    private fun setupPermissions() {
        permissionManager.init(this)
        permissionManager.requestLocationPermissions(
            onGranted = {
                permissionManager.requestBackgroundLocationPermission(
                    onGranted = {
                        requestNotificationPermission()
                    },
                    onDenied = {
                        requestNotificationPermission()
                    }
                )
            },
            onDenied = {
                requestNotificationPermission()
            }
        )
    }

    private fun requestNotificationPermission() {
        permissionManager.requestNotificationPermission(
            onGranted = {
                startApp()
            },
            onDenied = {
                startApp()
            }
        )
    }

    private fun startApp() {
        setupNavigation()
    }

    private fun setupNavigation() {
        navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
    }

    override fun onDestroy() {
        super.onDestroy()
        permissionManager.onCleared()
    }
}