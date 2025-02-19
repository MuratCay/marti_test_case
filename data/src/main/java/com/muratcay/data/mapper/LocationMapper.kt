package com.muratcay.data.mapper

import com.muratcay.domain.model.LocationPoint
import com.muratcay.data.model.LocationPointEntity

fun LocationPointEntity.toDomain(): LocationPoint {
    return LocationPoint(
        id = id,
        latitude = latitude,
        longitude = longitude,
        timestamp = timestamp,
        address = address
    )
}

fun LocationPoint.toEntity(): LocationPointEntity {
    return LocationPointEntity(
        id = id,
        latitude = latitude,
        longitude = longitude,
        timestamp = timestamp,
        address = address
    )
} 