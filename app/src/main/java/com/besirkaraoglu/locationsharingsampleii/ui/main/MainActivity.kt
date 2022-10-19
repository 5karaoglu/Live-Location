package com.besirkaraoglu.locationsharingsampleii.ui.main

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.ToggleButton
import androidx.activity.viewModels
import androidx.appcompat.widget.SwitchCompat
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.besirkaraoglu.locationsharingsampleii.CloudDbWrapper
import com.besirkaraoglu.locationsharingsampleii.R
import com.besirkaraoglu.locationsharingsampleii.data.LSSReceiver
import com.besirkaraoglu.locationsharingsampleii.model.Users
import com.besirkaraoglu.locationsharingsampleii.util.*
import com.besirkaraoglu.locationsharingsampleii.util.Utils.ACTION_PROCESS_LOCATION
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.huawei.agconnect.auth.AGConnectAuth
import com.huawei.hmf.tasks.OnFailureListener
import com.huawei.hmf.tasks.OnSuccessListener
import com.huawei.hms.location.*
import com.huawei.hms.maps.*
import com.huawei.hms.maps.model.LatLng
import com.huawei.hms.maps.model.Marker
import com.huawei.hms.maps.model.MarkerOptions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.math.log

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), OnMapReadyCallback, UsersAdapter.OnItemClickListener {
    val TAG = "MainActivity"

    private val viewModel: MainViewModel by viewModels()

    private lateinit var switchLocation: SwitchMaterial
    private lateinit var usersAdapter: UsersAdapter
    private lateinit var tvRvWarning: TextView

    private lateinit var mLocationRequest: LocationRequest
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var builder: Notification.Builder
    private lateinit var mNotification: Notification

    private lateinit var mMapView: MapView
    private var mMarker: Marker? = null
    private lateinit var hMap: HuaweiMap
    private val user = AGConnectAuth.getInstance().currentUser

    private val config = Firebase.remoteConfig

    private var locationRequestInterval = config.getLong(LOCATION_REQUEST_INTERVAL_KEY)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setConfigSettings()
        fetchRemoteConfigValues()

        initRecyclerView()
        initListeners()
        initCloudDBAndListenData()
        initMapKit(savedInstanceState)

        checkPermissions()
        checkLocationSettings()
        initFusedLocation()
    }

    private fun setConfigSettings() {
        val map = mutableMapOf<String, Any>()
        map[LOCATION_REQUEST_INTERVAL_KEY] = DEFAULT_INTERVAL_VALUE
        config.setDefaultsAsync(map)
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 10
        }
        config.setConfigSettingsAsync(configSettings)
    }

    private fun fetchRemoteConfigValues() {
        config.fetchAndActivate()
            .addOnCompleteListener { task ->
                if (task.isSuccessful)
                    Log.d(TAG, "fetchRemoteConfigValues: Task is successful.")
                else
                    Log.e(TAG, "fetchRemoteConfigValues: Task failed! Cause: ${task.exception?.cause}")
                locationRequestInterval = config.getLong(LOCATION_REQUEST_INTERVAL_KEY)
            }
    }

    private fun initRecyclerView() {
        tvRvWarning = findViewById(R.id.tvRvWarning)
        usersAdapter = UsersAdapter(this)
        val rv = findViewById<RecyclerView>(R.id.rvUsers)
        with(rv) {
            layoutManager = LinearLayoutManager(
                this@MainActivity,
                LinearLayoutManager.HORIZONTAL, false
            )
            adapter = usersAdapter
        }
    }

    private fun initCloudDBAndListenData() {
        CloudDbWrapper.initialize(this) {
            if (it) {
                viewModel.userData.observe(this) { resource ->
                    when (resource) {
                        is Resource.Loading -> {
                            Log.d(TAG, "onCreate: Loading...")
                        }
                        is Resource.Empty -> {
                            Log.d(TAG, "onCreate: Result is empty.")
                            mMarker?.remove()
                        }
                        is Resource.Error -> {
                            Log.e(TAG, "onCreate: Error! ${resource.exception.cause}")
                        }
                        is Resource.Success -> {
                            Log.d(TAG, "onCreate: success called")
                            if (this::hMap.isInitialized) {
                                clearMap()
                                val userList = resource.data
                                userList.sortedByDescending { it1 -> it1.name }
                                tvRvWarning.isVisible = userList.isEmpty()
                                usersAdapter.setUserList(userList)
                                for (i in userList) {
                                    addMarker(
                                        i.name, LatLng(i.latitude, i.longitude)
                                    )
                                }
                            }
                        }
                        else -> {}
                    }
                }
            }
        }
    }

    private fun clearMap() {
        hMap.clear()
    }

    private fun initListeners() {
        switchLocation = findViewById(R.id.tbLocation)
        switchLocation.setOnCheckedChangeListener { compoundButton, isChecked ->
            if (isChecked) {
                enableBackgroundNotification()
                requestLocationUpdatesWithIntent()
            } else {
                stopRequestLocationUpdates()
            }
        }
    }

    private fun setCameraPos(latLng: LatLng) {
        val cameraUpdate = CameraUpdateFactory.newLatLng(latLng)
        hMap.animateCamera(cameraUpdate)
    }

    private fun addMarker(name: String, latLng: LatLng) {
        val options = MarkerOptions()
            .position(latLng)
            .title(name)
        hMap.addMarker(options)
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
        removeLocationUpdatesWithIntent()
        viewModel.deleteLocation(user.uid) {
            Log.d(TAG, "stopRequestLocationUpdates: Delete result $it")
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun requestLocationUpdatesWithIntent() {
        GlobalScope.launch {
            try {
                val locationRequest = LocationRequest().apply {
                    this.interval = locationRequestInterval
                    this.needAddress = true
                    this.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                }
                fusedLocationProviderClient.requestLocationUpdates(
                    locationRequest,
                    getPendingIntent()
                )
                    .addOnSuccessListener {
                        LocationLog.i(TAG, "requestLocationUpdatesWithIntent onSuccess")
                    }
                    .addOnFailureListener { e ->
                        LocationLog.i(
                            TAG,
                            "requestLocationUpdatesWithIntent onFailure:" + e.message
                        )
                    }
            } catch (e: Exception) {
                LocationLog.e(TAG, "requestLocationUpdatesWithIntent exception:" + e.message)
            }
        }
    }

    @SuppressLint("WrongConstant")
    private fun getPendingIntent(): PendingIntent? {
        val intent = Intent(
            this@MainActivity,
            LSSReceiver::class.java
        )
        intent.action = ACTION_PROCESS_LOCATION
        return if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R) {
            PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        } else {
            // For Android 12 or later devices, proactively configure the pendingIntent variability.
            // The default value is PendingIntent.FLAG_MUTABLE. If compileSDKVersion is 30 or less, set this parameter
            // to 1<<25.
            PendingIntent.getBroadcast(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or (1 shl 25)
            )
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun removeLocationUpdatesWithIntent() {
        GlobalScope.launch {
            try {
                fusedLocationProviderClient.removeLocationUpdates(getPendingIntent())
                    .addOnSuccessListener {
                        LocationLog.i(TAG, "removeLocationUpdatesWithIntent onSuccess")
                    }
                    .addOnFailureListener { e ->
                        LocationLog.i(TAG, "removeLocationUpdatesWithIntent onFailure:" + e.message)
                    }
                LocationLog.i(TAG, "removeLocationUpdatesWithIntent call finish")
            } catch (e: java.lang.Exception) {
                LocationLog.e(TAG, "removeLocationUpdatesWithIntent exception:" + e.message)
            }
        }
    }


    private fun enableBackgroundNotification() {
        val notificationId = 1
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
        val settingsClient = LocationServices.getSettingsClient(this)
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

    override fun onItemClicked(users: Users) {
        setCameraPos(LatLng(users.latitude, users.longitude))
    }
}