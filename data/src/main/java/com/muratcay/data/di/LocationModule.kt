package com.muratcay.data.di

import android.content.Context
import androidx.room.Room
import com.muratcay.data.local.LocationDatabase
import com.muratcay.data.local.dao.LocationDao
import com.muratcay.data.repository.LocationRepositoryImpl
import com.muratcay.data.source.LocationDataSource
import com.muratcay.data.source.LocationDataSourceImpl
import com.muratcay.domain.repository.LocationRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class LocationModule {

    @Binds
    abstract fun bindLocationRepository(
        locationRepositoryImpl: LocationRepositoryImpl
    ): LocationRepository

    @Binds
    abstract fun bindLocationDataSource(
        locationDataSourceImpl: LocationDataSourceImpl
    ): LocationDataSource

    companion object {
        @Provides
        @Singleton
        fun provideLocationDatabase(
            @ApplicationContext context: Context
        ): LocationDatabase {
            return Room.databaseBuilder(
                context,
                LocationDatabase::class.java,
                "location_database"
            ).build()
        }

        @Provides
        fun provideLocationDao(database: LocationDatabase): LocationDao {
            return database.locationDao()
        }
    }
} 