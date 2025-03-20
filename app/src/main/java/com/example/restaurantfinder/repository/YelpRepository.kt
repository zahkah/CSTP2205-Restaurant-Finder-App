package com.example.restaurantfinder.repository

import com.example.restaurantfinder.data.BusinessDetail
import com.example.restaurantfinder.data.ReviewsResponse
import com.example.restaurantfinder.data.YelpSearchResponse
import com.example.restaurantfinder.network.YelpApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class YelpRepository(private val apiService: YelpApiService) {

    suspend fun searchRestaurants(
        term: String? = "restaurants",
        location: String? = null,
        latitude: Double? = null,
        longitude: Double? = null,
        radius: Int? = null,
        categories: String? = null,
        price: String? = null,
        sortBy: String? = null,
        limit: Int? = 20,
        offset: Int? = 0,
        openNow: Boolean? = null
    ): Result<YelpSearchResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.searchBusinesses(
                term = term,
                location = location,
                latitude = latitude,
                longitude = longitude,
                radius = radius,
                categories = categories,
                price = price,
                sortBy = sortBy,
                limit = limit,
                offset = offset,
                openNow = openNow
            )
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRestaurantDetails(businessId: String): Result<BusinessDetail> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getBusinessDetails(
                id = businessId
            )
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRestaurantReviews(
        businessId: String,
        limit: Int? = 3,
        offset: Int? = 0
    ): Result<ReviewsResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getBusinessReviews(
                id = businessId,
                limit = limit,
                offset = offset
            )
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}