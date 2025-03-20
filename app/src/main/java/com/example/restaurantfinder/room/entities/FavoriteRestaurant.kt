package com.example.restaurantfinder.room.entities


import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_restaurants")
data class FavoriteRestaurant(
    @PrimaryKey
    val id: String,
    val name: String,
    val imageUrl: String? = null,
    val rating: Double = 0.0,
    val price: String? = null,
    val address: String,
    val categories: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val phone: String? = null,
    val reviewCount: Int = 0,
    val timestamp: Long = 0
)