package com.example.restaurantfinder.data

import com.google.gson.annotations.SerializedName

data class Review(
    @SerializedName("id") val id: String,
    @SerializedName("rating") val rating: Int,
    @SerializedName("user") val user: User,
    @SerializedName("text") val text: String,
    @SerializedName("time_created") val timeCreated: String,
    @SerializedName("url") val url: String
)