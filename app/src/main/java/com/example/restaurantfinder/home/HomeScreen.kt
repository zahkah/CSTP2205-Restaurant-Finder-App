package com.example.restaurantfinder.home

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.restaurantfinder.auth.AuthViewModel
import com.example.restaurantfinder.auth.AuthState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToLogin: () -> Unit = {}, // Added navigation callback
    authViewModel: AuthViewModel = viewModel(),
    restaurantViewModel: RestaurantViewModel = viewModel()
) {
    val authState by authViewModel.authState.collectAsState()
    val navController = rememberNavController()
    var selectedTab by remember { mutableStateOf(0) }

    // Add this effect to observe auth state changes
    LaunchedEffect(authState) {
        if (authState is AuthState.Unauthenticated) {
            onNavigateToLogin()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Restaurant Finder") },
                actions = {
                    IconButton(onClick = { authViewModel.signOut() }) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Sign Out"
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = {
                        selectedTab = 0
                        navController.navigate("search") {
                            // Pop up to the start destination of the graph to avoid building up a large stack
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            // Avoid duplicate destinations
                            launchSingleTop = true
                            // Restore state when reselecting a previously selected item
                            restoreState = true
                        }
                    },
                    icon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    label = { Text("Search") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = {
                        selectedTab = 1
                        navController.navigate("favorites") {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(Icons.Default.Favorite, contentDescription = "Favorites") },
                    label = { Text("Favorites") }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            HomeNavigation(
                navController = navController,
                viewModel = restaurantViewModel
            )
        }
    }
}

@Composable
fun HomeNavigation(
    navController: androidx.navigation.NavHostController,
    viewModel: RestaurantViewModel
) {
    NavHost(navController = navController, startDestination = "search") {
        composable("search") {
            SearchScreen(
                onRestaurantClick = { businessId ->
                    navController.navigate("details/$businessId")
                },
                viewModel = viewModel
            )
        }
        composable("favorites") {
            FavoritesScreen(
                onRestaurantClick = { businessId ->
                    navController.navigate("details/$businessId")
                },
                viewModel = viewModel
            )
        }
        composable("details/{businessId}") { backStackEntry ->
            val businessId = backStackEntry.arguments?.getString("businessId") ?: ""
            RestaurantDetailScreen(
                businessId = businessId,
                onBackClick = { navController.popBackStack() },
                viewModel = viewModel
            )
        }
    }
}
// Preview function remains outside of HomeScreen
@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen()
}