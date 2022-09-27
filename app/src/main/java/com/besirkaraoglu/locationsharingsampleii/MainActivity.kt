package com.besirkaraoglu.locationsharingsampleii

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import com.huawei.hmf.tasks.OnFailureListener
import com.huawei.hmf.tasks.OnSuccessListener
import com.huawei.hms.location.*

class MainActivity : AppCompatActivity() {
    val TAG = "MainActivity"

    private lateinit var mLocationRequest: LocationRequest
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var builder: Notification.Builder
    private lateinit var mNotification: Notification

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        checkPermissions()
        checkLocationSettings()
        initFusedLocation()
        enableBackgroundNotification()
        requestLocationUpdates()
    }

    private fun stopRequestLocationUpdates(){
        fusedLocationProviderClient.disableBackgroundLocation()
    }

    private fun requestLocationUpdates() {
        val mLocationCallback: LocationCallback
        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                Log.d(TAG, "onLocationResult: $locationResult")
            }
        }
        fusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.getMainLooper())
            .addOnSuccessListener {
                Log.d(TAG, "requestLocationUpdates: Success! $it")
            }
            .addOnFailureListener {
                Log.d(TAG, "requestLocationUpdates: Failed! ${it.cause}, ${it.message}")
            }
    }

    private fun enableBackgroundNotification(){
        var notificationId = 1
        builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channelId = this.packageName
            val notificationChannel =
                NotificationChannel(channelId, "LOCATION", NotificationManager.IMPORTANCE_LOW)
            notificationManager.createNotificationChannel(notificationChannel)
            Notification.Builder(this, channelId)
        } else {
            Notification.Builder(this)
        }
        mNotification =
            builder.build()
        fusedLocationProviderClient.enableBackgroundLocation(notificationId,mNotification)
    }

    private fun initFusedLocation(){
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
    }

    private fun checkLocationSettings(){
        var settingsClient = LocationServices.getSettingsClient(this)
        val builder = LocationSettingsRequest.Builder()
        mLocationRequest = LocationRequest()
        builder.addLocationRequest(mLocationRequest)
        val locationSettingsRequest = builder.build()
        // Check the device location settings.
        settingsClient.checkLocationSettings(locationSettingsRequest)
            // Define the listener for success in calling the API for checking device location settings.
            .addOnSuccessListener(OnSuccessListener { locationSettingsResponse ->
                val locationSettingsStates = locationSettingsResponse.locationSettingsStates
                val stringBuilder = StringBuilder()
                // Check whether the location function is enabled.
                stringBuilder.append("isLocationUsable=")
                    .append(locationSettingsStates.isLocationUsable)
                // Check whether HMS Core (APK) is available.
                stringBuilder.append(",\nisHMSLocationUsable=")
                    .append(locationSettingsStates.isHMSLocationUsable)
                Log.i(TAG, "checkLocationSetting onComplete:$stringBuilder")
            })
            // Define callback for failure in checking the device location settings.
            .addOnFailureListener(OnFailureListener { e ->
                Log.i(TAG, "checkLocationSetting onFailure:" + e.message + "${e.cause}")
            })
    }

    private fun checkPermissions(){
        // Dynamically apply for required permissions if the API level is 28 or smaller.
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            Log.i(TAG, "android sdk <= 28 Q")
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                val strings = arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
                ActivityCompat.requestPermissions(this, strings, 1)
            }
        } else {
            // Dynamically apply for required permissions if the API level is greater than 28. The android.permission.ACCESS_BACKGROUND_LOCATION permission is required.
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    "android.permission.ACCESS_BACKGROUND_LOCATION"
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                val strings = arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    "android.permission.ACCESS_BACKGROUND_LOCATION"
                )
                ActivityCompat.requestPermissions(this, strings, 2)
            }
        }
    }

    override fun onDestroy() {
        stopRequestLocationUpdates()
        super.onDestroy()
    }
}