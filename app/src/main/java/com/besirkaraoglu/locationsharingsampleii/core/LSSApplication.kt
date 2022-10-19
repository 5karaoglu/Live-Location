package com.besirkaraoglu.locationsharingsampleii.core

import android.app.Application
import com.besirkaraoglu.locationsharingsampleii.util.*
import com.huawei.agconnect.AGConnectInstance
import com.huawei.agconnect.AGConnectOptionsBuilder
import com.huawei.hms.maps.MapsInitializer
import dagger.hilt.android.HiltAndroidApp
import java.io.IOException


@HiltAndroidApp
class LSSApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        MapsInitializer.setApiKey(API_KEY)
        MapsInitializer.initialize(this)

        try {
            val builder = AGConnectOptionsBuilder()
            builder.setClientId(CLIENT_ID)
            builder.setClientSecret(CLIENT_SECRET)
            builder.setApiKey(API_KEY)
            builder.setCPId(CP_ID)
            builder.setProductId(PRODUCT_ID)
            builder.setAppId(APP_ID)
            AGConnectInstance.initialize(this, builder)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}