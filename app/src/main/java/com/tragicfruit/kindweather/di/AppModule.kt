package com.tragicfruit.kindweather.di

import android.content.Context
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.tragicfruit.kindweather.BuildConfig
import com.tragicfruit.kindweather.data.source.remote.DarkSkyAPIService
import com.tragicfruit.kindweather.util.DefaultSharedPrefsHelper
import com.tragicfruit.kindweather.util.SharedPrefsHelper
import com.tragicfruit.kindweather.util.ViewHelper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.create

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @ExperimentalSerializationApi
    @Provides
    @Singleton
    fun providesApiService(): DarkSkyAPIService {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.API_URL)
            .addConverterFactory(
                Json {
                    isLenient = true
                    ignoreUnknownKeys = true
                }.asConverterFactory("application/json".toMediaType())
            )
            .build()
            .create()
    }

    @Provides
    @Singleton
    fun providesSharedPrefsHelper(@ApplicationContext context: Context): SharedPrefsHelper {
        return DefaultSharedPrefsHelper(context)
    }

    @Provides
    @Singleton
    fun providesViewHelper(@ApplicationContext context: Context): ViewHelper {
        return ViewHelper(context)
    }
}
