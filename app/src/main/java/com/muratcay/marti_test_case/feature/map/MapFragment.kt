package com.muratcay.marti_test_case.feature.map

import com.muratcay.marti_test_case.R
import com.muratcay.marti_test_case.databinding.FragmentMapBinding
import com.muratcay.presentation.base.BaseFragment
import com.muratcay.presentation.map.MapViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MapFragment : BaseFragment<FragmentMapBinding, MapViewModel>(R.layout.fragment_map) {

    override fun getViewModelClass(): Class<MapViewModel> = MapViewModel::class.java

    override fun initObserver() {

    }

    override fun initViews() {

    }

}