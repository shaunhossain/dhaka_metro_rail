package com.shaunhossain.dhakametrorail.utils

import android.content.Context
import androidx.core.content.ContextCompat
import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
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
