package com.muratcay.marti_test_case.di

import com.muratcay.domain.service.LocationServiceController
import com.muratcay.marti_test_case.service.LocationServiceControllerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class ServiceModule {

    @Binds
    abstract fun bindLocationServiceController(
        impl: LocationServiceControllerImpl
    ): LocationServiceController
} 