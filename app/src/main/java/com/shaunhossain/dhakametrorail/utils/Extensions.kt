package com.shaunhossain.dhakametrorail.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.IntentSender
import android.util.Log
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.mapbox.mapboxsdk.geometry.LatLng
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

@SuppressLint("SimpleDateFormat")
fun formatDate(timestampInMillis: Long): String? {
    val sdf = SimpleDateFormat("MM/dd/yyyy")
    return sdf.format(Date(timestampInMillis))
}

suspend fun readJSONFromAsset(activity: Activity, fileName: String): String? {
    var json: String? = null
    try {
        val  inputStream: InputStream = activity.assets.open(fileName)
        json = inputStream.bufferedReader().use{it.readText()}
    } catch (ex: Exception) {
        ex.printStackTrace()
        return null
    }
    return json
}

 fun getCirclePoints(position: LatLng, radius: Double): ArrayList<LatLng> {
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
