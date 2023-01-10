package com.shaunhossain.dhakametrorail.ui.map_route

import android.location.Location
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.shaunhossain.dhakametrorail.databinding.FragmentMapRouteBinding
import com.shaunhossain.dhakametrorail.utils.Constants.STYLE_URL


class MapRouteFragment : Fragment() {
    private var _binding: FragmentMapRouteBinding? = null
    private val binding get() = _binding!!

    private var mapView: MapView? = null
    private var mMap: MapboxMap? = null
    private var style: Style? = null

    var currentLocation: Location? = null
    var fusedLocationProviderClient: FusedLocationProviderClient? = null
    val REQUEST_CODE = 101

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapRouteBinding.inflate(inflater, container, false)
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())
        mapView = _binding!!.mapView
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView?.onCreate(savedInstanceState)
        mapView?.getMapAsync { map ->
            // Set the style after mapView was loaded
            mMap = map
            map.uiSettings.setAttributionMargins(15, 0, 0, 15)
            map.setStyle(STYLE_URL) {
                style = it

                try {
                    map.cameraPosition = CameraPosition.Builder()
                        .target(LatLng(currentLocation!!.latitude, currentLocation!!.longitude))
                        .zoom(12.0)
                        .build()
                } catch (e: Exception) {

                }
            }


        }
    }

}