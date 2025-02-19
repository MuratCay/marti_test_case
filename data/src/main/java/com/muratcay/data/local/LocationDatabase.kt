package com.muratcay.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.muratcay.data.local.dao.LocationDao
import com.muratcay.data.model.LocationPointEntity

@Database(
    entities = [LocationPointEntity::class],
    version = 1,
    exportSchema = false
)
abstract class LocationDatabase : RoomDatabase() {
    abstract fun locationDao(): LocationDao
} 