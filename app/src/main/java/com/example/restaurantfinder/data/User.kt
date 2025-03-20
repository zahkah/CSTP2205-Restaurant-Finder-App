package com.example.restaurantfinder.data

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("id") val id: String,
    @SerializedName("profile_url") val profileUrl: String,
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("name") val name: String
)