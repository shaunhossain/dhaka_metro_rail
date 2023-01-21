package com.shaunhossain.dhakametrorail.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.util.Log
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("SimpleDateFormat")
fun formatDate(timestampInMillis: Long): String? {
    val sdf = SimpleDateFormat("MM/dd/yyyy")
    return sdf.format(Date(timestampInMillis))
}

fun readJSONFromAsset(activity: Activity, fileName: String): String? {
    var json: String? = null
    try {
        val  inputStream: InputStream = activity.assets.open(fileName)
        json = inputStream.bufferedReader().use{it.readText()}
        Log.d("readFromAsset", json)

    } catch (ex: Exception) {
        ex.printStackTrace()
        return null
    }
    return json
}