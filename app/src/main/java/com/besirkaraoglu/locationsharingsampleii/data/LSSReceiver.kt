package com.besirkaraoglu.locationsharingsampleii.data

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.besirkaraoglu.locationsharingsampleii.CloudDbWrapper
import com.besirkaraoglu.locationsharingsampleii.model.Users
import com.besirkaraoglu.locationsharingsampleii.util.LocationLog
import com.besirkaraoglu.locationsharingsampleii.util.Utils.ACTION_PROCESS_LOCATION
import com.besirkaraoglu.locationsharingsampleii.util.Utils.isListenActivityConversion
import com.besirkaraoglu.locationsharingsampleii.util.Utils.isListenActivityIdentification
import com.huawei.agconnect.auth.AGConnectAuth
import com.huawei.hms.location.ActivityConversionResponse
import com.huawei.hms.location.ActivityIdentificationResponse
import com.huawei.hms.location.LocationAvailability
import com.huawei.hms.location.LocationResult


class LSSReceiver: BroadcastReceiver() {
    val TAG = "BroadcastReceiver"

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent != null) {
            val action = intent.action
            val sb = StringBuilder()
            if (ACTION_PROCESS_LOCATION == action) {
                // Processing LocationResult information
                Log.i(TAG, "null != intent")
                var messageBack = ""
                val activityTransitionResult =
                    ActivityConversionResponse.getDataFromIntent(intent)
                if (activityTransitionResult != null && isListenActivityConversion) {
                    val list =
                        activityTransitionResult.activityConversionDatas
                    for (i in list.indices) {
                        Log.i(TAG, "activityTransitionEvent[  $i ] $list[i]")
                        messageBack += """${list[i]}""".trimIndent()
                    }
                    LocationLog.d("TAG", messageBack)
                }

                if (LocationResult.hasResult(intent)) {
                    val result = LocationResult.extractResult(intent)
                    result?.let {
                        val locations =
                            result.locations
                        if (locations.isNotEmpty()) {
                            sb.append("requestLocationUpdatesWithIntent[Longitude,Latitude,Accuracy]:\r\n")
                            for (location in locations) {
                                sb.apply {
                                    append(location.longitude)
                                    append(",")
                                    append(location.latitude)
                                    append(",")
                                    append(location.accuracy)
                                    append("\r\n")
                                }
                            }
                            val user = Users()
                            val cUser = AGConnectAuth.getInstance().currentUser
                            user.uid = cUser.uid
                            user.name = cUser.displayName
                            user.isActive = true
                            user.latitude = locations.last().latitude
                            user.longitude = locations.last().longitude
                            user.photoUrl = cUser.photoUrl

                            CloudDbWrapper.upsertUser(user){
                                Log.d(TAG, "onReceive: Result $it")
                            }
                            
                        }
                    }
                }
                if (sb.isNotEmpty()) {
                    LocationLog.i(TAG, sb.toString())
                }
            }
        }
    }
}