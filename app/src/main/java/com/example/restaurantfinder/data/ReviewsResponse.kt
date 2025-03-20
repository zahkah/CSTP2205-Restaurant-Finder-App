package com.example.restaurantfinder.data

import com.google.gson.annotations.SerializedName

data class ReviewsResponse(
    @SerializedName("reviews") val reviews: List<Review>,
    @SerializedName("total") val total: Int,
    @SerializedName("possible_languages") val possibleLanguages: List<String>
)