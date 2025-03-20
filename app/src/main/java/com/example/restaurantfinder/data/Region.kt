package com.example.restaurantfinder.data

import com.google.gson.annotations.SerializedName

data class Region(
    @SerializedName("center") val center: Coordinates
)