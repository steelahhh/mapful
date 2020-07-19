package dev.steelahhh.mapful.data

import android.location.Address
import android.location.Geocoder
import com.google.android.gms.maps.model.LatLng

data class User(
    val id: Int,
    val name: String,
    val latLng: LatLng,
    val avatar: String
)

fun User.getAddress(geocoder: Geocoder): String {
    return try {
        val addresses: List<Address> = geocoder.getFromLocation(
            latLng.latitude,
            latLng.longitude,
            1
        )
        if (addresses.isNotEmpty()) {
            addresses.first().getAddressLine(0)
        } else {
            ""
        }
    } catch (e: Exception) {
        ""
    }
}
