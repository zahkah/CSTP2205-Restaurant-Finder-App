package com.example.restaurantfinder.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class LocationHelper(private val context: Context) {
    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }

    // Default location (San Francisco - Yelp HQ)
    companion object {
        val DEFAULT_LOCATION = Location("").apply {
            latitude = 37.786882
            longitude = -122.399972
        }
    }

    /**
     * Get the last known location with a fallback if not available
     * Using FusedLocationProviderClient (Google Play Services)
     */
    @SuppressLint("MissingPermission")
    suspend fun getLastLocationWithFallback(): Location = suspendCancellableCoroutine { continuation ->
        try {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        continuation.resume(location)
                    } else {
                        // If location is null, try to get a fresh update
                        requestLocationUpdateWithFallback(continuation)
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("LocationHelper", "Error getting last location", exception)
                    continuation.resume(DEFAULT_LOCATION)
                }
        } catch (e: Exception) {
            Log.e("LocationHelper", "Exception in getLastLocation", e)
            continuation.resume(DEFAULT_LOCATION)
        }
    }

    /**
     * Get current location using Android's LocationManager
     * This provides an alternative to FusedLocationProviderClient
     */
    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): Location = suspendCancellableCoroutine { continuation ->
        // Check if location permissions are granted first
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d("LocationHelper", "Location permission not granted, using default location")
            continuation.resume(DEFAULT_LOCATION)
            return@suspendCancellableCoroutine
        }

        try {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

            // Check if any provider is enabled
            val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

            if (!isGpsEnabled && !isNetworkEnabled) {
                Log.d("LocationHelper", "No location provider enabled, using default location")
                continuation.resume(DEFAULT_LOCATION)
                return@suspendCancellableCoroutine
            }

            // Try to get the last known location first as a quick response
            val lastLocation = if (isGpsEnabled) {
                locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            } else if (isNetworkEnabled) {
                locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            } else null

            if (lastLocation != null && System.currentTimeMillis() - lastLocation.time < 60000) {
                // If we have a recent location (less than 1 minute old), use it immediately
                Log.d("LocationHelper", "Using recent last known location: ${lastLocation.latitude}, ${lastLocation.longitude}")
                continuation.resume(lastLocation)
                return@suspendCancellableCoroutine
            }

            // We need a fresh location update
            val locationListener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    // Remove the listener to avoid further updates
                    locationManager.removeUpdates(this)
                    Log.d("LocationHelper", "Got fresh location: ${location.latitude}, ${location.longitude}")
                    continuation.resume(location)
                }

                @Deprecated("Deprecated in Java")
                override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}

                override fun onProviderEnabled(provider: String) {}

                override fun onProviderDisabled(provider: String) {
                    // If the provider gets disabled while we're waiting
                    if (!isGpsEnabled && !isNetworkEnabled) {
                        locationManager.removeUpdates(this)
                        Log.d("LocationHelper", "All providers disabled, using default location")
                        continuation.resume(DEFAULT_LOCATION)
                    }
                }
            }

            // Request location updates from best available provider
            val provider = if (isGpsEnabled) LocationManager.GPS_PROVIDER else LocationManager.NETWORK_PROVIDER
            locationManager.requestLocationUpdates(
                provider,
                0L,
                0f,
                locationListener
            )

            // Set a timeout to prevent waiting indefinitely
            Handler(Looper.getMainLooper()).postDelayed({
                if (continuation.isActive) {
                    locationManager.removeUpdates(locationListener)

                    // If we timed out, try one more time to get last location or use default
                    val fallbackLocation = if (isGpsEnabled) {
                        locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    } else if (isNetworkEnabled) {
                        locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                    } else null

                    if (fallbackLocation != null) {
                        Log.d("LocationHelper", "Using fallback location after timeout: ${fallbackLocation.latitude}, ${fallbackLocation.longitude}")
                        continuation.resume(fallbackLocation)
                    } else {
                        Log.d("LocationHelper", "No location available after timeout, using default")
                        continuation.resume(DEFAULT_LOCATION)
                    }
                }
            }, 10000) // 10 second timeout

            // Make sure we clean up if coroutine is cancelled
            continuation.invokeOnCancellation {
                locationManager.removeUpdates(locationListener)
            }

        } catch (e: SecurityException) {
            Log.e("LocationHelper", "Security exception getting location", e)
            continuation.resume(DEFAULT_LOCATION)
        } catch (e: Exception) {
            Log.e("LocationHelper", "Exception getting location", e)
            continuation.resume(DEFAULT_LOCATION)
        }
    }

    /**
     * Request a single location update with FusedLocationProviderClient
     */
    @SuppressLint("MissingPermission")
    private fun requestLocationUpdateWithFallback(continuation: kotlinx.coroutines.CancellableContinuation<Location>) {
        try {
            val locationRequest = LocationRequest.Builder(10000) // 10 seconds
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .build()

            val locationCallback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    fusedLocationClient.removeLocationUpdates(this)
                    val location = result.lastLocation
                    if (location != null) {
                        Log.d("LocationHelper", "Got fresh location from fused client: ${location.latitude}, ${location.longitude}")
                        continuation.resume(location)
                    } else {
                        // If we still can't get a location, use the default
                        Log.d("LocationHelper", "No location from fused client update, using default")
                        continuation.resume(DEFAULT_LOCATION)
                    }
                }
            }

            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )

            continuation.invokeOnCancellation {
                fusedLocationClient.removeLocationUpdates(locationCallback)
            }
        } catch (e: Exception) {
            Log.e("LocationHelper", "Exception in requestLocationUpdate", e)
            continuation.resume(DEFAULT_LOCATION)
        }
    }
}