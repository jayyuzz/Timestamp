package com.example.util

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

data class LocationInfo(
  val latitude: Double = 0.7303,
  val longitude: Double = 124.3168,
  val addressText: String = "SMK Negeri 3 Kotamobagu, Jl. AKD, Mongkonai, Kotamobagu Barat, Sulawesi Utara"
)

object LocationHelper {

  @SuppressLint("MissingPermission")
  fun getLastKnownLocation(context: Context): Location? {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
      ?: return null

    var bestLocation: Location? = null
    try {
      val providers = locationManager.getProviders(true)
      for (provider in providers) {
        val l = locationManager.getLastKnownLocation(provider) ?: continue
        if (bestLocation == null || l.accuracy < bestLocation.accuracy) {
          bestLocation = l
        }
      }
    } catch (_: Exception) {
      // Ignored
    }
    return bestLocation
  }

  suspend fun resolveLocationInfo(context: Context): LocationInfo = withContext(Dispatchers.IO) {
    val location = getLastKnownLocation(context)
    if (location != null) {
      val lat = location.latitude
      val lng = location.longitude
      val address = reverseGeocode(context, lat, lng)
      LocationInfo(
        latitude = lat,
        longitude = lng,
        addressText = address
      )
    } else {
      // Default official coordinate of SMKN 3 Kotamobagu
      LocationInfo()
    }
  }

  @Suppress("DEPRECATION")
  private fun reverseGeocode(context: Context, lat: Double, lng: Double): String {
    return try {
      val geocoder = Geocoder(context, Locale("id", "ID"))
      val list = geocoder.getFromLocation(lat, lng, 1)
      if (!list.isNullOrEmpty()) {
        val address = list[0]
        val thoroughfare = address.thoroughfare ?: address.subLocality ?: "Jl. AKD"
        val locality = address.locality ?: address.subAdminArea ?: "Kotamobagu"
        val adminArea = address.adminArea ?: "Sulawesi Utara"
        "$thoroughfare, $locality, $adminArea"
      } else {
        "Kotamobagu Barat, Kota Kotamobagu, Sulawesi Utara"
      }
    } catch (_: Exception) {
      "Kota Kotamobagu, Sulawesi Utara (${String.format(Locale.US, "%.4f, %.4f", lat, lng)})"
    }
  }
}
