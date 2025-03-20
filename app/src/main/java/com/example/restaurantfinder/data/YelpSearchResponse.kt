package com.example.restaurantfinder.data

import com.google.gson.annotations.SerializedName

data class YelpSearchResponse(
    @SerializedName("businesses") val businesses: List<Business>,
    @SerializedName("total") val total: Int,
    @SerializedName("region") val region: Region
)