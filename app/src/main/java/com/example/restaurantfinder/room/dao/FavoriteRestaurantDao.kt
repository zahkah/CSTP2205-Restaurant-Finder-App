package com.example.restaurantfinder.room.dao


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.restaurantfinder.room.entities.FavoriteRestaurant

@Dao
interface FavoriteRestaurantDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(favoriteRestaurant: FavoriteRestaurant)

    @Query("DELETE FROM favorite_restaurants WHERE id = :restaurantId")
    suspend fun delete(restaurantId: String)

    @Query("SELECT * FROM favorite_restaurants ORDER BY timestamp DESC")
    suspend fun getAllFavorites(): List<FavoriteRestaurant>

    @Query("SELECT * FROM favorite_restaurants WHERE id = :restaurantId LIMIT 1")
    suspend fun getFavoriteById(restaurantId: String): FavoriteRestaurant?

    @Query("SELECT COUNT(*) FROM favorite_restaurants WHERE id = :restaurantId")
    suspend fun isRestaurantFavorite(restaurantId: String): Int
}