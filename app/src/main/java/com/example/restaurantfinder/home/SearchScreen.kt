package com.example.restaurantfinder.home

import android.Manifest
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.restaurantfinder.data.Business
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SearchScreen(
    onRestaurantClick: (String) -> Unit,
    viewModel: RestaurantViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var showFilters by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Remember the permission state
    val locationPermissionState = rememberPermissionState(
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    // Initialize location helper
    LaunchedEffect(Unit) {
        viewModel.initLocationHelper(context)
    }

    // Content based on permission state
    val permissionGranted = locationPermissionState.status.isGranted

    LaunchedEffect(permissionGranted) {
        if (permissionGranted) {
            // Permission granted, fetch location and search
            viewModel.fetchLocationAndSearch()
        } else {
            // Use a default location as fallback
            viewModel.searchRestaurants(location = "New York")
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Permission request banner (if needed)
        if (!permissionGranted) {
            LocationPermissionBanner(
                shouldShowRationale = locationPermissionState.status.shouldShowRationale,
                onRequestPermission = { locationPermissionState.launchPermissionRequest() }
            )
        }

        // Search bar
        SearchBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            onSearch = {
                viewModel.updateSearchParams(term = it)

                // Use current location if permission granted, otherwise use the query as location
                if (permissionGranted) {
                    viewModel.searchRestaurants(term = it)
                } else {
                    viewModel.searchRestaurants(term = it, location = it)
                }
            },
            onFilterClick = { showFilters = !showFilters },
            onLocationClick = { viewModel.fetchLocationAndSearch() },
            locationEnabled = permissionGranted
        )

        // Filters
        if (showFilters) {
            FilterOptions(
                onPriceFilterSelected = { viewModel.filterByPrice(it) },
                onSortCriteriaSelected = { viewModel.sortRestaurants(it) }
            )
        }

        // Results
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.error != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Error: ${uiState.error}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "We're having trouble with this location. Try searching for a US city instead.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = {
                        // Use San Francisco as a fallback location
                        viewModel.searchRestaurants(location = "San Francisco")
                    }) {
                        Text("Try San Francisco")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(onClick = {
                        // Show a local sample dataset instead
                        viewModel.loadSampleData()
                    }) {
                        Text("Use Sample Data")
                    }
                }
            }
        }
         else {
            RestaurantList(
                restaurants = uiState.restaurants,
                onRestaurantClick = onRestaurantClick
            )
        }
    }

}

@Composable
fun LocationPermissionBanner(
    shouldShowRationale: Boolean,
    onRequestPermission: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (shouldShowRationale) {
                    "Location permission is needed to find restaurants near you."
                } else {
                    "Allow location access for better restaurant recommendations."
                },
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onRequestPermission,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Grant Permission")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    onFilterClick: () -> Unit,
    onLocationClick: () -> Unit,
    locationEnabled: Boolean
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shadowElevation = 4.dp,
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            TextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Search restaurants...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                },
                singleLine = true,
                keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                    onDone = { onSearch(query) }
                ),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    imeAction = androidx.compose.ui.text.input.ImeAction.Search
                )
            )

            // Location button
            IconButton(
                onClick = onLocationClick,
                enabled = locationEnabled
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Use My Location",
                    tint = if (locationEnabled) MaterialTheme.colorScheme.primary
                    else Color.Gray.copy(alpha = 0.5f)
                )
            }

            // Filter button
            TextButton(onClick = onFilterClick) {
                Text("Filter")
            }
        }
    }
}

// Rest of your components remain the same
@Composable
fun FilterOptions(
    onPriceFilterSelected: (List<String>) -> Unit,
    onSortCriteriaSelected: (SortCriteria) -> Unit
) {
    // Your existing code
    var selectedPriceFilters by remember { mutableStateOf(listOf<String>()) }
    var selectedSortCriteria by remember { mutableStateOf<SortCriteria?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            "Price Filter",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedPriceFilters.contains("1"),
                onClick = {
                    selectedPriceFilters = if (selectedPriceFilters.contains("1")) {
                        selectedPriceFilters - "1"
                    } else {
                        selectedPriceFilters + "1"
                    }
                    onPriceFilterSelected(selectedPriceFilters)
                },
                label = { Text("$") }
            )

            FilterChip(
                selected = selectedPriceFilters.contains("2"),
                onClick = {
                    selectedPriceFilters = if (selectedPriceFilters.contains("2")) {
                        selectedPriceFilters - "2"
                    } else {
                        selectedPriceFilters + "2"
                    }
                    onPriceFilterSelected(selectedPriceFilters)
                },
                label = { Text("$$") }
            )

            FilterChip(
                selected = selectedPriceFilters.contains("3"),
                onClick = {
                    selectedPriceFilters = if (selectedPriceFilters.contains("3")) {
                        selectedPriceFilters - "3"
                    } else {
                        selectedPriceFilters + "3"
                    }
                    onPriceFilterSelected(selectedPriceFilters)
                },
                label = { Text("$$$") }
            )

            FilterChip(
                selected = selectedPriceFilters.contains("4"),
                onClick = {
                    selectedPriceFilters = if (selectedPriceFilters.contains("4")) {
                        selectedPriceFilters - "4"
                    } else {
                        selectedPriceFilters + "4"
                    }
                    onPriceFilterSelected(selectedPriceFilters)
                },
                label = { Text("$$$$") }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "Sort By",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Column {
            SortCriteria.values().forEach { criteria ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selectedSortCriteria = criteria
                            onSortCriteriaSelected(criteria)
                        }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedSortCriteria == criteria,
                        onClick = {
                            selectedSortCriteria = criteria
                            onSortCriteriaSelected(criteria)
                        }
                    )
                    Text(
                        text = when (criteria) {
                            SortCriteria.RATING -> "Rating (High to Low)"
                            SortCriteria.REVIEW_COUNT -> "Review Count"
                            SortCriteria.DISTANCE -> "Distance"
                            SortCriteria.PRICE_LOW_TO_HIGH -> "Price (Low to High)"
                            SortCriteria.PRICE_HIGH_TO_LOW -> "Price (High to Low)"
                        },
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun RestaurantList(
    restaurants: List<Business>,
    onRestaurantClick: (String) -> Unit
) {
    if (restaurants.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("No restaurants found")
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(restaurants) { restaurant ->
                RestaurantItem(
                    restaurant = restaurant,
                    onClick = { onRestaurantClick(restaurant.id) }
                )
            }
        }
    }
}

@Composable
fun RestaurantItem(
    restaurant: Business,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = restaurant.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = restaurant.price ?: "",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                RatingBar(rating = restaurant.rating.toFloat())  // Convert Double to Float

                Text(
                    text = "(${restaurant.reviewCount})",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = restaurant.location?.address1 ?: "",
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Categories
            restaurant.categories?.let { categories ->
                Text(
                    text = categories.joinToString(", ") { it.title },
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun RatingBar(rating: Float) {
    Row {
        repeat(5) { index ->
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = if (index < rating) Color(0xFFFFC107) else Color.Gray.copy(alpha = 0.5f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}