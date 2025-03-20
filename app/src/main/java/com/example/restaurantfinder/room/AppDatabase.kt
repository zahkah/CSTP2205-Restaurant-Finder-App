package com.example.restaurantfinder.room


import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.restaurantfinder.room.dao.FavoriteRestaurantDao
import com.example.restaurantfinder.room.dao.RestaurantRatingDao
import com.example.restaurantfinder.room.entities.FavoriteRestaurant
import com.example.restaurantfinder.room.entities.UserRestaurantRating

@Database(
    entities = [FavoriteRestaurant::class, UserRestaurantRating::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favoriteRestaurantDao(): FavoriteRestaurantDao
    abstract fun restaurantRatingDao(): RestaurantRatingDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "restaurant_finder_database"
                ).fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}