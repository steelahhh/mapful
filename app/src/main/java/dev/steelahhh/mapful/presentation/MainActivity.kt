package dev.steelahhh.mapful.presentation

import android.annotation.SuppressLint
import android.location.Geocoder
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import dev.steelahhh.mapful.R
import dev.steelahhh.mapful.data.User
import dev.steelahhh.mapful.maps.LatLngInterpolator
import dev.steelahhh.mapful.maps.MarkerAnimation
import io.ktor.util.KtorExperimentalAPI
import java.util.Locale
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import org.koin.androidx.viewmodel.ext.android.viewModel

@ExperimentalCoroutinesApi
@KtorExperimentalAPI
class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private lateinit var geocoder: Geocoder
    private lateinit var infoWindowAdapter: BubbleInfoViewAdapter
    private val vm: MainViewModel by viewModel()

    private var markers: MutableList<Marker> = mutableListOf()

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        geocoder = Geocoder(this, Locale.getDefault())
        infoWindowAdapter = BubbleInfoViewAdapter(geocoder, this)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        lifecycleScope.launchWhenStarted {
            vm.state.collect { state ->
                when (state) {
                    is MainViewModel.State.Idle -> Unit // do nothing here
                    is MainViewModel.State.New -> displayNewUsers(state.users)
                    is MainViewModel.State.Update -> updateMarkers(state.users)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        vm.start()
    }

    override fun onStop() {
        super.onStop()
        vm.stop()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        val topPadding = resources.getDimensionPixelSize(R.dimen.padding_48)
        val padding = resources.getDimensionPixelSize(R.dimen.padding_8)
        map.setPadding(padding, topPadding, padding, padding)
        map.setInfoWindowAdapter(infoWindowAdapter)
    }

    private fun displayNewUsers(users: List<User>) {
        // clear map and displayed markers
        map.clear()
        markers.clear()

        users.forEach { user ->
            // add marker to map, and set tag to it in order to render the info later
            val marker = map.addMarker(
                MarkerOptions()
                    .position(user.latLng)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.map_pin_icon))
            )
            marker.tag = user

            markers.add(marker)
        }

        animateToBounds(users)
    }

    private fun updateMarkers(users: List<User>) {
        animateToBounds(users)

        users.forEach { user ->
            val userMarker = markers.find { (it.tag as User).id == user.id }
            userMarker?.let { marker ->
                if (marker.position != user.latLng) {
                    // make InfoWindow reappear in order to update the address
                    val wasInfoWindowShown = marker.isInfoWindowShown

                    if (wasInfoWindowShown) marker.hideInfoWindow()

                    // update the user info in the marker
                    marker.tag = user

                    // animate the pin and map to new position
                    MarkerAnimation.animateMarker(
                        marker = marker,
                        finalPosition = user.latLng,
                        latLngInterpolator = LatLngInterpolator.Spherical()
                    )

                    if (wasInfoWindowShown) marker.showInfoWindow()
                }
            }
        }
    }

    private fun calculateBounds(
        coordinates: List<LatLng>
    ): LatLngBounds = LatLngBounds.Builder().apply {
        coordinates.forEach { include(it) }
    }.build()

    private fun animateToBounds(users: List<User>) {
        val bounds = calculateBounds(users.map { it.latLng })
        val update = CameraUpdateFactory.newLatLngBounds(bounds, 300)
        map.animateCamera(update)
    }
}
