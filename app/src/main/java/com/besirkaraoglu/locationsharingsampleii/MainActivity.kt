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
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.core.app.ActivityCompat
import com.huawei.hmf.tasks.OnFailureListener
import com.huawei.hmf.tasks.OnSuccessListener
import com.huawei.hms.location.*
import com.huawei.hms.maps.*
import com.huawei.hms.maps.model.LatLng
import com.huawei.hms.maps.model.Marker
import com.huawei.hms.maps.model.MarkerOptions

class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    val TAG = "MainActivity"

    private lateinit var buttonShare: Button
    private lateinit var buttonStop: Button
    private lateinit var etName: EditText

    private lateinit var mLocationRequest: LocationRequest
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var builder: Notification.Builder
    private lateinit var mNotification: Notification
    private lateinit var lastLocation: LatLng

    private lateinit var mMapView: MapView
    private var mMarker: Marker? = null
    private lateinit var hMap: HuaweiMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initListeners()

        initMapKit(savedInstanceState)

        checkPermissions()
        checkLocationSettings()
        initFusedLocation()
    }

    private fun initListeners() {
        buttonShare = findViewById(R.id.buttonShare)
        buttonStop = findViewById(R.id.buttonStop)
        etName = findViewById(R.id.etName)

        buttonShare.setOnClickListener {
            enableBackgroundNotification()
            requestLocationUpdates()
        }
        buttonStop.setOnClickListener {
            mMarker?.remove()
        }
    }

    private fun setCameraPos(latLng: LatLng) {
        val cameraUpdate = CameraUpdateFactory.newLatLng(latLng)
        hMap.animateCamera(cameraUpdate)
    }

    fun addMarker(title: String, snippet: String, latLng: LatLng) {
        if (null != mMarker) {
            mMarker?.remove()
        }
        val options = MarkerOptions()
            .position(latLng)
            .title("Hello Huawei Map")
            .snippet("This is a snippet!")
        mMarker = hMap.addMarker(options)
    }

    private fun initMapKit(savedInstanceState: Bundle?) {
        mMapView = findViewById(R.id.mapview_mapviewdemo)
        var mapViewBundle: Bundle? = null
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle("MapViewBundleKey")
        }
        mMapView.onCreate(mapViewBundle)
        mMapView.getMapAsync(this)
    }

    private fun stopRequestLocationUpdates() {
        fusedLocationProviderClient.disableBackgroundLocation()
    }

    private fun requestLocationUpdates() {
        val mLocationCallback: LocationCallback
        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                Log.d(
                    TAG,
                    "onLocationResult: ${locationResult.lastLocation.latitude}, ${locationResult.lastLocation.longitude}"
                )
                Log.d(TAG, "onLocationResult: ${locationResult.locations}")
                val latLng = LatLng(
                    locationResult.lastLocation.latitude,
                    locationResult.lastLocation.longitude
                )
                if (this@MainActivity::lastLocation.isInitialized && lastLocation != latLng) {
                    lastLocation = latLng
                    addMarker("", "", latLng)
                    setCameraPos(latLng)
                } else {
                    lastLocation = latLng
                    addMarker("", "", latLng)
                    setCameraPos(latLng)
                }
            }
        }
        fusedLocationProviderClient.requestLocationUpdates(
            mLocationRequest,
            mLocationCallback,
            Looper.getMainLooper()
        )
            .addOnSuccessListener {
                Log.d(TAG, "requestLocationUpdates: Success! $it")
            }
            .addOnFailureListener {
                Log.d(TAG, "requestLocationUpdates: Failed! ${it.cause}, ${it.message}")
            }
    }

    private fun enableBackgroundNotification() {
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
        fusedLocationProviderClient.enableBackgroundLocation(notificationId, mNotification)
    }

    private fun initFusedLocation() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
    }

    private fun checkLocationSettings() {
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

    private fun checkPermissions() {
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

    override fun onMapReady(p0: HuaweiMap?) {
        Log.d(TAG, "onMapReady: Map ready")
        if (p0 != null) {
            hMap = p0
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        var mapViewBundle: Bundle? = outState.getBundle("MapViewBundleKey")
        if (mapViewBundle == null) {
            mapViewBundle = Bundle()
            outState.putBundle("MapViewBundleKey", mapViewBundle)
        }

        mMapView.onSaveInstanceState(mapViewBundle)
    }

    override fun onStart() {
        super.onStart()
        mMapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mMapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mMapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        mMapView.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopRequestLocationUpdates()
        mMapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mMapView.onLowMemory()
    }
}