package com.example.restaurantfinder.home

import android.content.Context
import android.location.Location
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.restaurantfinder.data.Business
import com.example.restaurantfinder.data.BusinessDetail
import com.example.restaurantfinder.data.Category
import com.example.restaurantfinder.repository.RepositoryProvider
import com.example.restaurantfinder.repository.YelpRepository
import com.example.restaurantfinder.room.AppDatabase
import com.example.restaurantfinder.room.dao.FavoriteRestaurantDao
import com.example.restaurantfinder.room.dao.RestaurantRatingDao
import com.example.restaurantfinder.room.entities.FavoriteRestaurant
import com.example.restaurantfinder.room.entities.UserRestaurantRating
import com.example.restaurantfinder.util.LocationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for restaurant search functionality with location awareness, favorites and ratings
 */
class RestaurantViewModel(
    private val yelpRepository: YelpRepository = RepositoryProvider.yelpRepository
) : ViewModel() {

    // UI states
    private val _uiState = MutableStateFlow(RestaurantUiState())
    val uiState: StateFlow<RestaurantUiState> = _uiState.asStateFlow()

    // Location helper
    private var locationHelper: LocationHelper? = null

    // Room database references
    private var database: AppDatabase? = null
    private var favoriteDao: FavoriteRestaurantDao? = null // Make nullable instead of lateinit
    private var ratingDao: RestaurantRatingDao? = null // Make nullable instead of lateinit


    fun initLocationHelper(context: Context) {
        locationHelper = LocationHelper(context)
    }


    fun initializeApp(context: Context) {
        // Initialize location helper
        locationHelper = LocationHelper(context)

        // Initialize Room database
        database = AppDatabase.getDatabase(context)

        // Initialize DAOs only if database is not null
        database?.let { db ->
            favoriteDao = db.favoriteRestaurantDao()
            ratingDao = db.restaurantRatingDao()

            // Load favorites and ratings
            loadFavorites()
            loadUserRatings()
        } ?: run {
            Log.e("RestaurantViewModel", "Failed to initialize database")
        }
    }


    fun fetchLocationAndSearch(defaultLocation: String = "New York") {
        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            locationHelper?.let { helper ->
                try {
                    // Use a US-based default location that works with Yelp
                    val defaultLoc = Location("default").apply {
                        latitude = 37.786882  // San Francisco coordinates
                        longitude = -122.399972
                    }

                    // Search with the default US location
                    searchWithLocation(defaultLoc)

                    // We're using a default US location because your current location
                    // appears to be in a region with limited Yelp API coverage

                } catch (e: Exception) {
                    Log.e("RestaurantViewModel", "Error in location handling", e)
                    // If any error occurs, use the default city name
                    searchRestaurants(location = defaultLocation)
                }
            } ?: run {
                // If locationHelper is null, use the default location
                searchRestaurants(location = defaultLocation)
            }
        }
    }

//    fun fetchLocationAndSearch(defaultLocation: String = "New York") {
//        _uiState.update { it.copy(isLoading = true, error = null) }
//
//        viewModelScope.launch {
//            locationHelper?.let { helper ->
//                try {
//                    // Get the actual device location using our updated method
//                    val location = helper.getCurrentLocation()
//
//                    // Log the location for debugging
//                    Log.d("RestaurantViewModel", "Location found: ${location.latitude}, ${location.longitude}")
//
//                    // Use the actual location
//                    searchWithLocation(location)
//
//                } catch (e: Exception) {
//                    Log.e("RestaurantViewModel", "Error in location handling", e)
//                    // If any error occurs, use the default city name
//                    searchRestaurants(location = defaultLocation)
//                }
//            } ?: run {
//                // If locationHelper is null, use the default location
//                searchRestaurants(location = defaultLocation)
//            }
//        }
//    }
    // Add to RestaurantViewModel
    fun loadSampleData() {
        // Create a sample dataset of restaurants
        val sampleRestaurants = listOf(
            Business(
                id = "sample-1",
                name = "Sample Restaurant 1",
                rating = 4.5,
                price = "$$$",
                reviewCount = 127,
                location = com.example.restaurantfinder.data.Location(
                    address1 = "123 Main St",
                    address2 = "",
                    address3 = "",
                    city = "San Francisco",
                    zipCode = "94111",
                    country = "US",
                    state = "CA",
                    displayAddress = listOf("123 Main St", "San Francisco, CA 94111")
                ),
                categories = listOf(Category(title = "Italian", alias = "italian")),
                imageUrl = "https://example.com/image1.jpg",
                isClosed = false,
                url = "https://example.com/restaurant1",
                coordinates = com.example.restaurantfinder.data.Coordinates(37.7749, -122.4194),
                transactions = listOf("delivery", "pickup"),
                phone = "+14155551234",
                displayPhone = "(415) 555-1234",
                distance = 1200.0
            ),
            Business(
                id = "sample-2",
                name = "Sample Restaurant 2",
                rating = 4.0,
                price = "$$",
                reviewCount = 98,
                location = com.example.restaurantfinder.data.Location(
                    address1 = "456 Elm St",
                    address2 = "",
                    address3 = "",
                    city = "San Francisco",
                    zipCode = "94112",
                    country = "US",
                    state = "CA",
                    displayAddress = listOf("456 Elm St", "San Francisco, CA 94112")
                ),
                categories = listOf(Category(title = "American", alias = "american")),
                imageUrl = "https://example.com/image2.jpg",
                isClosed = false,
                url = "https://example.com/restaurant2",
                coordinates = com.example.restaurantfinder.data.Coordinates(37.7833, -122.4167),
                transactions = listOf("delivery"),
                phone = "+14155552345",
                displayPhone = "(415) 555-2345",
                distance = 1500.0
            ),
            Business(
                id = "sample-3",
                name = "Sample Restaurant 3",
                rating = 4.8,
                price = "$$$$",
                reviewCount = 214,
                location = com.example.restaurantfinder.data.Location(
                    address1 = "789 Oak St",
                    address2 = "",
                    address3 = "",
                    city = "San Francisco",
                    zipCode = "94109",
                    country = "US",
                    state = "CA",
                    displayAddress = listOf("789 Oak St", "San Francisco, CA 94109")
                ),
                categories = listOf(Category(title = "French", alias = "french")),
                imageUrl = "https://example.com/image3.jpg",
                isClosed = false,
                url = "https://example.com/restaurant3",
                coordinates = com.example.restaurantfinder.data.Coordinates(37.7900, -122.4100),
                transactions = listOf("pickup"),
                phone = "+14155553456",
                displayPhone = "(415) 555-3456",
                distance = 1800.0
            )
        )

        _uiState.update {
            it.copy(
                restaurants = sampleRestaurants,
                totalResults = sampleRestaurants.size,
                isLoading = false,
                error = null
            )
        }
    }


    private fun isLikelyUnsupportedRegion(latitude: Double, longitude: Double): Boolean {
        // Major regions where Yelp has good coverage
        val usCanada = (latitude in 24.0..60.0 && longitude in -130.0..-60.0)
        val europe = (latitude in 35.0..60.0 && longitude in -10.0..40.0)
        val japan = (latitude in 30.0..46.0 && longitude in 129.0..146.0)
        val australia = (latitude in -45.0..-10.0 && longitude in 110.0..155.0)

        // If location is in any of these regions, it's likely supported
        return !(usCanada || europe || japan || australia)
    }


    private fun searchWithLocation(location: Location) {
        val latitude = location.latitude
        val longitude = location.longitude

        // Update UI state with current location
        _uiState.update {
            it.copy(
                searchLatitude = latitude,
                searchLongitude = longitude
            )
        }

        // Search with coordinates
        searchRestaurants(
            latitude = latitude,
            longitude = longitude
        )
    }


    fun searchRestaurants(
        term: String = "restaurants",
        location: String? = null,
        latitude: Double? = null,
        longitude: Double? = null,
        radius: Int? = null,
        categories: String? = null,
        price: String? = null,
        sortBy: String? = null,
        limit: Int? = 20,
        offset: Int? = 0,
        openNow: Boolean? = null
    ) {
        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            yelpRepository.searchRestaurants(
                term = term,
                location = location,
                latitude = latitude,
                longitude = longitude,
                radius = radius,
                categories = categories,
                price = price,
                sortBy = sortBy,
                limit = limit,
                offset = offset,
                openNow = openNow
            ).onSuccess { response ->
                _uiState.update {
                    it.copy(
                        restaurants = response.businesses,
                        totalResults = response.total,
                        isLoading = false
                    )
                }
            }.onFailure { exception ->
                _uiState.update {
                    it.copy(
                        error = exception.message ?: "Unknown error occurred",
                        isLoading = false
                    )
                }
            }
        }
    }


    fun getRestaurantDetails(businessId: String) {
        _uiState.update { it.copy(isLoadingDetails = true, detailsError = null) }

        viewModelScope.launch {
            yelpRepository.getRestaurantDetails(businessId)
                .onSuccess { businessDetail ->
                    _uiState.update {
                        it.copy(
                            selectedRestaurant = businessDetail,
                            isLoadingDetails = false
                        )
                    }
                }.onFailure { exception ->
                    _uiState.update {
                        it.copy(
                            detailsError = exception.message ?: "Unknown error occurred",
                            isLoadingDetails = false
                        )
                    }
                }
        }
    }


    fun getRestaurantReviews(businessId: String, limit: Int? = 3) {
        _uiState.update { it.copy(isLoadingReviews = true, reviewsError = null) }

        viewModelScope.launch {
            yelpRepository.getRestaurantReviews(businessId, limit)
                .onSuccess { reviewsResponse ->
                    _uiState.update {
                        it.copy(
                            reviews = reviewsResponse.reviews,
                            isLoadingReviews = false
                        )
                    }
                }.onFailure { exception ->
                    _uiState.update {
                        it.copy(
                            reviewsError = exception.message ?: "Unknown error occurred",
                            isLoadingReviews = false
                        )
                    }
                }
        }
    }


    fun sortRestaurants(sortBy: SortCriteria) {
        val currentRestaurants = _uiState.value.restaurants.toMutableList()

        val sortedList = when (sortBy) {
            SortCriteria.RATING -> currentRestaurants.sortedByDescending { it.rating }
            SortCriteria.REVIEW_COUNT -> currentRestaurants.sortedByDescending { it.reviewCount }
            SortCriteria.DISTANCE -> currentRestaurants.sortedBy { it.distance }
            SortCriteria.PRICE_LOW_TO_HIGH -> currentRestaurants.sortedBy {
                it.price?.length ?: 0
            }
            SortCriteria.PRICE_HIGH_TO_LOW -> currentRestaurants.sortedByDescending {
                it.price?.length ?: 0
            }
        }

        _uiState.update { it.copy(restaurants = sortedList) }
    }


    fun filterByPrice(priceFilters: List<String>) {
        if (priceFilters.isEmpty()) {
            // If no filters selected, perform a new search without price filter
            val currentState = _uiState.value
            searchRestaurants(
                term = currentState.searchTerm,
                location = currentState.searchLocation,
                latitude = currentState.searchLatitude,
                longitude = currentState.searchLongitude,
                radius = currentState.searchRadius,
                sortBy = currentState.searchSortBy
            )
            return
        }

        val priceParam = priceFilters.joinToString(",")
        val currentState = _uiState.value

        searchRestaurants(
            term = currentState.searchTerm,
            location = currentState.searchLocation,
            latitude = currentState.searchLatitude,
            longitude = currentState.searchLongitude,
            radius = currentState.searchRadius,
            price = priceParam,
            sortBy = currentState.searchSortBy
        )
    }


    fun updateSearchParams(
        term: String? = null,
        location: String? = null,
        latitude: Double? = null,
        longitude: Double? = null,
        radius: Int? = null,
        sortBy: String? = null
    ) {
        _uiState.update {
            it.copy(
                searchTerm = term ?: it.searchTerm,
                searchLocation = location ?: it.searchLocation,
                searchLatitude = latitude ?: it.searchLatitude,
                searchLongitude = longitude ?: it.searchLongitude,
                searchRadius = radius ?: it.searchRadius,
                searchSortBy = sortBy ?: it.searchSortBy
            )
        }
    }


    fun loadFavorites() {
        viewModelScope.launch {
            try {
                favoriteDao?.let { dao ->
                    val favorites = dao.getAllFavorites()
                    _uiState.update { it.copy(favorites = favorites) }
                }
            } catch (e: Exception) {
                Log.e("RestaurantViewModel", "Error loading favorites", e)
            }
        }
    }


    fun addToFavorites(restaurant: BusinessDetail) {
        viewModelScope.launch {
            try {
                // Check if favoriteDao is initialized
                favoriteDao?.let { dao ->
                    val favorite = FavoriteRestaurant(
                        id = restaurant.id,
                        name = restaurant.name,
                        imageUrl = restaurant.imageUrl,
                        rating = restaurant.rating,
                        price = restaurant.price ?: "",
                        address = restaurant.location?.address1 ?: "",
                        categories = restaurant.categories.joinToString(", ") { it.title },
                        latitude = restaurant.coordinates?.latitude,
                        longitude = restaurant.coordinates?.longitude,
                        phone = restaurant.phone,
                        reviewCount = restaurant.reviewCount,
                        timestamp = System.currentTimeMillis()
                    )
                    dao.insert(favorite)

                    // Refresh favorites list
                    loadFavorites()
                } ?: run {
                    Log.e("RestaurantViewModel", "Cannot add to favorites: Database not initialized")
                    // Optionally show a message to the user that DB is not initialized
                    _uiState.update {
                        it.copy(error = "Database not initialized. Please restart the app.")
                    }
                }
            } catch (e: Exception) {
                Log.e("RestaurantViewModel", "Error adding to favorites", e)
            }
        }
    }


    fun addBusinessToFavorites(business: Business) {
        viewModelScope.launch {
            try {
                // Check if favoriteDao is initialized
                favoriteDao?.let { dao ->
                    val favorite = FavoriteRestaurant(
                        id = business.id,
                        name = business.name,
                        imageUrl = business.imageUrl,
                        rating = business.rating,
                        price = business.price ?: "",
                        address = business.location.address1 ?: "",
                        categories = business.categories.joinToString(", ") { it.title },
                        latitude = business.coordinates?.latitude,
                        longitude = business.coordinates?.longitude,
                        phone = business.phone,
                        reviewCount = business.reviewCount,
                        timestamp = System.currentTimeMillis()
                    )

                    dao.insert(favorite)

                    // Refresh favorites list
                    loadFavorites()
                } ?: run {
                    Log.e("RestaurantViewModel", "Cannot add to favorites: Database not initialized")
                    // Optionally show a message to the user that DB is not initialized
                    _uiState.update {
                        it.copy(error = "Database not initialized. Please restart the app.")
                    }
                }
            } catch (e: Exception) {
                Log.e("RestaurantViewModel", "Error adding business to favorites", e)
            }
        }
    }

    /**
     * Remove a restaurant from favorites
     */
    fun removeFromFavorites(restaurantId: String) {
        viewModelScope.launch {
            try {
                favoriteDao?.let { dao ->
                    dao.delete(restaurantId)

                    // Refresh favorites list
                    loadFavorites()
                } ?: run {
                    Log.e("RestaurantViewModel", "Cannot remove from favorites: Database not initialized")
                }
            } catch (e: Exception) {
                Log.e("RestaurantViewModel", "Error removing from favorites", e)
            }
        }
    }

    /**
     * Check if a restaurant is in favorites
     */
    fun isRestaurantFavorite(restaurantId: String): Boolean {
        return _uiState.value.favorites.any { it.id == restaurantId }
    }

    // RATING METHODS

    /**
     * Load all user ratings from database
     */
    private fun loadUserRatings() {
        viewModelScope.launch {
            try {
                ratingDao?.let { dao ->
                    val ratings = dao.getAllRatings()
                    val ratingsMap = ratings.associate { it.restaurantId to it.rating }
                    _uiState.update { it.copy(userRatings = ratingsMap) }
                }
            } catch (e: Exception) {
                Log.e("RestaurantViewModel", "Error loading ratings", e)
            }
        }
    }

    /**
     * Rate a restaurant
     */
    fun rateRestaurant(restaurantId: String, rating: Double) {
        viewModelScope.launch {
            try {
                ratingDao?.let { dao ->
                    val userRating = UserRestaurantRating(
                        restaurantId = restaurantId,
                        rating = rating,
                        timestamp = System.currentTimeMillis()
                    )
                    dao.insertOrUpdate(userRating)

                    // Refresh ratings
                    loadUserRatings()
                } ?: run {
                    Log.e("RestaurantViewModel", "Cannot rate restaurant: Database not initialized")
                }
            } catch (e: Exception) {
                Log.e("RestaurantViewModel", "Error rating restaurant", e)
            }
        }
    }

    /**
     * Get user rating for a restaurant
     */
    fun getUserRating(restaurantId: String): Double? {
        return _uiState.value.userRatings[restaurantId]
    }

    /**
     * Check if the database is initialized
     */
    fun isDatabaseInitialized(): Boolean {
        return database != null && favoriteDao != null && ratingDao != null
    }
}

/**
 * UI state for restaurant screens
 */
data class RestaurantUiState(
    // Search results
    val restaurants: List<Business> = emptyList(),
    val totalResults: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null,

    // Restaurant details
    val selectedRestaurant: Any? = null,  // Using Any to avoid import issues, should be BusinessDetail
    val isLoadingDetails: Boolean = false,
    val detailsError: String? = null,

    // Reviews
    val reviews: List<Any> = emptyList(),  // Using Any to avoid import issues, should be List<Review>
    val isLoadingReviews: Boolean = false,
    val reviewsError: String? = null,

    // Search parameters
    val searchTerm: String = "restaurants",
    val searchLocation: String? = null,
    val searchLatitude: Double? = null,
    val searchLongitude: Double? = null,
    val searchRadius: Int? = null,
    val searchSortBy: String? = "best_match",

    // Favorites and ratings
    val favorites: List<FavoriteRestaurant> = emptyList(),
    val userRatings: Map<String, Double> = emptyMap()
)

/**
 * Sort criteria for restaurant list
 */
enum class SortCriteria {
    RATING,
    REVIEW_COUNT,
    DISTANCE,
    PRICE_LOW_TO_HIGH,
    PRICE_HIGH_TO_LOW
}