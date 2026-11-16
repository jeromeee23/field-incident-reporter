package com.example.incidentreporter.di

import com.example.incidentreporter.data.repository.IncidentRepositoryImpl
import com.example.incidentreporter.domain.repository.IncidentRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindIncidentRepository(
        impl: IncidentRepositoryImpl,
    ): IncidentRepository
}
