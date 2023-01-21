package com.shaunhossain.dhakametrorail.ui.map_route

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.app.Activity
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.gson.Gson
import com.mapbox.geojson.Feature
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.layers.LineLayer
import com.mapbox.mapboxsdk.style.layers.Property
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import com.shaunhossain.dhakametrorail.R
import com.shaunhossain.dhakametrorail.databinding.FragmentMapRouteBinding
import com.shaunhossain.dhakametrorail.model.route_model.RouteModel
import com.shaunhossain.dhakametrorail.utils.Constants.STYLE_URL
import com.shaunhossain.dhakametrorail.utils.hasLocationPermission
import com.shaunhossain.dhakametrorail.utils.readJSONFromAsset
import java.io.InputStream


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
        if (!requireContext().hasLocationPermission()) {
            userCurrentLocation()
            throw Exception("No permission")
        }
        userCurrentLocation()
        mapView?.getMapAsync { map ->
            // Set the style after mapView was loaded
            mMap = map
            map.uiSettings.setAttributionMargins(15, 0, 0, 15)
            map.setStyle(STYLE_URL) {
                style = it

                try {
                    panToSlopes(map)
                    drawDirectionLine(it)
                } catch (e: Exception) {

                }

                try {
                    map.cameraPosition = CameraPosition.Builder()
                        .target(LatLng(currentLocation!!.latitude , currentLocation!!.longitude))
                        .zoom(12.0)
                        .build()
                } catch (e: Exception) {

                }
            }


        }
    }

    private fun panToSlopes(map: MapboxMap) {
        val latLngBounds = LatLngBounds.Builder()
            .include(
                LatLng(
                    23.707310,
                    90.415480
                )
            )
            .include(
                LatLng(
                    currentLocation!!.latitude ?: 23.82155,
                    currentLocation!!.latitude ?: 90.393
                )
            )
            .build()
        map.animateCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 10))
    }

    private fun userCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(requireActivity(), ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireActivity(),
                ACCESS_COARSE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(ACCESS_FINE_LOCATION),
                REQUEST_CODE
            )
            return
        }

        val task = fusedLocationProviderClient!!.lastLocation

        task.addOnSuccessListener { location ->
            if (location != null) {
                //Log.d("current_location", location.latitude.toString())
                currentLocation = location
            }
        }
    }

    private fun drawDirectionLine(style: Style) {
        val polygonFeatureJson = """{
                                "type": "Feature",
                                "properties": {},
                                "geometry": {
                                "type": "LineString",
                                "coordinates": ${parseJSON().features?.get(0)?.geometry?.coordinates} }} """

        Log.d("coordinate",parseJSON().features?.get(0)?.geometry?.coordinates.toString())


        val parisBoundariesFeature = polygonFeatureJson?.let { Feature.fromJson(it) }
        val geoJsonSource =
            GeoJsonSource("geojson-paris-boundaries", parisBoundariesFeature)
        style.removeSource("geojson-paris-boundaries")
        style.removeLayer("linelayer")
        style.addSource(geoJsonSource)

        // Create a layer with the desired style for our source.
        val layer = LineLayer("linelayer", "geojson-paris-boundaries")
            .withProperties(
                PropertyFactory.lineCap(Property.LINE_CAP_SQUARE),
                PropertyFactory.lineJoin(Property.LINE_JOIN_MITER),
                PropertyFactory.lineOpacity(.7f),
                PropertyFactory.lineWidth(1f),
                PropertyFactory.lineColor(resources.getColor(R.color.black))
            )
        // Add it to the map
        style.addLayer(layer)
    }


    private fun parseJSON(): RouteModel {
        return Gson().fromJson(readJSONFromAsset(activity = requireActivity(),"metro_route.json"), RouteModel::class.java)
    }

}