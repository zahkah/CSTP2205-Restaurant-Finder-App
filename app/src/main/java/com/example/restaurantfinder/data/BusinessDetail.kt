package com.example.restaurantfinder.data

import com.google.gson.annotations.SerializedName

data class BusinessDetail(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("is_claimed") val isClaimed: Boolean,
    @SerializedName("is_closed") val isClosed: Boolean,
    @SerializedName("url") val url: String,
    @SerializedName("phone") val phone: String,
    @SerializedName("display_phone") val displayPhone: String,
    @SerializedName("review_count") val reviewCount: Int,
    @SerializedName("categories") val categories: List<Category>,
    @SerializedName("rating") val rating: Double,
    @SerializedName("location") val location: Location,
    @SerializedName("coordinates") val coordinates: Coordinates,
    @SerializedName("photos") val photos: List<String>,
    @SerializedName("price") val price: String?,
    @SerializedName("hours") val hours: List<Hours>?,
    @SerializedName("transactions") val transactions: List<String>
)