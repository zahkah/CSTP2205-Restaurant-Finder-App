package com.example.restaurantfinder.home


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.restaurantfinder.rating.InteractiveRatingBar

import com.example.restaurantfinder.data.BusinessDetail

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantDetailScreen(
    businessId: String,
    onBackClick: () -> Unit,
    viewModel: RestaurantViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val restaurant = uiState.selectedRestaurant as? BusinessDetail
    val isFavorite = remember(businessId, uiState.favorites) {
        viewModel.isRestaurantFavorite(businessId)
    }

    // Request restaurant details and reviews when the screen is shown
    LaunchedEffect(businessId) {
        viewModel.getRestaurantDetails(businessId)
        viewModel.getRestaurantReviews(businessId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val restaurantName = when (val rest = uiState.selectedRestaurant) {
                        is BusinessDetail -> rest.name
                        else -> "Restaurant Details"
                    }
                    Text(restaurantName)
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Favorite button with toggle functionality
                    IconButton(
                        onClick = {
                            restaurant?.let {
                                if (isFavorite) {
                                    viewModel.removeFromFavorites(businessId)
                                } else {
                                    viewModel.addToFavorites(it)
                                }
                            }
                        },
                        enabled = restaurant != null
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                            tint = if (isFavorite) Color.Red else LocalContentColor.current
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoadingDetails -> {
                    // Loading state
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                uiState.detailsError != null -> {
                    // Error state
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Error: ${uiState.detailsError}",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { viewModel.getRestaurantDetails(businessId) }) {
                                Text("Retry")
                            }
                        }
                    }
                }
                uiState.selectedRestaurant != null -> {
                    // Content
                    val restaurant = uiState.selectedRestaurant as? BusinessDetail
                    restaurant?.let {
                        RestaurantDetailContent(
                            restaurant = it,
                            reviews = uiState.reviews,
                            isLoadingReviews = uiState.isLoadingReviews,
                            viewModel = viewModel
                        )
                    }
                }
                else -> {
                    // Empty state
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No restaurant details available")
                    }
                }
            }
        }
    }
}

@Composable
fun RestaurantDetailContent(
    restaurant: BusinessDetail,
    reviews: List<Any>,
    isLoadingReviews: Boolean,
    viewModel: RestaurantViewModel

) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Main image
        if (restaurant.photos.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                AsyncImage(
                    model = restaurant.photos[0],
                    contentDescription = "Restaurant Photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Restaurant name and basic info
        Text(
            text = restaurant.name,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Categories and price
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = restaurant.categories.joinToString(", ") { it.title },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = restaurant.price ?: "",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Rating
        Row(verticalAlignment = Alignment.CenterVertically) {
            RatingBar(rating = restaurant.rating.toFloat())

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "${restaurant.rating} (${restaurant.reviewCount} reviews)",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Divider(modifier = Modifier.padding(vertical = 16.dp))

        // Hours section
        restaurant.hours?.firstOrNull()?.let { hoursInfo ->
            Text(
                text = "Hours",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            val daysOfWeek = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

            hoursInfo.open.forEach { openHours ->
                val dayName = daysOfWeek.getOrNull(openHours.day) ?: "Unknown"
                Text(
                    text = "$dayName: ${formatTime(openHours.start)} - ${formatTime(openHours.end)}",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(4.dp))
            }

            Text(
                text = if (restaurant.hours.firstOrNull()?.isOpenNow == true) {
                    "Open now"
                } else {
                    "Closed now"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = if (restaurant.hours.firstOrNull()?.isOpenNow == true) {
                    Color.Green
                } else {
                    Color.Red
                }
            )

            Divider(modifier = Modifier.padding(vertical = 16.dp))
        }

        // Location section
        Text(
            text = "Location",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        restaurant.location?.let { location ->
            Text(
                text = location.address1,
                style = MaterialTheme.typography.bodyMedium
            )

            if (!location.address2.isNullOrBlank()) {
                Text(
                    text = location.address2,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (!location.address3.isNullOrBlank()) {
                Text(
                    text = location.address3,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Text(
                text = "${location.city}, ${location.state} ${location.zipCode}",
                style = MaterialTheme.typography.bodyMedium
            )

            // Map placeholder
            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                // In a real app, this would be a Google Map
                Text(text = "Map View")
            }
        }

        Divider(modifier = Modifier.padding(vertical = 16.dp))

        // Contact section
        Text(
            text = "Contact",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.Phone,
                contentDescription = "Phone",
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = restaurant.phone ?: "No phone available",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        if (!restaurant.url.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Public,  // Changed from Language to Public
                    contentDescription = "Website",
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Visit Website",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Divider(modifier = Modifier.padding(vertical = 16.dp))

        // User Rating section
        Text(
            text = "Your Rating",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

// Get the current user rating if available
        val userRating = remember(restaurant.id) {
            viewModel.getUserRating(restaurant.id)?.toFloat() ?: 0f
        }

// State to hold the current user rating
        var currentUserRating by remember { mutableStateOf(userRating) }

// Display the interactive rating bar
        InteractiveRatingBar(
            currentRating = currentUserRating,
            onRatingChanged = { newRating ->
                currentUserRating = newRating
                viewModel.rateRestaurant(restaurant.id, newRating.toDouble())
            }
        )

        if (currentUserRating > 0) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Thanks for rating!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Reviews section
        Text(
            text = "Reviews",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (isLoadingReviews) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (reviews.isEmpty()) {
            Text(
                text = "No reviews available",
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            reviews.forEach { reviewObj ->
                // Cast the review to the correct type
                val review = reviewObj as com.example.restaurantfinder.data.Review
                ReviewItem(review)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun ReviewItem(review: com.example.restaurantfinder.data.Review) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // User image
                    review.user.imageUrl?.let { imageUrl ->
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = "User profile",
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    Text(
                        text = review.user.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                RatingBar(rating = review.rating.toFloat())
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = review.text,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = review.timeCreated.split(" ")[0], // Just show the date part
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Helper function to format time from 24-hour format (e.g., "1100") to 12-hour format (e.g., "11:00 AM")
private fun formatTime(time: String): String {
    if (time.length != 4) return time

    val hour = time.substring(0, 2).toInt()
    val minute = time.substring(2, 4)

    val hourIn12Format = when (hour) {
        0 -> 12
        in 13..23 -> hour - 12
        else -> hour
    }

    val amPm = if (hour < 12) "AM" else "PM"

    return "$hourIn12Format:$minute $amPm"
}