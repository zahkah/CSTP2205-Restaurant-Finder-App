package com.example.restaurantfinder.network

import com.example.restaurantfinder.data.BusinessDetail
import com.example.restaurantfinder.data.ReviewsResponse
import com.example.restaurantfinder.data.YelpSearchResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface YelpApiService {

    @GET("businesses/search")
    suspend fun searchBusinesses(
        @Query("term") term: String? = null,
        @Query("location") location: String? = null,
        @Query("latitude") latitude: Double? = null,
        @Query("longitude") longitude: Double? = null,
        @Query("radius") radius: Int? = null,
        @Query("categories") categories: String? = null,
        @Query("locale") locale: String? = null,
        @Query("limit") limit: Int? = null,
        @Query("offset") offset: Int? = null,
        @Query("sort_by") sortBy: String? = null,
        @Query("price") price: String? = null,
        @Query("open_now") openNow: Boolean? = null,
        @Query("open_at") openAt: Int? = null,
        @Query("attributes") attributes: String? = null
    ): YelpSearchResponse

    @GET("businesses/{id}")
    suspend fun getBusinessDetails(
        @Path("id") id: String,
        @Query("locale") locale: String? = null
    ): BusinessDetail

    @GET("businesses/{id}/reviews")
    suspend fun getBusinessReviews(
        @Path("id") id: String,
        @Query("locale") locale: String? = null,
        @Query("limit") limit: Int? = null,
        @Query("offset") offset: Int? = null
    ): ReviewsResponse
}