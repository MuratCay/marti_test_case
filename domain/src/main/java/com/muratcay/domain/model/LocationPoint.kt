package com.muratcay.domain.model

data class LocationPoint(
    val id: Long = 0,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val address: String? = null
) 