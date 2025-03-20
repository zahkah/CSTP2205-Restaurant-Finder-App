package com.example.restaurantfinder.data

import com.google.gson.annotations.SerializedName

data class Hours(
    @SerializedName("open") val open: List<OpenHours>,
    @SerializedName("hours_type") val hoursType: String,
    @SerializedName("is_open_now") val isOpenNow: Boolean
)