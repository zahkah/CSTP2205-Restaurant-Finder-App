package com.example.restaurantfinder.data

import com.google.gson.annotations.SerializedName

data class OpenHours(
    @SerializedName("is_overnight") val isOvernight: Boolean,
    @SerializedName("start") val start: String,  // In 24-hour format like "0800"
    @SerializedName("end") val end: String,      // In 24-hour format like "2200"
    @SerializedName("day") val day: Int          // 0 = Monday, 6 = Sunday
)