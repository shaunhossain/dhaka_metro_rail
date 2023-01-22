package com.shaunhossain.dhakametrorail.ui.map_route

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.gson.Gson
import com.mapbox.geojson.Feature
import com.mapbox.mapboxsdk.annotations.Icon
import com.mapbox.mapboxsdk.annotations.IconFactory
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import com.mapbox.mapboxsdk.annotations.PolygonOptions
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions
import com.mapbox.mapboxsdk.style.expressions.Expression.*
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
import kotlin.math.*


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
            map.setStyle(STYLE_URL) { it ->
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

                try {
                   for (item in getStationList()!!){
                       Log.d("Station",item?.properties?.name!!)
                      item.geometry.let { station ->

                          val options: MarkerOptions = MarkerOptions()
                          options.icon = IconFactory.recreate( "location",
                              BitmapUtils.getBitmapFromDrawable(resources.getDrawable(R.drawable.ic_metro))!!)
                          options.title = item.properties.name
                          options.position = LatLng(station?.coordinates!![1]!!, station.coordinates[0]!!)
                          options.snippet("this is stack location ");
                          mMap!!.addMarker(options)


                          val polygonOptions3 = PolygonOptions()
                          polygonOptions3.fillColor(Color.MAGENTA)
                          polygonOptions3.strokeColor(Color.BLUE)
                          polygonOptions3.alpha(1f)
                          polygonOptions3.addAll(getCirclePoints( LatLng(station?.coordinates!![1]!!, station.coordinates[0]!!), 80.0))
                          mMap!!.addPolygon(polygonOptions3)


                         // mMap?.addMarker(MarkerOptions().position(LatLng(station?.coordinates!![1]!!, station.coordinates[0]!!)).icon(icon))
                         // mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(currentLocation?.latitude!!, currentLocation?.longitude!!), 15.0))

                      }
                   }
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
                                "coordinates": ${addStationRoute().features?.get(0)?.geometry?.coordinates} }} """

        Log.d("coordinate",addStationRoute().features?.get(0)?.geometry?.coordinates.toString())


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
                lineDasharray(arrayOf(2f,2f)),
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

    private fun getCirclePoints(position: LatLng, radius: Double): ArrayList<LatLng>? {
        val degreesBetweenPoints = 10 // change here for shape
        val numberOfPoints = Math.floor(360 / degreesBetweenPoints.toDouble()).toInt()
        val distRadians = radius / 6371000.0 // earth radius in meters
        val centerLatRadians = position.latitude * Math.PI / 180
        val centerLonRadians = position.longitude * Math.PI / 180
        val polygons: ArrayList<LatLng> = ArrayList() // array to hold all the points
        for (index in 0 until numberOfPoints) {
            val degrees = index * degreesBetweenPoints.toDouble()
            val degreeRadians = degrees * Math.PI / 180
            val pointLatRadians = Math.asin(
                sin(centerLatRadians) * cos(distRadians)
                        + cos(centerLatRadians) * sin(distRadians) * cos(degreeRadians)
            )
            val pointLonRadians = centerLonRadians + Math.atan2(
                sin(degreeRadians)
                        * sin(distRadians) * cos(centerLatRadians),
                cos(distRadians) - sin(centerLatRadians) * sin(pointLatRadians)
            )
            val pointLat = pointLatRadians * 180 / Math.PI
            val pointLon = pointLonRadians * 180 / Math.PI
            val point = LatLng(pointLat, pointLon)
            polygons.add(point)
        }
        // add first point at end to close circle
        polygons.add(polygons[0])
        return polygons
    }


    private fun getStationList(): List<CoordinateFeature?>? {
      return addStationCoordinate().features
    }


    private fun addStationRoute(): RouteModel {
        return Gson().fromJson(readJSONFromAsset(activity = requireActivity(),"metro_route.json"), RouteModel::class.java)
    }

    private fun addStationCoordinate(): CoordinateModel {
        return Gson().fromJson(readJSONFromAsset(activity = requireActivity(),"metro_stations.json"), CoordinateModel::class.java)
    }

}