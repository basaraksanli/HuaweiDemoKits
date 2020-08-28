package com.example.basardemo

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.IntentSender
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.location.Location
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.core.app.ActivityCompat
import com.huawei.hmf.tasks.Task
import com.huawei.hms.common.ApiException
import com.huawei.hms.common.ResolvableApiException
import com.huawei.hms.location.*

class LocationActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "LocationMainActivity"
    }

    private lateinit var logText: TextView
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var settingsClient: SettingsClient
    private lateinit var mLocationRequest: LocationRequest
    private lateinit var mLocationCallback: LocationCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location)

        logText = findViewById(R.id.logText)

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            Log.i(TAG, "sdk < 28 Q")
            if (checkSelfPermission(ACCESS_FINE_LOCATION) != PERMISSION_GRANTED && checkSelfPermission(ACCESS_COARSE_LOCATION) != PERMISSION_GRANTED
            ) {
                val strings = arrayOf(
                    ACCESS_FINE_LOCATION,
                    ACCESS_COARSE_LOCATION
                )
                ActivityCompat.requestPermissions(this, strings, 1)
            }
        } else {
            if (checkSelfPermission(ACCESS_FINE_LOCATION) != PERMISSION_GRANTED && checkSelfPermission(ACCESS_COARSE_LOCATION) != PERMISSION_GRANTED && checkSelfPermission("android.permission.ACCESS_BACKGROUND_LOCATION") != PERMISSION_GRANTED
            ) {
                val strings = arrayOf(
                    ACCESS_FINE_LOCATION,
                    ACCESS_COARSE_LOCATION,
                    "android.permission.ACCESS_BACKGROUND_LOCATION"
                )
                ActivityCompat.requestPermissions(this, strings, 2)
            }
            else
                locationUpdate()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.size > 1 && grantResults[0] == PERMISSION_GRANTED && grantResults[1] == PERMISSION_GRANTED) {
                printLog("onRequestPermissionsResult: apply LOCATION PERMISSION successful\n\n")
                locationUpdate()
            } else {
                printLog("onRequestPermissionsResult: apply LOCATION PERMISSSION  failed\n\n")
            }
        }
        else if (requestCode == 2) {
            if (grantResults.size > 2 && grantResults[2] == PERMISSION_GRANTED && grantResults[0] == PERMISSION_GRANTED && grantResults[1] == PERMISSION_GRANTED) {
                printLog("onRequestPermissionsResult: apply ACCESS_BACKGROUND_LOCATION successful\n\n")
                locationUpdate()
            } else {
                printLog("onRequestPermissionsResult: apply ACCESS_BACKGROUND_LOCATION failed\n\n")
            }
        }
        else
            locationUpdate()
    }

    private fun printLog(log: String) {
        val logText = findViewById<TextView>(R.id.logText)
        logText.append(log)
    }

    private fun locationUpdate() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        settingsClient = LocationServices.getSettingsClient(this)

        mLocationRequest = LocationRequest().apply {
            // set the interval for location updates, in milliseconds
            interval = 1000
            needAddress = true
            // set the priority of the request
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                if (locationResult != null) {
                    val locations: List<Location> =
                        locationResult.locations
                    if (locations.isNotEmpty()) {
                        for (location in locations) {
                            printLog(
                                "onLocationResult location[Longitude,Latitude,Accuracy]:${location.longitude} , ${location.latitude} , ${location.accuracy}\n\n"
                            )
                        }
                    }
                }
            }

            override fun onLocationAvailability(locationAvailability: LocationAvailability?) {
                locationAvailability?.let {
                    val flag: Boolean = locationAvailability.isLocationAvailable
                    printLog("onLocationAvailability isLocationAvailable:$flag")
                }
            }
        }

    }

    fun removeLocationUpdatesWithCallback(view: View) {
        try {
            fusedLocationProviderClient.removeLocationUpdates(mLocationCallback)
                .addOnSuccessListener {
                    printLog(
                        "removeLocationUpdatesWithCallback onSuccess\n\n"
                    )
                }
                .addOnFailureListener { e ->
                    printLog(
                        "removeLocationUpdatesWithCallback onFailure:${e.message}\n\n"
                    )
                }
        } catch (e: Exception) {
            printLog("removeLocationUpdatesWithCallback exception:${e.message}\n\n"
            )
        }
    }

    fun requestLocationUpdatesWithCallback() {
        try {
            val builder = LocationSettingsRequest.Builder()
            builder.addLocationRequest(mLocationRequest)
            val locationSettingsRequest = builder.build()
            // check devices settings before request location updates.
            //Before requesting location update, invoke checkLocationSettings to check device settings.
            val locationSettingsResponseTask: Task<LocationSettingsResponse> =
                settingsClient.checkLocationSettings(locationSettingsRequest)

            locationSettingsResponseTask.addOnSuccessListener { locationSettingsResponse: LocationSettingsResponse? ->
                Log.i(TAG, "check location settings success  {$locationSettingsResponse}")
                // request location updates
                fusedLocationProviderClient.requestLocationUpdates(
                    mLocationRequest,
                    mLocationCallback,
                    Looper.getMainLooper()
                )
                    .addOnSuccessListener {
                        printLog("requestLocationUpdatesWithCallback onSuccess\n\n")
                    }
                    .addOnFailureListener { e ->
                        printLog("requestLocationUpdatesWithCallback onFailure:${e.message}\n\n")
                    }
            }
                .addOnFailureListener { e: Exception ->
                    printLog("checkLocationSetting onFailure:${e.message}\n\n")
                    when ((e as ApiException).statusCode) {
                        LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> try {
                            val rae = e as ResolvableApiException
                            rae.startResolutionForResult(
                                this@LocationActivity, 0
                            )
                        } catch (sie: IntentSender.SendIntentException) {
                            Log.e(TAG, "PendingIntent unable to execute request.\n\n")
                        }
                    }
                }
        } catch (e: Exception) {
            printLog("requestLocationUpdatesWithCallback exception:${e.message}\n\n")
        }
    }

}