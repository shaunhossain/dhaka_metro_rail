package com.shaunhossain.dhakametrorail.ui.map_route

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.gson.Gson
import com.mapbox.geojson.Feature
import com.mapbox.mapboxsdk.annotations.IconFactory
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.markerview.MarkerView
import com.mapbox.mapboxsdk.plugins.markerview.MarkerViewManager
import com.mapbox.mapboxsdk.style.layers.LineLayer
import com.mapbox.mapboxsdk.style.layers.Property
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.*
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import com.mapbox.mapboxsdk.utils.BitmapUtils
import com.shaunhossain.dhakametrorail.R
import com.shaunhossain.dhakametrorail.databinding.FragmentMapRouteBinding
import com.shaunhossain.dhakametrorail.model.coordinate_model.CoordinateFeature
import com.shaunhossain.dhakametrorail.model.coordinate_model.CoordinateModel
import com.shaunhossain.dhakametrorail.model.route_model.RouteModel
import com.shaunhossain.dhakametrorail.utils.Constants.STYLE_URL
import com.shaunhossain.dhakametrorail.utils.hasLocationPermission
import com.shaunhossain.dhakametrorail.utils.readJSONFromAsset


class MapRouteFragment : Fragment() {
    private var _binding: FragmentMapRouteBinding? = null
    private val binding get() = _binding!!

    private lateinit var mapView: MapView
    private lateinit var mMap: MapboxMap
    private lateinit var style: Style
    private lateinit var markerViewManager: MarkerViewManager
    private lateinit var marker: MarkerView


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
        mapView.onCreate(savedInstanceState)
        if (!requireContext().hasLocationPermission()) {
            userCurrentLocation()
            throw Exception("No permission")
        }


        userCurrentLocation()
        bindUserCurrentLocationButton()


        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync { map ->
            // Set the style after mapView was loaded
            mMap = map
            map.uiSettings.setAttributionMargins(15, 0, 0, 15)
            map.setStyle(STYLE_URL) { it ->
                style = it
                markerViewManager = MarkerViewManager(mapView, map)

                try {
                    panToSlopes(map)
                    drawDirectionLine(it)
                } catch (e: Exception) {

                }

                try {
                    map.cameraPosition = CameraPosition.Builder()
                        .target(LatLng(currentLocation!!.latitude, currentLocation!!.longitude))
                        .zoom(12.0)
                        .build()
                } catch (e: Exception) {

                }

                try {
                    for (item in getStationList()!!) {
                        Log.d("Station", item?.properties?.name!!)
                        item.geometry.let { station ->

                            val options: MarkerOptions = MarkerOptions()
                            options.icon = IconFactory.recreate(
                                "location",
                                BitmapUtils.getBitmapFromDrawable(resources.getDrawable(R.drawable.ic_metro))!!
                            )
                            options.title = item.properties.station
                            options.position =
                                LatLng(station?.coordinates!![1]!!, station.coordinates[0]!!)
                            options.snippet(item.properties.address);
                            mMap.addMarker(options)

                            createCustomMarker(
                                LatLng(
                                    station.coordinates[1]!!,
                                    station.coordinates[0]!!
                                ),
                                item.properties.name
                            )
                            mMap.moveCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    LatLng(
                                        currentLocation?.latitude!!,
                                        currentLocation?.longitude!!
                                    ), 15.0
                                )
                            )

                        }
                    }
                } catch (e: Exception) {

                }
            }


        }
    }

    private fun bindUserCurrentLocationButton() {
        binding.currentLocationButton.setOnClickListener {
            userCurrentLocation()

            try {
                updateCameraPosition(
                    LatLng(
                        currentLocation!!.latitude,
                        currentLocation!!.longitude
                    ), 14.0
                )


                val options: MarkerOptions = MarkerOptions()
                options.position = LatLng(currentLocation!!.latitude, currentLocation!!.longitude)
                options.snippet("this is stack location ");
                mMap.addMarker(options)

            } catch (e: Exception) {

            }
        }
    }

    private fun createCustomMarker(latLng: LatLng, stationName: String) {
        // create a custom animation marker view
        val customView = createCustomAnimationView(stationName)
        marker = MarkerView(latLng, customView)
        marker.let {
            markerViewManager.addMarker(it)
        }
    }

    private fun createCustomAnimationView(stationName: String): View {
        val customView = LayoutInflater.from(requireActivity()).inflate(R.layout.marker_view, null)
        customView.layoutParams = FrameLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {
            this.gravity = Gravity.LEFT
            this.setMargins(0, 0, 0, 0)
        }
        val stationThum = customView.findViewById<TextView>(R.id.imageview)
        stationThum.text = stationName
        stationThum.setOnClickListener { view ->
            findNavController().navigate(R.id.action_mapRouteFragment_to_stationDetailsFragment)
        }
        return customView
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
                                "coordinates": ${addStationRoute().features?.get(0)?.geometry?.coordinates} }} """

        Log.d("coordinate", addStationRoute().features?.get(0)?.geometry?.coordinates.toString())


        val parisBoundariesFeature = polygonFeatureJson.let { Feature.fromJson(it) }
        val geoJsonSource =
            GeoJsonSource("geojson-paris-boundaries", parisBoundariesFeature)
        style.removeSource("geojson-paris-boundaries")
        style.removeLayer("linelayer")
        style.addSource(geoJsonSource)

        // Create a layer with the desired style for our source.
        val layer = LineLayer("linelayer", "geojson-paris-boundaries")
            .withProperties(
                lineCap(Property.LINE_CAP_SQUARE),
                lineJoin(Property.LINE_JOIN_MITER),
                lineOpacity(.7f),
                lineWidth(1f),
                lineDasharray(arrayOf(2f, 2f)),
                lineCap(Property.LINE_CAP_ROUND),
                lineJoin(Property.LINE_JOIN_ROUND),
                lineColor(resources.getColor(R.color.black)),
                visibility(Property.VISIBLE),
                circleRadius(25f),
                circleColor(Color.argb(1, 55, 148, 179))
            )
        // Add it to the map
        style.addLayer(layer)
    }

    private fun updateCameraPosition(location: LatLng, zoom: Double?) {
        mMap.moveCamera(
            CameraUpdateFactory.newLatLngZoom(
                location, zoom!!
            )
        )
    }


    private fun getStationList(): List<CoordinateFeature?>? {
        return addStationCoordinate().features
    }


    private fun addStationRoute(): RouteModel {
        return Gson().fromJson(
            readJSONFromAsset(activity = requireActivity(), "metro_route.json"),
            RouteModel::class.java
        )
    }

    private fun addStationCoordinate(): CoordinateModel {
        return Gson().fromJson(
            readJSONFromAsset(
                activity = requireActivity(),
                "metro_stations.json"
            ), CoordinateModel::class.java
        )
    }


    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

}