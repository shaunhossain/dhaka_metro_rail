package com.shaunhossain.dhakametrorail.utils

import android.content.Context
import androidx.core.content.ContextCompat
import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.IntentSender
import android.content.pm.PackageManager
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import java.text.SimpleDateFormat
import java.util.*


fun Context.hasLocationPermission(): Boolean {
    return ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
}

fun showEnableLocationSetting(activity: Activity?) {
    val locationRequest = LocationRequest.create()
    locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    val builder = LocationSettingsRequest.Builder()
        .addLocationRequest(locationRequest)
    val task = LocationServices.getSettingsClient(
        activity!!
    )
        .checkLocationSettings(builder.build())
    task.addOnSuccessListener(
        activity
    ) { response ->
        val states = response.locationSettingsStates
        if (states!!.isLocationPresent) {
            //Do something
        }
    }
    task.addOnFailureListener(activity) { e ->
        if (e is ResolvableApiException) {
            try {
                e.startResolutionForResult(
                    activity,
                    999
                )
            } catch (sendEx: IntentSender.SendIntentException) {
                // Ignore the error.
            }
        }
    }
}
