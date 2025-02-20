package com.muratcay.marti_test_case.feature.map

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.muratcay.domain.model.LocationPoint
import com.muratcay.marti_test_case.R
import com.muratcay.marti_test_case.databinding.FragmentMapBinding
import com.muratcay.presentation.base.BaseFragment
import com.muratcay.presentation.map.MapState
import com.muratcay.presentation.map.MapViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MapFragment : BaseFragment<FragmentMapBinding, MapViewModel>(R.layout.fragment_map),
    OnMapReadyCallback {

    private var googleMap: GoogleMap? = null
    private var isTracking = false

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true -> {
                setupMap()
            }
            else -> {
                showSnackbar(getString(R.string.location_permission_required))
            }
        }
    }

    override fun getViewModelClass(): Class<MapViewModel> = MapViewModel::class.java

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupMapFragment()
        setupClickListeners()
        checkLocationPermission()
    }

    override fun initObserver() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    handleState(state)
                }
            }
        }
    }

    override fun initViews() {
        binding.viewModel = viewModel
    }

    private fun setupMapFragment() {
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun setupClickListeners() {
        binding.fabStartStop.setOnClickListener {
            if (isTracking) {
                viewModel.stopLocationTracking()
            } else {
                if (hasLocationPermission()) {
                    viewModel.startLocationTracking()
                } else {
                    requestLocationPermission()
                }
            }
        }

        binding.fabClear.setOnClickListener {
            viewModel.clearRoute()
        }
    }

    private fun checkLocationPermission() {
        when {
            hasLocationPermission() -> {
                setupMap()
            }
            else -> {
                requestLocationPermission()
            }
        }
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    private fun setupMap() {
        try {
            if (hasLocationPermission()) {
                googleMap?.isMyLocationEnabled = true
                googleMap?.uiSettings?.apply {
                    isMyLocationButtonEnabled = true
                    isZoomControlsEnabled = true
                    isCompassEnabled = true
                }
            }
        } catch (e: SecurityException) {
            showSnackbar(getString(R.string.location_permission_required))
        }
    }

    private fun handleState(state: MapState) {
        when (state) {
            is MapState.Loading -> {
                binding.progressBar.visibility = View.VISIBLE
            }
            is MapState.Tracking -> {
                binding.progressBar.visibility = View.GONE
                isTracking = state.isTracking
                updateTrackingUI(state.isTracking)
                state.currentLocation?.let { updateMapLocation(it.latitude, it.longitude) }
                updateRouteOnMap(state.locationPoints)
            }
            is MapState.Error -> {
                binding.progressBar.visibility = View.GONE
                showSnackbar(state.message)
            }
            is MapState.Idle -> {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun updateTrackingUI(isTracking: Boolean) {
        binding.fabStartStop.setImageResource(
            if (isTracking) R.drawable.ic_stop else R.drawable.ic_play
        )
    }

    private fun updateMapLocation(latitude: Double, longitude: Double) {
        try {
            if (hasLocationPermission()) {
                val location = LatLng(latitude, longitude)
                googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
            }
        } catch (e: SecurityException) {
            showSnackbar(getString(R.string.location_permission_required))
        }
    }

    private fun updateRouteOnMap(points: List<LocationPoint>) {
        try {
            if (!hasLocationPermission()) {
                showSnackbar(getString(R.string.location_permission_required))
                return
            }

            googleMap?.clear()

            if (points.isEmpty()) return

            // Draw route
            val routePoints = points.map { LatLng(it.latitude, it.longitude) }
            googleMap?.addPolyline(
                PolylineOptions()
                    .addAll(routePoints)
                    .color(ContextCompat.getColor(requireContext(), R.color.purple_500))
                    .width(12f)
            )

            // Add markers
            points.forEach { point ->
                googleMap?.addMarker(
                    MarkerOptions()
                        .position(LatLng(point.latitude, point.longitude))
                        .title(point.address ?: "Location point")
                )
            }
        } catch (e: SecurityException) {
            showSnackbar(getString(R.string.location_permission_required))
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        setupMap()
    }
}