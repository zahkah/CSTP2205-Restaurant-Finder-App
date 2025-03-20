package com.example.restaurantfinder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.example.restaurantfinder.home.RestaurantViewModel
import com.example.restaurantfinder.navigation.AppNavigation
import com.example.restaurantfinder.ui.theme.RestaurantFinderTheme
import com.example.restaurantfinder.util.YelpCredentialsProvider

class MainActivity : ComponentActivity() {

    // Single ViewModel instance at the activity level
    private lateinit var restaurantViewModel: RestaurantViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Yelp API credentials
        YelpCredentialsProvider.initialize()

        // Create and initialize ViewModel
        restaurantViewModel = ViewModelProvider(this)[RestaurantViewModel::class.java]

        // Use application context for database initialization
        restaurantViewModel.initializeApp(applicationContext)

        setContent {
            RestaurantFinderTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Pass the existing viewModel instead of creating new ones
                    AppNavigation(viewModel = restaurantViewModel)
                }
            }
        }
    }
}