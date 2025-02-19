package com.muratcay.presentation.map

import com.muratcay.presentation.base.IState

sealed interface MapState : IState {
    data object Loading: MapState
}