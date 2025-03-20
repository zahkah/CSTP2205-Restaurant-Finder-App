package com.example.restaurantfinder.room.dao


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.restaurantfinder.room.entities.UserRestaurantRating

@Dao
interface RestaurantRatingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(rating: UserRestaurantRating)

    @Query("SELECT * FROM user_restaurant_ratings")
    suspend fun getAllRatings(): List<UserRestaurantRating>

    @Query("SELECT * FROM user_restaurant_ratings WHERE restaurantId = :restaurantId")
    suspend fun getRatingForRestaurant(restaurantId: String): UserRestaurantRating?
}