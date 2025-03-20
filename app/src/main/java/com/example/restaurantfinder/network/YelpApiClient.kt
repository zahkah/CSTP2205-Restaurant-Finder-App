package com.example.restaurantfinder.network

import com.example.restaurantfinder.util.YelpCredentialsProvider
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Client for creating and accessing the Yelp API service.
 * Handles the setup and configuration of Retrofit.
 */
object YelpApiClient {
    private const val BASE_URL = "https://api.yelp.com/v3/"

    /**
     * Creates an OkHttpClient with logging and timeout configurations
     */
    private val okHttpClient by lazy {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Authorization", YelpCredentialsProvider.getYelpAuthHeader())
                    .build()
                chain.proceed(request)
            }
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    /**
     * Creates and configures the Retrofit instance
     */
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /**
     * Creates an implementation of the YelpApiService interface
     */
    val apiService: YelpApiService by lazy {
        retrofit.create(YelpApiService::class.java)
    }
}