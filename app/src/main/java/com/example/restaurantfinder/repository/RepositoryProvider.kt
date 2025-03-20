package com.example.restaurantfinder.repository


import com.example.restaurantfinder.network.YelpApiClient


object RepositoryProvider {


    val yelpRepository: YelpRepository by lazy {
        YelpRepository(YelpApiClient.apiService)
    }
}