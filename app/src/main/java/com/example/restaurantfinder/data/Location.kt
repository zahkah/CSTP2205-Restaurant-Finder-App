package com.example.restaurantfinder.data

import com.google.gson.annotations.SerializedName

data class Location(
    @SerializedName("address1") val address1: String,
    @SerializedName("address2") val address2: String?,
    @SerializedName("address3") val address3: String?,
    @SerializedName("city") val city: String,
    @SerializedName("zip_code") val zipCode: String,
    @SerializedName("country") val country: String,
    @SerializedName("state") val state: String,
    @SerializedName("display_address") val displayAddress: List<String>
)