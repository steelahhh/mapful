package dev.steelahhh.mapful.presentation

import android.content.Context
import android.location.Geocoder
import android.view.View
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import dev.steelahhh.mapful.data.User

class BubbleInfoViewAdapter(
    private val geocoder: Geocoder,
    private val context: Context
) : GoogleMap.InfoWindowAdapter {
    override fun getInfoContents(marker: Marker): View? = null

    override fun getInfoWindow(marker: Marker): View = UserBubbleView(context).apply {
        bind(marker.tag as User, geocoder)
    }
}
