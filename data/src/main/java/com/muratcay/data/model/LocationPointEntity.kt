package com.muratcay.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "location_points")
data class LocationPointEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val address: String? = null
) 