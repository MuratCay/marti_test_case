package com.muratcay.presentation.map

import com.muratcay.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor() : BaseViewModel<MapState> () {

    override fun setInitialState(): MapState = MapState.Loading

    init {

    }

}