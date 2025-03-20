package com.example.restaurantfinder.room.entities


import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_restaurant_ratings")
data class UserRestaurantRating(
    @PrimaryKey
    val restaurantId: String,
    val rating: Double,
    val timestamp: Long
)