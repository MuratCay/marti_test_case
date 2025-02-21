package com.muratcay.marti_test_case.feature.map

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.muratcay.domain.model.LocationPoint
import com.muratcay.marti_test_case.R
import com.muratcay.marti_test_case.databinding.FragmentMapBinding
import com.muratcay.marti_test_case.utils.PermissionUtils
import com.muratcay.presentation.base.BaseFragment
import com.muratcay.presentation.ui.map.MapState
import com.muratcay.presentation.ui.map.MapViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MapFragment : BaseFragment<FragmentMapBinding, MapViewModel>(R.layout.fragment_map),
    OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private var googleMap: GoogleMap? = null
    private var isTracking = false
    private val markers = mutableMapOf<LatLng, Marker>()
    private var currentPolyline: com.google.android.gms.maps.model.Polyline? = null

    override fun getViewModelClass(): Class<MapViewModel> = MapViewModel::class.java

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupMapFragment()
        setupClickListeners()
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
                if (PermissionUtils.hasLocationPermissions(requireContext())) {
                    viewModel.startLocationTracking()
                } else {
                    showSnackbar(getString(R.string.location_permission_required))
                }
            }
        }

        binding.fabClear.setOnClickListener {
            clearMapAndRoute()
            viewModel.clearRoute()
        }
    }

    private fun setupMap() {
        try {
            if (PermissionUtils.hasLocationPermissions(requireContext())) {
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
                clearMapAndRoute()
            }
            is MapState.AddressLoaded -> {
                binding.progressBar.visibility = View.GONE
                val position = LatLng(state.latitude, state.longitude)
                markers[position]?.let { marker ->
                    updateMarkerAddress(marker, state.address)
                }
            }
            else -> Unit
        }
    }

    private fun updateTrackingUI(isTracking: Boolean) {
        binding.fabStartStop.setImageResource(
            if (isTracking) R.drawable.ic_stop else R.drawable.ic_play
        )
    }

    private fun updateMapLocation(latitude: Double, longitude: Double) {
        try {
            if (PermissionUtils.hasLocationPermissions(requireContext())) {
                val location = LatLng(latitude, longitude)
                googleMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
            }
        } catch (e: SecurityException) {
            showSnackbar(getString(R.string.location_permission_required))
        }
    }

    private fun clearMapAndRoute() {
        markers.values.forEach { marker ->
            marker.remove()
        }
        markers.clear()
        currentPolyline?.remove()
        currentPolyline = null
        googleMap?.clear()
    }

    private fun updateRouteOnMap(points: List<LocationPoint>) {
        try {
            if (!PermissionUtils.hasLocationPermissions(requireContext())) {
                showSnackbar(getString(R.string.location_permission_required))
                return
            }

            currentPolyline?.remove()
            currentPolyline = null

            if (points.isEmpty()) return

            // Draw route
            val routePoints = points.map { LatLng(it.latitude, it.longitude) }
            currentPolyline = googleMap?.addPolyline(
                PolylineOptions()
                    .addAll(routePoints)
                    .color(ContextCompat.getColor(requireContext(), R.color.purple_500))
                    .width(12f)
            )

            // Add markers only for new points
            points.forEach { point ->
                val position = LatLng(point.latitude, point.longitude)
                if (!markers.containsKey(position)) {
                    googleMap?.addMarker(
                        MarkerOptions()
                            .position(position)
                            .title(point.address ?: "Location point")
                    )?.let { marker ->
                        markers[position] = marker
                    }
                }
            }
        } catch (e: SecurityException) {
            showSnackbar(getString(R.string.location_permission_required))
        }
    }

    @SuppressLint("PotentialBehaviorOverride")
    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        setupMap()
        googleMap?.setOnMarkerClickListener(this)
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        val position = marker.position
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.getAddressForLocation(position.latitude, position.longitude)
        }
        return false
    }

    private fun updateMarkerAddress(marker: Marker, address: String) {
        marker.title = address
        marker.showInfoWindow()
    }

    @SuppressLint("PotentialBehaviorOverride")
    override fun onDestroyView() {
        super.onDestroyView()
        clearMapAndRoute()
        googleMap?.setOnMarkerClickListener(null)
        googleMap = null
    }
}