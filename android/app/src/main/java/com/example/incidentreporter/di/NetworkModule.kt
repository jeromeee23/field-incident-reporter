package com.example.incidentreporter.di

import android.content.Context
import android.content.pm.ApplicationInfo
import com.example.incidentreporter.data.remote.api.IncidentApiService
import com.example.incidentreporter.data.remote.api.PhotoApiService
import com.example.incidentreporter.network.buildOkHttpClient
import com.example.incidentreporter.network.buildRetrofit
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(
        @ApplicationContext context: Context,
    ): OkHttpClient {
        val isDebug = (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        return buildOkHttpClient(context, isDebug)
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return buildRetrofit(okHttpClient)
    }

    @Provides
    @Singleton
    fun provideIncidentApiService(retrofit: Retrofit): IncidentApiService {
        return retrofit.create(IncidentApiService::class.java)
    }

    @Provides
    @Singleton
    fun providePhotoApiService(retrofit: Retrofit): PhotoApiService {
        return retrofit.create(PhotoApiService::class.java)
    }
}
