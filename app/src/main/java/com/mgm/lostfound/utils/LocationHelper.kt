package com.mgm.lostfound.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.mgm.lostfound.data.model.Location as ItemLocation
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import kotlin.coroutines.resume

object LocationHelper {
    suspend fun getCurrentLocation(context: Context): ItemLocation? {
        val fusedLocationClient: FusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(context)

        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return null
        }

        return suspendCancellableCoroutine { continuation ->
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    val geocoder = Geocoder(context, Locale.getDefault())
                    val address = try {
                        val addresses = geocoder.getFromLocation(it.latitude, it.longitude, 1)
                        addresses?.firstOrNull()?.getAddressLine(0) ?: "Lat: ${it.latitude}, Lng: ${it.longitude}"
                    } catch (e: Exception) {
                        "Lat: ${it.latitude}, Lng: ${it.longitude}"
                    }

                    continuation.resume(
                        ItemLocation(
                            latitude = it.latitude,
                            longitude = it.longitude,
                            address = address
                        )
                    )
                } ?: continuation.resume(null)
            }.addOnFailureListener {
                continuation.resume(null)
            }
        }
    }
}

