package com.example.restaurantfinder.util

import android.content.Context
import android.util.Log
import com.example.restaurantfinder.BuildConfig

object YelpCredentialsProvider {
    private var yelpApiKey: String? = null
    private var yelpClientId: String? = null
    private var isInitialized = false

    fun initialize() {
        if (isInitialized) {
            return
        }

        try {
            // Get API key from BuildConfig
            yelpApiKey = BuildConfig.YELP_API_KEY
            yelpClientId = BuildConfig.YELP_CLIENT_ID

            if (yelpApiKey.isNullOrEmpty() || yelpClientId.isNullOrEmpty()) {
                Log.e("YelpCredentialsProvider", "Missing Yelp credentials in BuildConfig")
            } else {
                Log.d("YelpCredentialsProvider", "Successfully loaded Yelp credentials from BuildConfig")
                isInitialized = true
            }
        } catch (e: Exception) {
            Log.e("YelpCredentialsProvider", "Error loading Yelp credentials: ${e.message}")
        }
    }

    fun getYelpApiKey(): String {
        if (!isInitialized) initialize()
        return yelpApiKey ?: throw IllegalStateException("Yelp API key not initialized")
    }

    fun getYelpClientId(): String {
        if (!isInitialized) initialize()
        return yelpClientId ?: throw IllegalStateException("Yelp Client ID not initialized")
    }

    fun getYelpAuthHeader(): String {
        return "Bearer ${getYelpApiKey()}"
    }
}